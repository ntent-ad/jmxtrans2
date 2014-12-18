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

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlConfigParserTest {

    @Test
    public void intervalIsNullWhenNotInConfig() throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element config = doc.createElement("jmxtrans");
        doc.appendChild(config);

        ConfigParser parser = new XmlConfigParser(new PropertyPlaceholderResolver(), config);
        assertThat(parser.parseInterval()).isNull();
    }

    @Test
    public void intervalIsReadCorrectly() throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element config = doc.createElement("jmxtrans");
        doc.appendChild(config);

        Element interval = doc.createElement("collectIntervalInSeconds");
        interval.setTextContent("10");
        config.appendChild(interval);

        ConfigParser parser = new XmlConfigParser(new PropertyPlaceholderResolver(), config);
        assertThat(parser.parseInterval()).isEqualTo(new Interval(10, TimeUnit.SECONDS));
    }

    @Test(expected = IllegalStateException.class)
    public void multipleIntervalsAreNotAllowed() throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element config = doc.createElement("jmxtrans");
        doc.appendChild(config);

        Element interval = doc.createElement("collectIntervalInSeconds");
        interval.setTextContent("10");
        config.appendChild(interval);

        Element secondInterval = doc.createElement("collectIntervalInSeconds");
        secondInterval.setTextContent("20");
        config.appendChild(secondInterval);

        ConfigParser parser = new XmlConfigParser(new PropertyPlaceholderResolver(), config);
        parser.parseInterval();
    }

}
