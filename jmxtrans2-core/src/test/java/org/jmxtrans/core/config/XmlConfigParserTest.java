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
package org.jmxtrans.core.config;

import org.jmxtrans.core.query.Invocation;
import org.jmxtrans.core.query.embedded.Server;
import org.jmxtrans.utils.PropertyPlaceholderResolver;
import org.jmxtrans.utils.io.Resource;
import org.jmxtrans.utils.io.StandardResource;
import org.jmxtrans.utils.time.Interval;
import org.jmxtrans.utils.time.SystemClock;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.management.MalformedObjectNameException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Iterator;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public class XmlConfigParserTest {

    private XmlConfigParser parser;

    @Before
    public void createConfigurationParser() throws JAXBException, ParserConfigurationException, IOException, SAXException {
        parser = XmlConfigParser.newInstance(
                new PropertyPlaceholderResolverXmlPreprocessor(new PropertyPlaceholderResolver()),
                new SystemClock());
    }

    @Test(expected = UnmarshalException.class)
    public void invalidConfigurationThrowsException() throws JAXBException, SAXException, IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        Resource resource = new StandardResource("classpath:org/jmxtrans/core/config/invalid-configuration.xml");
        parser.parseConfiguration(resource);
    }

    @Test
    public void queriesAreParsed() throws IllegalAccessException, IOException, JAXBException, InstantiationException, SAXException, ClassNotFoundException {
        Resource resource = new StandardResource("classpath:org/jmxtrans/core/config/simple-configuration.xml");
        Configuration configuration = parser.parseConfiguration(resource);
        assertThat(configuration).isNotNull();
        assertThat(configuration.getServers()).hasSize(1);
        Server server = configuration.getServers().iterator().next();
        assertThat(server.getQueries()).hasSize(3);
        assertThat(configuration.getPeriod()).isEqualTo(new Interval(10, SECONDS));
    }

    @Test
    public void serversAreParsed() throws Exception {
        Resource resource = new StandardResource("classpath:org/jmxtrans/core/config/with-servers.xml");
        Configuration configuration = parser.parseConfiguration(resource);
        assertThat(configuration).isNotNull();
        assertThat(configuration.getServers()).hasSize(1);

        Server server = configuration.getServers().iterator().next();
        assertThat(server.getHost()).isEqualTo("host.test.net");
        assertThat(server.getQueries()).hasSize(1);
    }

    @Test
    public void invocationsAreParsed() throws JAXBException, SAXException, IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, MalformedObjectNameException {
        Resource resource = new StandardResource("classpath:org/jmxtrans/core/config/simple-configuration.xml");
        Configuration configuration = parser.parseConfiguration(resource);
        assertThat(configuration).isNotNull();
        assertThat(configuration.getInvocations()).hasSize(2);

        Iterator<Invocation> invocationIterator = configuration.getInvocations().iterator();

        Invocation gc = invocationIterator.next();
        assertThat(gc).isEqualTo(new Invocation("java.lang:type=Memory", "gc", new Object[0], new String[0], "jvm.gc", new SystemClock()));

        Invocation threadCpuTime = invocationIterator.next();
        assertThat(threadCpuTime).isEqualTo(new Invocation(
                "java.lang:type=Threading",
                "getThreadCpuTime",
                new Object[] { "1" },
                new String[] { "long" },
                "jvm.thread.cpu", new SystemClock()
        ));
    }

    @Test
    public void outputWritersAreParsed() throws JAXBException, SAXException, IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        Resource resource = new StandardResource("classpath:org/jmxtrans/core/config/simple-configuration.xml");
        Configuration configuration = parser.parseConfiguration(resource);
        assertThat(configuration).isNotNull();
        assertThat(configuration.getOutputWriters()).hasSize(2);
    }

    @Test
    public void defaultCollectIntervalIfNotConfigured() throws IllegalAccessException, IOException, JAXBException, InstantiationException, SAXException, ClassNotFoundException {
        Resource resource = new StandardResource("classpath:org/jmxtrans/core/config/no-collection-interval.xml");
        Configuration configuration = parser.parseConfiguration(resource);
        assertThat(configuration.getPeriod()).isNotNull();
        assertThat(configuration.getPeriod()).isEqualTo(new Interval(60, SECONDS));
    }
}
