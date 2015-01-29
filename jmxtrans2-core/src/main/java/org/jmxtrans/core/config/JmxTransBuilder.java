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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.jmxtrans.core.log.Logger;
import org.jmxtrans.core.log.LoggerFactory;
import org.jmxtrans.core.query.embedded.ResultNameStrategy;
import org.jmxtrans.core.scheduler.JmxTransThreadFactory;
import org.jmxtrans.core.scheduler.NaiveScheduler;
import org.jmxtrans.core.scheduler.QueryGenerator;
import org.jmxtrans.core.scheduler.QueryProcessor;
import org.jmxtrans.core.scheduler.ResultProcessor;
import org.jmxtrans.utils.PropertyPlaceholderResolver;
import org.jmxtrans.utils.io.Resource;
import org.jmxtrans.utils.time.Clock;
import org.jmxtrans.utils.time.SystemClock;

import org.xml.sax.SAXException;

import static java.util.Collections.singleton;
import static java.util.concurrent.TimeUnit.MINUTES;

@ThreadSafe
public class JmxTransBuilder {

    private final boolean ignoreParsingErrors;
    @Nonnull private final Iterable<Resource> configResources;
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public JmxTransBuilder(boolean ignoreParsingErrors, @Nonnull Iterable<Resource> configResources) {
        this.ignoreParsingErrors = ignoreParsingErrors;
        this.configResources = configResources;
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
                        configuration.getPeriod(),
                        configuration.getServers(),
                        new QueryProcessor(
                                clock,
                                configuration.getOutputWriters(),
                                queryExecutor,
                                new ResultProcessor(
                                        clock,
                                        resultExecutor
                                ),
                                new ResultNameStrategy()
                        ),
                        queryTimer
                ),
                shutdownTimerMillis
        );
    }

    private Configuration loadConfiguration(Clock clock) throws JAXBException, ParserConfigurationException, SAXException, IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        Iterable<ConfigParser> parsers = getConfigParsers(clock);

        Collection<Configuration> configurations = new ArrayList<>();

        for (Resource configResource : configResources) {
            boolean parsed = false;
            for (ConfigParser parser : parsers) {
                if (parser.supports(configResource)) {
                    try {
                        Configuration configuration = parser.parseConfiguration(configResource);
                        configurations.add(configuration);
                        parsed = true;
                    } catch (Exception e) {
                        String message = "Could not parse configuration " + configResource.getPath();
                        if (ignoreParsingErrors) {
                            logger.info(message);
                        } else {
                            throw new JmxtransConfigurationException(message, e);
                        }
                    }
                }
            }
            if (!parsed && !ignoreParsingErrors) {
                throw new JmxtransConfigurationException("Found no parsers supporting config file " + configResource.getPath());
            }
        }

        return new ConfigurationMerger().merge(configurations);
    }


    private Iterable<ConfigParser> getConfigParsers(Clock clock) throws JAXBException, ParserConfigurationException, SAXException, IOException {
        ConfigParser configParser = XmlConfigParser.newInstance(
                new PropertyPlaceholderResolverXmlPreprocessor(new PropertyPlaceholderResolver()),
                clock);
        return singleton(configParser);
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
