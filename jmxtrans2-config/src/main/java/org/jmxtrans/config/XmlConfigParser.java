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
package org.jmxtrans.config;

import org.jmxtrans.output.OutputWriter;
import org.jmxtrans.output.OutputWriterFactory;
import org.jmxtrans.query.Invocation;
import org.jmxtrans.query.embedded.Query;
import org.jmxtrans.utils.PropertyPlaceholderResolver;
import org.jmxtrans.utils.circuitbreaker.CircuitBreakerProxy;
import org.jmxtrans.utils.time.Clock;
import org.jmxtrans.utils.time.Interval;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.parseInt;
import static java.util.Collections.unmodifiableCollection;

public class XmlConfigParser implements ConfigParser {

    public static final Interval DEFAULT_QUERY_PERIOD = new Interval(10, TimeUnit.SECONDS);
    public static final int MAX_FAILURES = 5;
    public static final int DISABLE_DURATION_MILLIS = 60 * 1000;
    private final PropertyPlaceholderResolver propertyPlaceholderResolver;

    private Document configurationRoot;
    private final Clock clock;

    public XmlConfigParser(PropertyPlaceholderResolver propertyPlaceholderResolver, Clock clock) {
        this.propertyPlaceholderResolver = propertyPlaceholderResolver;
        this.clock = clock;
    }

    public void setConfiguration(Document configuration) {
        this.configurationRoot = configuration;
    }

    @Nullable
    public Interval parseInterval() {
        Interval result;
        NodeList collectIntervalNodeList = configurationRoot.getElementsByTagName("collectIntervalInSeconds");
        switch (collectIntervalNodeList.getLength()) {
            case 0:
                result = null;
                break;
            case 1:
                Element collectIntervalElement = (Element) collectIntervalNodeList.item(0);
                String collectIntervalString = propertyPlaceholderResolver.resolveString(collectIntervalElement.getTextContent());
                try {
                    result = new Interval(parseInt(collectIntervalString), TimeUnit.SECONDS);
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Invalid <collectIntervalInSeconds> value '" + collectIntervalString + "', integer expected", e);
                }
                break;
            default:
                throw new IllegalStateException("Multiple <collectIntervalInSeconds> in configuration");
        }
        return result;
    }

    @Nonnull
    public Collection<Query> parseQueries() {
        List<Query> result = new ArrayList<Query>();
        NodeList queries = configurationRoot.getElementsByTagName("query");
        for (int i = 0; i < queries.getLength(); i++) {
            Element queryElement = (Element) queries.item(i);
            String objectName = queryElement.getAttribute("objectName");
            String attribute = queryElement.getAttribute("attribute");
            String resultAlias = queryElement.getAttribute("resultAlias");
            result.add(Query.builder()
                    .withObjectName(objectName)
                    .addAttribute(attribute)
                    .withResultAlias(resultAlias)
                    .build());
        }
        return unmodifiableCollection(result);
    }

    @Nonnull
    public Collection<Invocation> parseInvocations() {
        List<Invocation> result = new ArrayList<Invocation>();
        NodeList invocations = configurationRoot.getElementsByTagName("invocation");
        for (int i = 0; i < invocations.getLength(); i++) {
            Element invocationElement = (Element) invocations.item(i);
            String objectName = invocationElement.getAttribute("objectName");
            String operation = invocationElement.getAttribute("operation");
            String resultAlias = invocationElement.getAttribute("resultAlias");

            result.add(new Invocation(objectName, operation, new Object[0], new String[0], resultAlias));
        }
        return unmodifiableCollection(result);
    }

    @Nonnull
    public Collection<OutputWriter> parseOutputWriters() {
        List<OutputWriter> outputWriters = new ArrayList<OutputWriter>();

        NodeList outputWriterNodeList = configurationRoot.getElementsByTagName("outputWriter");

        for (int i = 0; i < outputWriterNodeList.getLength(); i++) {
            Element outputWriterElement = (Element) outputWriterNodeList.item(i);
            String outputWriterClass = outputWriterElement.getAttribute("class");
            if (outputWriterClass.isEmpty()) {
                throw new IllegalArgumentException("<outputWriter> element must contain a 'class' attribute");
            }
            try {
                Map<String, String> settings = new HashMap<String, String>();
                NodeList settingsNodeList = outputWriterElement.getElementsByTagName("*");
                for (int j = 0; j < settingsNodeList.getLength(); j++) {
                    Element settingElement = (Element) settingsNodeList.item(j);
                    settings.put(settingElement.getNodeName(), propertyPlaceholderResolver.resolveString(settingElement.getTextContent()));
                }
                outputWriters.add(instantiateOutputWriter(outputWriterClass, settings));
            } catch (Exception e) {
                throw new IllegalArgumentException("Exception instantiating " + outputWriterClass, e);
            }
        }
        return unmodifiableCollection(outputWriters);
    }

    private OutputWriter instantiateOutputWriter(String outputWriterClass, Map<String, String> settings)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Class<OutputWriterFactory<?>> builderClass = (Class<OutputWriterFactory<?>>) Class.forName(outputWriterClass + "$Factory");
        OutputWriterFactory<?> builder = builderClass.newInstance();
        return CircuitBreakerProxy.create(
                clock,
                OutputWriter.class,
                builder.create(settings),
                MAX_FAILURES,
                DISABLE_DURATION_MILLIS);
    }

    @Override
    public Configuration parseConfiguration() {
        // Collection interval
        Interval collectionInterval = parseInterval();
        if (collectionInterval == null) {
            collectionInterval = DEFAULT_QUERY_PERIOD;
        }

        StandardConfiguration configuration = new StandardConfiguration();
        configuration.addInvocations(parseInvocations());
        configuration.addQueries(parseQueries());
        configuration.addOutputWriters(parseOutputWriters());
        configuration.setQueryPeriod(collectionInterval);
        return configuration;
    }
}
