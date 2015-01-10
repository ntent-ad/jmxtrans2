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

import org.jmxtrans.config.Configuration;
import org.jmxtrans.log.LoggerFactory;
import org.jmxtrans.output.OutputWriter;
import org.jmxtrans.query.Invocation;
import org.jmxtrans.query.embedded.Query;
import org.jmxtrans.results.QueryResult;
import org.jmxtrans.utils.concurrent.DiscardingBlockingQueue;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class JmxTransExporter {

    private final Configuration configuration;
    private org.jmxtrans.log.Logger logger = LoggerFactory.getLogger(getClass().getName());
    private ThreadFactory threadFactory = new ThreadFactory() {
        final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setDaemon(true);
            thread.setName("jmxtrans-agent-" + counter.incrementAndGet());
            return thread;
        }
    };
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1, threadFactory);
    private MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
    private ScheduledFuture scheduledFuture;

    public JmxTransExporter(Configuration configuration) {
        this.configuration = configuration;
    }

    public void start() {
        if (logger.isDebugEnabled()) {
            logger.debug("starting " + this.toString() + " ...");
        }

        if (scheduledFuture != null) {
            throw new IllegalArgumentException("Exporter is already started");
        }

        scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                collectAndExport();
            }
        }, configuration.getQueryPeriod().getValue() / 2,
                configuration.getQueryPeriod().getValue(),
                configuration.getQueryPeriod().getTimeUnit());

        logger.debug(getClass().getName() + " started");
    }

    public void stop() {
        // cancel jobs
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        scheduledExecutorService.shutdown();

        // one last export
        collectAndExport();

        // wait for stop
        try {
            scheduledExecutorService.awaitTermination(
                    configuration.getQueryPeriod().getValue(),
                    configuration.getQueryPeriod().getTimeUnit());
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        logger.info(getClass().getName() + " stopped.");

    }

    protected void collectAndExport() {
        for (Invocation invocation : configuration.getInvocations()) {
            try {
                BlockingQueue<QueryResult> results = new DiscardingBlockingQueue<>(100);
                invocation.invoke(mbeanServer, results);
                writeResults(results);
            } catch (Exception e) {
                logger.warn("Ignore exception invoking " + invocation, e);
            }
        }
        for (Query query : configuration.getQueries()) {
            try {
                BlockingQueue<QueryResult> results = new DiscardingBlockingQueue<>(100);
                query.collectMetrics(mbeanServer, results);
                writeResults(results);
            } catch (Exception e) {
                logger.warn("Ignore exception collecting metrics for " + query, e);
            }
        }
    }

    private void writeResults(BlockingQueue<QueryResult> results) {
        for (OutputWriter outputWriter : configuration.getOutputWriters()) {
            try {
                outputWriter.write(results);
            } catch (Exception e) {
                logger.warn(format("Could not write results to output writer [%s].", outputWriter), e);
            }
        }
    }

    @Override
    public String toString() {
        return "JmxTransExporter{}";
    }
}
