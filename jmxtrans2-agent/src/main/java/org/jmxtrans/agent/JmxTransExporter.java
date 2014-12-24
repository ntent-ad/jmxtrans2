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

import org.jmxtrans.config.Interval;
import org.jmxtrans.config.Invocation;
import org.jmxtrans.output.OutputWriter;
import org.jmxtrans.query.Query;
import org.jmxtrans.query.ResultNameStrategy;
import org.jmxtrans.output.DevNullOutputWriter;
import org.jmxtrans.results.QueryResult;

import javax.annotation.Nonnull;
import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class JmxTransExporter {
    /**
     * visible for test
     */
    protected List<Query> queries = new ArrayList<Query>();
    /**
     * visible for test
     */
    protected List<Invocation> invocations = new ArrayList<Invocation>();
    /**
     * visible for test
     */
    protected OutputWriter outputWriter = new DevNullOutputWriter();

    private ResultNameStrategy resultNameStrategy;
    protected int collectInterval = 10;
    protected TimeUnit collectIntervalTimeUnit = TimeUnit.SECONDS;
    private Logger logger = Logger.getLogger(getClass().getName());
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

    @Nonnull
    public JmxTransExporter withQueries(@Nonnull Collection<Query> queries) {
        this.queries.addAll(queries);
        return this;
    }

    @Nonnull
    public JmxTransExporter withInvocations(@Nonnull Collection<Invocation> invocations) {
        this.invocations.addAll(invocations);
        return this;
    }

    @Nonnull
    public JmxTransExporter withOutputWriter(@Nonnull OutputWriter outputWriter) {
        this.outputWriter = outputWriter;
        return this;
    }

    @Nonnull
    public JmxTransExporter withResultNameStrategy(@Nonnull ResultNameStrategy resultNameStrategy) {
        this.resultNameStrategy = resultNameStrategy;
        return this;
    }

    @Nonnull
    public JmxTransExporter withCollectInterval(@Nonnull Interval interval) {
        this.collectInterval = interval.getValue();
        this.collectIntervalTimeUnit = interval.getTimeUnit();
        return this;
    }

    public void start() {
        if (logger.isLoggable(Level.FINER)) {
            logger.fine("starting " + this.toString() + " ...");
        } else {
            logger.fine("starting " + getClass().getName() + " ...");
        }

        if (scheduledFuture != null) {
            throw new IllegalArgumentException("Exporter is already started");
        }

        if (resultNameStrategy == null)
            throw new IllegalStateException("resultNameStrategy is not defined, jmxTransExporter is not properly initialised");

        scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                collectAndExport();
            }
        }, collectInterval / 2, collectInterval, collectIntervalTimeUnit);

        logger.fine(getClass().getName() + " started");
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
            scheduledExecutorService.awaitTermination(collectInterval, collectIntervalTimeUnit);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        logger.info(getClass().getName() + " stopped.");

    }

    protected void collectAndExport() {
        try {
            outputWriter.preCollect();
            for (Invocation invocation : invocations) {
                try {
                    invocation.invoke(mbeanServer, outputWriter);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Ignore exception invoking " + invocation, e);
                }
            }
            for (Query query : queries) {
                try {
                    Queue<QueryResult> results = new LinkedList<QueryResult>();
                    query.collectAndExport(mbeanServer, results);
                    outputWriter.write(results);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Ignore exception collecting metrics for " + query, e);
                }
            }
            outputWriter.postCollect();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Ignore exception flushing metrics ", e);
        }
    }

    @Override
    public String toString() {
        return "JmxTransExporter{" +
                "queries=" + queries +
                ", invocations=" + invocations +
                ", outputWriter=" + outputWriter +
                ", collectInterval=" + collectInterval +
                " " + collectIntervalTimeUnit +
                '}';
    }
}
