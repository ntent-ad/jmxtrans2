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
package org.jmxtrans.agent;

import org.jmxtrans.config.ConfigParser;
import org.jmxtrans.config.Configuration;
import org.jmxtrans.config.PropertyPlaceholderResolverXmlPreprocessor;
import org.jmxtrans.config.XmlConfigParser;
import org.jmxtrans.scheduler.JmxTransThreadFactory;
import org.jmxtrans.scheduler.NaiveScheduler;
import org.jmxtrans.scheduler.QueryGenerator;
import org.jmxtrans.scheduler.QueryProcessor;
import org.jmxtrans.scheduler.ResultProcessor;
import org.jmxtrans.utils.PropertyPlaceholderResolver;
import org.jmxtrans.utils.io.Resource;
import org.jmxtrans.utils.time.Clock;
import org.jmxtrans.utils.time.SystemClock;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MINUTES;

public class JmxTransBuilder {

    @Nonnull private final String configFile;

    public JmxTransBuilder(@Nonnull String configFile) {
        this.configFile = configFile;
    }

    public NaiveScheduler build() throws ParserConfigurationException, IOException, SAXException, JAXBException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        SystemClock clock = new SystemClock();
        long shutdownTimerMillis = 1000;

        ExecutorService queryExecutor = createExecutorService("queries", 2, 1000, 1, MINUTES);
        ExecutorService resultExecutor = createExecutorService("results", 2, 1000, 1, MINUTES);
        ScheduledExecutorService queryTimer = createScheduledExecutorService("queryTimer");

        Configuration configuration = loadConfiguration(clock);

        return new NaiveScheduler(
                queryExecutor,
                resultExecutor,
                queryTimer,
                new QueryGenerator(
                        clock,
                        configuration.getQueryPeriod(),
                        configuration.getQueries(),
                        new QueryProcessor(
                                clock,
                                ManagementFactory.getPlatformMBeanServer(),
                                configuration.getOutputWriters(),
                                queryExecutor,
                                new ResultProcessor(
                                        clock,
                                        resultExecutor
                                )
                        ),
                        queryTimer
                ),
                shutdownTimerMillis
        );
    }

    private Configuration loadConfiguration(Clock clock) throws JAXBException, ParserConfigurationException, SAXException, IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        ConfigParser configParser = XmlConfigParser.newInstance(
                new PropertyPlaceholderResolverXmlPreprocessor(new PropertyPlaceholderResolver()),
                clock);
        configParser.setSource(new Resource(configFile));

        return configParser.parseConfiguration();
    }

    @Nonnull
    private ScheduledExecutorService createScheduledExecutorService(@Nonnull String componentName) {
        return new ScheduledThreadPoolExecutor(1, new JmxTransThreadFactory(componentName), new AbortPolicy());
    }

    @Nonnull
    private ExecutorService createExecutorService(
            @Nonnull String componentName,
            int maxThreads,
            int maxQueueSize,
            int keepAliveTime,
            @Nonnull TimeUnit unit) {
        return new ThreadPoolExecutor(
                1, maxThreads,
                keepAliveTime, unit,
                new ArrayBlockingQueue<Runnable>(maxQueueSize),
                new JmxTransThreadFactory(componentName),
                new AbortPolicy());
    }
}
