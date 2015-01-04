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
import org.jmxtrans.query.Invocation;
import org.jmxtrans.query.embedded.Query;
import org.jmxtrans.query.embedded.QueryAttribute;
import org.jmxtrans.utils.PropertyPlaceholderResolver;
import org.jmxtrans.utils.time.Interval;
import org.jmxtrans.utils.time.SystemClock;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public class XmlConfigParserTest {

    private Document configDocument;
    private Element config;
    private XmlConfigParser parser;

    @Before
    public void createEmptyConfig() throws ParserConfigurationException {
        configDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        config = configDocument.createElement("jmxtrans");
        configDocument.appendChild(config);
    }

    @Before
    public void createConfigurationParser() {
        parser = new XmlConfigParser(new PropertyPlaceholderResolver(), new SystemClock());
    }

    @Test
    public void defaultQueryPeriodIsReturnedWhenNotInConfigFile() throws ParserConfigurationException {
        parser.setConfiguration(configDocument);
        Configuration configuration = parser.parseConfiguration();
        assertThat(configuration.getQueryPeriod()).isEqualTo(new Interval(10, SECONDS));
    }

    @Test
    public void queryPeriodIsReadCorrectly() throws ParserConfigurationException {
        Element interval = configDocument.createElement("collectIntervalInSeconds");
        interval.setTextContent("10");
        config.appendChild(interval);

        parser.setConfiguration(configDocument);

        Configuration configuration = parser.parseConfiguration();

        assertThat(configuration.getQueryPeriod()).isEqualTo(new Interval(10, SECONDS));
    }

    @Test(expected = IllegalStateException.class)
    public void intervalMustBeAnInteger() throws ParserConfigurationException {
        Element interval = configDocument.createElement("collectIntervalInSeconds");
        interval.setTextContent("abc");
        config.appendChild(interval);

        parser.setConfiguration(configDocument);
        parser.parseInterval();
    }

    @Test(expected = IllegalStateException.class)
    public void multipleIntervalsAreNotAllowed() throws ParserConfigurationException {
        Element interval = configDocument.createElement("collectIntervalInSeconds");
        interval.setTextContent("10");
        config.appendChild(interval);

        Element secondInterval = configDocument.createElement("collectIntervalInSeconds");
        secondInterval.setTextContent("20");
        config.appendChild(secondInterval);

        parser.setConfiguration(configDocument);
        parser.parseInterval();
    }

    @Test
    public void emptyListReturnedWhenNoQueryInConfig() {
        parser.setConfiguration(configDocument);
        Configuration configuration = parser.parseConfiguration();

        assertThat(configuration.getQueries()).isEmpty();
    }

    @Test
    public void canParseSingleQuery() {
        Element queriesElement = configDocument.createElement("queries");
        config.appendChild(queriesElement);
        Element queryElement = configDocument.createElement("query");
        queriesElement.appendChild(queryElement);
        queryElement.setAttribute("objectName", "java.lang:type=OperatingSystem");
        queryElement.setAttribute("attribute", "attribute");
        queryElement.setAttribute("key", "key");
        queryElement.setAttribute("position", "1");
        queryElement.setAttribute("type", "type");
        queryElement.setAttribute("resultAlias", "resultAlias");

        parser.setConfiguration(configDocument);

        Iterable<Query> queries = parser.parseConfiguration().getQueries();

        assertThat(queries).hasSize(1);
        assertThat(queries).containsOnly(Query.builder()
                .withObjectName("java.lang:type=OperatingSystem")
                .withResultAlias("resultAlias")
                .addAttribute(QueryAttribute.builder("attribute").build())
                .build());
    }

    @Test
    public void emptyListReturnedWhenNoInvocationInConfig() {
        parser.setConfiguration(configDocument);
        assertThat(parser.parseConfiguration().getInvocations()).isEmpty();
    }

    @Test
    public void canParseSingleInvocation() {
        Element invocationsElement = configDocument.createElement("invocations");
        config.appendChild(invocationsElement);
        Element invocationElement = configDocument.createElement("invocation");
        invocationsElement.appendChild(invocationElement);
        invocationElement.setAttribute("objectName", "java.lang:type=OperatingSystem");
        invocationElement.setAttribute("operation", "operation");
        invocationElement.setAttribute("resultAlias", "resultAlias");

        parser.setConfiguration(configDocument);
        Iterable<Invocation> invocations = parser.parseConfiguration().getInvocations();

        assertThat(invocations).hasSize(1);
        assertThat(invocations).containsOnly(new Invocation(
                "java.lang:type=OperatingSystem",
                "operation",
                new Object[0],
                new String[0],
                "resultAlias"));
    }

    @Test
    public void emptyListReturnedWhenNoOutputWriterInConfig() {
        parser.setConfiguration(configDocument);
        assertThat(parser.parseConfiguration().getOutputWriters()).isEmpty();
    }

    @Test
    public void canParseSingleOutputWriter() {
        Element outputWriterElement = configDocument.createElement("outputWriter");
        config.appendChild(outputWriterElement);
        outputWriterElement.setAttribute("class", DummyOutputWriter.class.getName());

        parser.setConfiguration(configDocument);

        Iterable<OutputWriter> outputWriters = parser.parseConfiguration().getOutputWriters();

        assertThat(outputWriters).hasSize(1);
    }

}
