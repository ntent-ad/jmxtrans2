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
import org.jmxtrans.query.Query;
import org.jmxtrans.query.ResultNameStrategy;
import org.jmxtrans.utils.PropertyPlaceholderResolver;
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

    private final PropertyPlaceholderResolver propertyPlaceholderResolver;

    private final Element configurationRoot;

    public XmlConfigParser(PropertyPlaceholderResolver propertyPlaceholderResolver, Element configurationRoot) {
        this.propertyPlaceholderResolver = propertyPlaceholderResolver;
        this.configurationRoot = configurationRoot;
    }

    @Nullable
    @Override
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

    @Nullable
    @Override
    public ResultNameStrategy parseResultNameStrategy() {
        NodeList resultNameStrategyNodeList = configurationRoot.getElementsByTagName("resultNameStrategy");

        ResultNameStrategy resultNameStrategy;
        switch (resultNameStrategyNodeList.getLength()) {
            case 0:
                resultNameStrategy = null;
                break;
            case 1:
                Element resultNameStrategyElement = (Element) resultNameStrategyNodeList.item(0);
                String resultNameStrategyClass = resultNameStrategyElement.getAttribute("class");
                if (resultNameStrategyClass.isEmpty())
                    throw new IllegalArgumentException("<resultNameStrategy> element must contain a 'class' attribute");

                try {
                    resultNameStrategy = (ResultNameStrategy) Class.forName(resultNameStrategyClass).newInstance();
                    Map<String, String> settings = new HashMap<String, String>();
                    NodeList settingsNodeList = resultNameStrategyElement.getElementsByTagName("*");
                    for (int j = 0; j < settingsNodeList.getLength(); j++) {
                        Element settingElement = (Element) settingsNodeList.item(j);
                        settings.put(settingElement.getNodeName(), propertyPlaceholderResolver.resolveString(settingElement.getTextContent()));
                    }
                    resultNameStrategy.postConstruct(settings);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Exception instantiating " + resultNameStrategyClass, e);
                }
                break;
            default:
                throw new IllegalStateException("More than 1 <resultNameStrategy> element found (" + resultNameStrategyNodeList.getLength() + ")");
        }
        return resultNameStrategy;
    }

    @Nonnull
    @Override
    public Collection<Query> parseQueries(@Nonnull ResultNameStrategy resultNameStrategy) {
        List<Query> result = new ArrayList<Query>();
        NodeList queries = configurationRoot.getElementsByTagName("query");
        for (int i = 0; i < queries.getLength(); i++) {
            Element queryElement = (Element) queries.item(i);
            String objectName = queryElement.getAttribute("objectName");
            String attribute = queryElement.getAttribute("attribute");
            String key = queryElement.hasAttribute("key") ? queryElement.getAttribute("key") : null;
            String resultAlias = queryElement.getAttribute("resultAlias");
            String type = queryElement.getAttribute("type");
            Integer position;
            try {
                position = queryElement.hasAttribute("position") ? Integer.parseInt(queryElement.getAttribute("position")) : null;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid 'position' attribute for query objectName=" + objectName +
                        ", attribute=" + attribute + ", resultAlias=" + resultAlias);

            }
            Query query = new Query(objectName, attribute, key, position, type, resultAlias, resultNameStrategy);
            result.add(query);
        }
        return unmodifiableCollection(result);
    }

    @Nonnull
    @Override
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
    @Override
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
                OutputWriter outputWriter = instantiateOutputWriter(outputWriterClass, settings);
                outputWriter = new OutputWriterCircuitBreakerDecorator(outputWriter);
                outputWriters.add(outputWriter);
            } catch (Exception e) {
                throw new IllegalArgumentException("Exception instantiating " + outputWriterClass, e);
            }
        }
        return unmodifiableCollection(outputWriters);
    }

    private OutputWriter instantiateOutputWriter(String outputWriterClass, Map<String, String> settings) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Class<OutputWriterFactory<?>> builderClass = (Class<OutputWriterFactory<?>>) Class.forName(outputWriterClass + "$Factory");
        OutputWriterFactory<?> builder = builderClass.newInstance();
        return builder.create(settings);
    }
}
