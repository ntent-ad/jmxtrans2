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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.parseInt;

public class XmlConfigParser implements ConfigParser {

    private final PropertyPlaceholderResolver propertyPlaceholderResolver;

    private final Element configurationRoot;

    public XmlConfigParser(PropertyPlaceholderResolver propertyPlaceholderResolver, Element configurationRoot) {
        this.propertyPlaceholderResolver = propertyPlaceholderResolver;
        this.configurationRoot = configurationRoot;
    }

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
                String outputWriterClass = resultNameStrategyElement.getAttribute("class");
                if (outputWriterClass.isEmpty())
                    throw new IllegalArgumentException("<resultNameStrategy> element must contain a 'class' attribute");

                try {
                    resultNameStrategy = (ResultNameStrategy) Class.forName(outputWriterClass).newInstance();
                    Map<String, String> settings = new HashMap<String, String>();
                    NodeList settingsNodeList = resultNameStrategyElement.getElementsByTagName("*");
                    for (int j = 0; j < settingsNodeList.getLength(); j++) {
                        Element settingElement = (Element) settingsNodeList.item(j);
                        settings.put(settingElement.getNodeName(), propertyPlaceholderResolver.resolveString(settingElement.getTextContent()));
                    }
                    resultNameStrategy.postConstruct(settings);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Exception instantiating " + outputWriterClass, e);
                }
                break;
            default:
                throw new IllegalStateException("More than 1 <resultNameStrategy> element found (" + resultNameStrategyNodeList.getLength() + ")");
        }
        return resultNameStrategy;
    }
}
