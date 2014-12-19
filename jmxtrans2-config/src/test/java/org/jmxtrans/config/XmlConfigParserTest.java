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

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.util.Collection;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public class XmlConfigParserTest {

    private Document configDocument;
    private Element config;

    @Before
    public void createEmptyConfig() throws ParserConfigurationException {
        configDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        config = configDocument.createElement("jmxtrans");
        configDocument.appendChild(config);
    }

    @Test
    public void intervalIsNullWhenNotInConfig() throws ParserConfigurationException {
        ConfigParser parser = new XmlConfigParser(new PropertyPlaceholderResolver(), config);
        assertThat(parser.parseInterval()).isNull();
    }

    @Test
    public void intervalIsReadCorrectly() throws ParserConfigurationException {
        Element interval = configDocument.createElement("collectIntervalInSeconds");
        interval.setTextContent("10");
        config.appendChild(interval);

        ConfigParser parser = new XmlConfigParser(new PropertyPlaceholderResolver(), config);
        assertThat(parser.parseInterval()).isEqualTo(new Interval(10, SECONDS));
    }

    @Test(expected = IllegalStateException.class)
    public void intervalMustBeAnInteger() throws ParserConfigurationException {
        Element interval = configDocument.createElement("collectIntervalInSeconds");
        interval.setTextContent("abc");
        config.appendChild(interval);

        new XmlConfigParser(new PropertyPlaceholderResolver(), config).parseInterval();
    }

    @Test(expected = IllegalStateException.class)
    public void multipleIntervalsAreNotAllowed() throws ParserConfigurationException {
        Element interval = configDocument.createElement("collectIntervalInSeconds");
        interval.setTextContent("10");
        config.appendChild(interval);

        Element secondInterval = configDocument.createElement("collectIntervalInSeconds");
        secondInterval.setTextContent("20");
        config.appendChild(secondInterval);

        new XmlConfigParser(new PropertyPlaceholderResolver(), config).parseInterval();
    }

    @Test
    public void resultNameStrategyIsNullWhenNotInConfig() throws ParserConfigurationException {
        ConfigParser parser = new XmlConfigParser(new PropertyPlaceholderResolver(), config);
        assertThat(parser.parseResultNameStrategy()).isNull();
    }

    @Test
    public void resultNameStrategyWithoutParametersIsInstantiated() throws ParserConfigurationException {
        Element resultNameStrategyConfig = configDocument.createElement("resultNameStrategy");
        resultNameStrategyConfig.setAttribute("class", DummyResultNameStrategy.class.getName());
        config.appendChild(resultNameStrategyConfig);

        ConfigParser parser = new XmlConfigParser(new PropertyPlaceholderResolver(), config);

        ResultNameStrategy resultNameStrategy = parser.parseResultNameStrategy();
        assertThat(resultNameStrategy).isNotNull();
        assertThat(resultNameStrategy).isExactlyInstanceOf(DummyResultNameStrategy.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resultNameStrategyWithoutClassIsInvalid() throws ParserConfigurationException {
        Element resultNameStrategyConfig = configDocument.createElement("resultNameStrategy");
        config.appendChild(resultNameStrategyConfig);

        new XmlConfigParser(new PropertyPlaceholderResolver(), config).parseResultNameStrategy();
    }

    @Test(expected = IllegalStateException.class)
    public void multipleResultNameStrategyAreNotAllowed() throws ParserConfigurationException {
        config.appendChild(configDocument.createElement("resultNameStrategy"));
        config.appendChild(configDocument.createElement("resultNameStrategy"));

        new XmlConfigParser(new PropertyPlaceholderResolver(), config).parseResultNameStrategy();
    }

    @Test
    public void emptyListReturnedWhenNoQueryInConfig() {
        ConfigParser configParser = new XmlConfigParser(new PropertyPlaceholderResolver(), config);
        assertThat(configParser.parseQueries(new DummyResultNameStrategy())).isEmpty();
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

        ConfigParser configParser = new XmlConfigParser(new PropertyPlaceholderResolver(), config);
        DummyResultNameStrategy resultNameStrategy = new DummyResultNameStrategy();
        Collection<Query> queries = configParser.parseQueries(resultNameStrategy);

        assertThat(queries).hasSize(1);
        assertThat(queries).containsOnly(new Query(
                "java.lang:type=OperatingSystem",
                "attribute",
                "key",
                1,
                "type",
                "resultAlias",
                resultNameStrategy));
    }

    @Test
    public void emptyListReturnedWhenNoInvocationInConfig() {
        ConfigParser configParser = new XmlConfigParser(new PropertyPlaceholderResolver(), config);
        assertThat(configParser.parseInvocations()).isEmpty();
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

        ConfigParser configParser = new XmlConfigParser(new PropertyPlaceholderResolver(), config);
        Collection<Invocation> invocations = configParser.parseInvocations();

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
        ConfigParser configParser = new XmlConfigParser(new PropertyPlaceholderResolver(), config);
        assertThat(configParser.parseOutputWriters()).isEmpty();
    }

    @Test
    public void canParseSingleOutputWriter() {
        Element outputWriterElement = configDocument.createElement("outputWriter");
        config.appendChild(outputWriterElement);
        outputWriterElement.setAttribute("class", DummyOutputWriter.class.getName());

        ConfigParser configParser = new XmlConfigParser(new PropertyPlaceholderResolver(), config);
        Collection<OutputWriter> outputWriters = configParser.parseOutputWriters();

        assertThat(outputWriters).hasSize(1);
        OutputWriter outputWriter = outputWriters.iterator().next();

        assertThat(outputWriter).isInstanceOf(OutputWriterCircuitBreakerDecorator.class);

        OutputWriterCircuitBreakerDecorator circuitBreaker = (OutputWriterCircuitBreakerDecorator) outputWriter;
        assertThat(circuitBreaker.delegate).isInstanceOf(DummyOutputWriter.class);
    }

}
