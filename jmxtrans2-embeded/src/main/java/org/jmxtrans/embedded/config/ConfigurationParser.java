/**
 * The MIT License
 * Copyright (c) 2014 JMXTrans Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jmxtrans.embedded.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jmxtrans.embedded.EmbeddedJmxTransException;
import org.jmxtrans.embedded.util.json.PlaceholderEnabledJsonNodeFactory;
import org.jmxtrans.output.OutputWriter;
import org.jmxtrans.output.OutputWriterFactory;
import org.jmxtrans.query.embedded.Query;
import org.jmxtrans.query.embedded.QueryAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JSON Configuration parser to create {@link org.jmxtrans.embedded.EmbeddedJmxTrans}.
 *
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
public class ConfigurationParser {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ObjectMapper mapper;

    {
        mapper = new ObjectMapper();
        mapper.setNodeFactory(new PlaceholderEnabledJsonNodeFactory());
    }

    public Config loadConfigurations(List<String> configurationUrls) {
        Config config = new Config();
        for (String configurationUrl : configurationUrls) {
            try (InputStream in = openResource(configurationUrl)) {
                loadConfiguration(in, config);
            } catch (Exception e) {
                throw new EmbeddedJmxTransException("Exception loading configuration'" + configurationUrl + "'", e);
            }
        }
        return config;
    }

    private InputStream openResource(String configurationUrl) throws IOException {
        if (configurationUrl.startsWith("classpath:")) {
            logger.debug("mergeEmbeddedJmxTransConfiguration({})", configurationUrl);
            String path = configurationUrl.substring("classpath:".length());

            return Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream(path), "No file found for '" + configurationUrl + "'");
        } else {
            return new URL(configurationUrl).openStream();
        }
    }

    public void loadConfiguration(InputStream configuration, Config config) throws IOException {

        JsonNode configurationRootNode = mapper.readValue(configuration, JsonNode.class);
        for (JsonNode queryNode : configurationRootNode.path("queries")) {

            String objectName = queryNode.path("objectName").asText();
            Query query = new Query(objectName);
            config.addQuery(query);
            JsonNode resultAliasNode = queryNode.path("resultAlias");
            if (resultAliasNode.isMissingNode()) {
            } else if (resultAliasNode.isValueNode()) {
                query.setResultAlias(resultAliasNode.asText());
            } else {
                logger.warn("Ignore invalid node {}", resultAliasNode);
            }

            JsonNode attributesNode = queryNode.path("attributes");
            if (attributesNode.isMissingNode()) {
            } else if (attributesNode.isArray()) {
                Iterator<JsonNode> itAttributeNode = attributesNode.elements();
                while (itAttributeNode.hasNext()) {
                    JsonNode attributeNode = itAttributeNode.next();
                    parseQueryAttributeNode(query, attributeNode);
                }
            } else {
                logger.warn("Ignore invalid node {}", resultAliasNode);
            }

            JsonNode attributeNode = queryNode.path("attribute");
            parseQueryAttributeNode(query, attributeNode);
            logger.trace("Add {}", query);
        }

        List<OutputWriter> outputWriters = parseOutputWritersNode(configurationRootNode);
        config.addOutputWriters(outputWriters);

        JsonNode queryIntervalInSecondsNode = configurationRootNode.path("queryIntervalInSeconds");
        if (!queryIntervalInSecondsNode.isMissingNode()) {
            config.setQueryIntervalInSeconds(queryIntervalInSecondsNode.asInt());
        }

        JsonNode exportBatchSizeNode = configurationRootNode.path("exportBatchSize");
        if (!exportBatchSizeNode.isMissingNode()) {
            config.setExportedBatchSize(exportBatchSizeNode.asInt());
        }

        JsonNode numQueryThreadsNode = configurationRootNode.path("numQueryThreads");
        if (!numQueryThreadsNode.isMissingNode()) {
            config.setNumQueryThreads(numQueryThreadsNode.asInt());
        }

        JsonNode exportIntervalInSecondsNode = configurationRootNode.path("exportIntervalInSeconds");
        if (!exportIntervalInSecondsNode.isMissingNode()) {
            config.setExportIntervalInSeconds(exportIntervalInSecondsNode.asInt());
        }

        JsonNode numExportThreadsNode = configurationRootNode.path("numExportThreads");
        if (!numExportThreadsNode.isMissingNode()) {
            config.setNumExportThreads(numExportThreadsNode.asInt());
        }
    }

    private List<OutputWriter> parseOutputWritersNode(@Nonnull JsonNode outputWritersParentNode) {
        JsonNode outputWritersNode = outputWritersParentNode.path("outputWriters");
        List<OutputWriter> outputWriters = new ArrayList<OutputWriter>();
        if (outputWritersNode.isMissingNode()) {
        } else if (outputWritersNode.isArray()) {
            for (JsonNode outputWriterNode : outputWritersNode) {
                try {
                    String className = outputWriterNode.path("@class").asText();
                    OutputWriter outputWriter = instantiateOutputWriter(outputWriterNode, className);
                    logger.trace("Add {}", outputWriter);
                    outputWriters.add(outputWriter);
                } catch (Exception e) {
                    throw new EmbeddedJmxTransException("Exception converting settings " + outputWritersNode, e);
                }
            }
        } else {
            logger.warn("Ignore invalid node {}", outputWritersNode);
        }
        return outputWriters;
    }

    private OutputWriter instantiateOutputWriter(JsonNode outputWriterNode, String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException, JsonProcessingException {
        OutputWriterFactory<OutputWriter> factory = (OutputWriterFactory<OutputWriter>) Class.forName(className + "$Factory").newInstance();

        JsonNode settingsNode = outputWriterNode.path("settings");
        Map<String, String> settings = Collections.emptyMap();
        if (settingsNode.isMissingNode()) {
        } else if (settingsNode.isObject()) {
            ObjectMapper mapper = new ObjectMapper();
            settings = mapper.treeToValue(settingsNode, Map.class);
        } else {
            logger.warn("Ignore invalid node {}", outputWriterNode);
        }
        return factory.create(settings);
    }

    protected void parseQueryAttributeNode(@Nonnull Query query, @Nonnull JsonNode attributeNode) {
        if (attributeNode.isMissingNode()) {
        } else if (attributeNode.isValueNode()) {
            query.addAttribute(attributeNode.asText());
        } else if (attributeNode.isObject()) {
            List<String> keys = null;

            JsonNode keysNode = attributeNode.path("keys");
            if (keysNode.isMissingNode()) {
            } else if (keysNode.isArray()) {
                if (keys == null) {
                    keys = new ArrayList<>();
                }
                Iterator<JsonNode> itAttributeNode = keysNode.elements();
                while (itAttributeNode.hasNext()) {
                    JsonNode keyNode = itAttributeNode.next();
                    if (keyNode.isValueNode()) {
                        keys.add(keyNode.asText());
                    } else {
                        logger.warn("Ignore invalid node {}", keyNode);
                    }
                }
            } else {
                logger.warn("Ignore invalid node {}", keysNode);
            }

            JsonNode keyNode = attributeNode.path("key");
            if (keyNode.isMissingNode()) {
            } else if (keyNode.isValueNode()) {
                if (keys == null) {
                    keys = new ArrayList<>();
                }
                keys.add(keyNode.asText());
            } else {
                logger.warn("Ignore invalid node {}", keyNode);
            }

            String name = attributeNode.path("name").asText();
            JsonNode resultAliasNode = attributeNode.path("resultAlias");
            String resultAlias = resultAliasNode.isMissingNode() ? null : resultAliasNode.asText();
            JsonNode typeNode = attributeNode.path("type");
            String type = typeNode.isMissingNode() ? null : typeNode.asText();
            if (keys == null) {
                query.addAttribute(QueryAttribute.builder(name)
                        .withType(type)
                        .withResultAlias(resultAlias)
                        .build());
            } else {
                query.addAttribute(QueryAttribute.builder(name)
                        .withType(type)
                        .withResultAlias(resultAlias)
                        .withKeys(keys)
                        .build());
            }
        } else {
            logger.warn("Ignore invalid node {}", attributeNode);
        }
    }

}
