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
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.jmxtrans.core.lifecycle.LifecycleAware;
import org.jmxtrans.core.log.Logger;
import org.jmxtrans.core.log.LoggerFactory;
import org.jmxtrans.core.monitoring.MBeanRegistry;
import org.jmxtrans.core.monitoring.SelfNamedMBean;
import org.jmxtrans.core.query.ResultNameStrategy;
import org.jmxtrans.core.query.Server;
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

import static java.lang.String.format;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
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

    public NaiveScheduler build() throws ParserConfigurationException, IOException, SAXException, JAXBException, IllegalAccessException, InstantiationException, ClassNotFoundException, MalformedObjectNameException {
        SystemClock clock = new SystemClock();
        long shutdownTimerMillis = 1000;

        MBeanRegistry mBeanRegistry = new MBeanRegistry(getPlatformMBeanServer());

        ExecutorService queryExecutor = createExecutorService("queries", 2, 1000, 1, MINUTES, mBeanRegistry);
        ExecutorService resultExecutor = createExecutorService("results", 2, 1000, 1, MINUTES, mBeanRegistry);
        ScheduledExecutorService queryTimer = createScheduledExecutorService("queryTimer", mBeanRegistry);

        Configuration configuration = loadConfiguration(clock);
        
        registerMBeans(configuration, mBeanRegistry);

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
                Collections.<LifecycleAware>singletonList(mBeanRegistry),
                shutdownTimerMillis
        );
    }

    private void registerMBeans(Configuration configuration, MBeanRegistry mBeanRegistry) {
        for (Server server : configuration.getServers()) {
            registerMBeans(mBeanRegistry, server.getQueries());
            registerMBeans(mBeanRegistry, configuration.getOutputWriters());
        }
    }

    private void registerMBeans(MBeanRegistry mBeanRegistry, Iterable<?> objects) {
        for (Object object : objects) {
            
            if (!(object instanceof SelfNamedMBean)) break;
            
            SelfNamedMBean selfNamedMBean = (SelfNamedMBean)object;
            try {
                mBeanRegistry.register(selfNamedMBean);
            } catch (MalformedObjectNameException e) {
                logger.warn(format("Could not register bean [%s]", selfNamedMBean), e);
            }
        }
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
    private ScheduledExecutorService createScheduledExecutorService(
            @Nonnull String componentName,
            @Nonnull MBeanRegistry mBeanRegistry) throws MalformedObjectNameException {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new JmxTransThreadFactory(componentName), new AbortPolicy());
        mBeanRegistry.register(
                new ObjectName("org.jmxtrans.executors:type=ExecutorMetrics,name=" + componentName),
                new ThreadPoolExecutorMetrics(executor));
        return executor;
    }

    @Nonnull
    private ExecutorService createExecutorService(
            @Nonnull String componentName,
            int maxThreads,
            int maxQueueSize,
            int keepAliveTime,
            @Nonnull TimeUnit unit,
            @Nonnull MBeanRegistry mBeanRegistry) throws MalformedObjectNameException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, maxThreads,
                keepAliveTime, unit,
                new ArrayBlockingQueue<Runnable>(maxQueueSize),
                new JmxTransThreadFactory(componentName),
                new AbortPolicy());
        mBeanRegistry.register(
                new ObjectName("org.jmxtrans.executors:type=ExecutorMetrics,name=" + componentName),
                new ThreadPoolExecutorMetrics(executor));
        return executor;
    }
}
