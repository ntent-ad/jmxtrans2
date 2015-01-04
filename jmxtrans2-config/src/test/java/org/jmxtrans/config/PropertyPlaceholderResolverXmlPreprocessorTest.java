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

import org.jmxtrans.utils.PropertyPlaceholderResolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyPlaceholderResolverXmlPreprocessorTest {

    private PropertyPlaceholderResolverXmlPreprocessor preprocessor;

    @Before
    public void configureProperties() {
        System.setProperty("property.name", "property.value");
    }

    @Before
    public void createPreprocessor() {
        preprocessor = new PropertyPlaceholderResolverXmlPreprocessor(new PropertyPlaceholderResolver());
    }

    @Test
    public void attributesArePreprocessed() throws ParserConfigurationException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = document.createElement("root");
        document.appendChild(root);
        root.setAttribute("testAttribute", "some value ${property.name}");

        Document processed = preprocessor.preprocess(document);

        Element processedRoot = processed.getDocumentElement();
        assertThat(processedRoot.getAttribute("testAttribute")).isEqualTo("some value property.value");
    }

    @Test
    public void textNodesArePreprocessed() throws ParserConfigurationException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = document.createElement("root");
        document.appendChild(root);
        root.setTextContent("some value ${property.name}");

        Document processed = preprocessor.preprocess(document);

        Element processedRoot = processed.getDocumentElement();
        assertThat(processedRoot.getTextContent()).isEqualTo("some value property.value");
    }

    @After
    public void resetProperties() {
        System.getProperties().remove("property.name");
    }

}
