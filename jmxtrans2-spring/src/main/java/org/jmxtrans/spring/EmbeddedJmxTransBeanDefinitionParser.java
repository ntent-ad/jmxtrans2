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
package org.jmxtrans.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link org.springframework.beans.factory.xml.BeanDefinitionParser} for an {@link org.jmxtrans.spring.EmbeddedJmxTransFactory}.
 *
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
public class EmbeddedJmxTransBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final String CONFIGURATION_ATTRIBUTE = "configuration";
    private static final String IGNORE_CONFIGURATION_NOT_FOUND_ATTRIBUTE = "ignore-configuration-not-found";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected Class getBeanClass(Element element) {
        return EmbeddedJmxTransFactory.class;
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
        String id = element.getAttribute(ID_ATTRIBUTE);
        return (StringUtils.hasText(id) ? id : "jmxtrans");
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        builder.setRole(BeanDefinition.ROLE_APPLICATION);
        builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));

        if (element.hasAttribute(IGNORE_CONFIGURATION_NOT_FOUND_ATTRIBUTE)) {
            builder.addPropertyValue("ignoreConfigurationNotFound", element.getAttribute(IGNORE_CONFIGURATION_NOT_FOUND_ATTRIBUTE));
        }
        List<String> configurationUrls = new ArrayList<String>();
        if (element.hasAttribute(CONFIGURATION_ATTRIBUTE)) {
            String configurationUrl = element.getAttribute(CONFIGURATION_ATTRIBUTE);
            logger.debug("Add configuration from attribute {}", configurationUrl);
            configurationUrls.add(configurationUrl);
        }

        NodeList configurationNodeList = element.getElementsByTagNameNS(element.getNamespaceURI(), CONFIGURATION_ATTRIBUTE);
        for (int i = 0; i < configurationNodeList.getLength(); i++) {
            Node node = configurationNodeList.item(i);
            if (node instanceof Element) {
                String configurationUrl = node.getTextContent();
                logger.debug("Add configuration from attribute {}", configurationUrl);
                configurationUrls.add(configurationUrl);
            } else {
                throw new RuntimeException("Invalid configuration child element " + node);
            }

        }
        builder.addPropertyValue("configurationUrls", configurationUrls);
    }
}
