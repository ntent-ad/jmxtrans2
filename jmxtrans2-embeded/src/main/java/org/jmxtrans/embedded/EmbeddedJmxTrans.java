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
package org.jmxtrans.embedded;

import org.jmxtrans.query.embedded.Query;
import org.jmxtrans.output.OutputWriter;
import org.jmxtrans.results.QueryResult;
import org.jmxtrans.utils.concurrent.DiscardingBlockingQueue;
import org.jmxtrans.utils.concurrent.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.MBeanServer;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p/>
 * <strong>JMX Queries</strong>
 * <p/>
 * If the JMX query returns several mbeans (thanks to '*' or '?' wildcards),
 * then the configured attributes are collected on all the returned mbeans.
 * <p/>
 * The drawback is to limit the benefits of batching result
 * to a backend and the size limit of the results list to prevent
 * {@linkplain OutOfMemoryError} in case of export slowness.
 * <p/>
 * An optimization would be, if only one {@linkplain org.jmxtrans.output.OutputWriter} is defined in the whole {@linkplain org.jmxtrans.embedded.EmbeddedJmxTrans}, to
 * replace all the query-local result queues by one global result-queue.
 *
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 * @author Jon Stevens
 */
public class EmbeddedJmxTrans implements EmbeddedJmxTransMBean {

	public EmbeddedJmxTrans() {
		super();
	}
	
    public EmbeddedJmxTrans(MBeanServer mbeanServer) {
		super();
		this.mbeanServer = mbeanServer;
	}

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean running = false;

    private ScheduledExecutorService collectScheduledExecutor;

    private ScheduledExecutorService exportScheduledExecutor;

    @Nonnull
    private MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

    @Nonnull
    private final List<Query> queries = new ArrayList<Query>();

    /**
     * Use to {@linkplain java.util.Set} to deduplicate during configuration merger
     */
    private Set<OutputWriter> outputWriters = new HashSet<OutputWriter>();

    private int numQueryThreads = 1;

    private int numExportThreads = 1;

    private int queryIntervalInSeconds = 30;

    private int exportIntervalInSeconds = 5;

    private int exportBatchSize = 50;

    /**
     * Start the exporter: initialize underlying queries, start scheduled executors, register shutdown hook
     */
    @PostConstruct
    public synchronized void start() throws Exception {
        if(running) {
            logger.debug("Ignore start() command for already running instance");
            return;
        }

        for (Query query : queries) {
            query.start();
        }

        collectScheduledExecutor = Executors.newScheduledThreadPool(getNumQueryThreads(), new NamedThreadFactory("org.jmxtrans-collect-", true));
        exportScheduledExecutor = Executors.newScheduledThreadPool(getNumExportThreads(), new NamedThreadFactory("org.jmxtrans-export-", true));

        for (final Query query : getQueries()) {
            final BlockingQueue<QueryResult> results = new DiscardingBlockingQueue<QueryResult>(200);
            collectScheduledExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    query.collectMetrics(mbeanServer, results);
                }
            }, 0, getQueryIntervalInSeconds(), TimeUnit.SECONDS);
            // start export just after first collect
            exportScheduledExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    exportMetrics(results);
                }
            }, getQueryIntervalInSeconds() + 1, getExportIntervalInSeconds(), TimeUnit.SECONDS);
        }

        running = true;
        logger.info("EmbeddedJmxTrans started");
    }

    private int exportMetrics(BlockingQueue<QueryResult> queryResults) {
        if(queryResults.isEmpty()) {
            return 0;
        }

        int totalExportedMetricsCount = 0;

        List<QueryResult> availableQueryResults = new ArrayList<QueryResult>(exportBatchSize);

        int size;
        while ((size = queryResults.drainTo(availableQueryResults, exportBatchSize)) > 0) {
            totalExportedMetricsCount += size;
            for (OutputWriter outputWriter : outputWriters) {
                try {
                    outputWriter.write(availableQueryResults);
                } catch (IOException ioe) {
                    logger.error("Could not send metrics to output writer {} for query {}.", outputWriter, this, ioe);
                }
            }
            availableQueryResults.clear();
        }
        return totalExportedMetricsCount;
    }

    /**
     * Stop scheduled executors and collect-and-export metrics one last time.
     */
    @PreDestroy
    public synchronized void stop() throws Exception {
        if(!running) {
            logger.debug("Ignore stop() command for not running instance");
            return;
        }
        collectScheduledExecutor.shutdown();
        try {
            collectScheduledExecutor.awaitTermination(getQueryIntervalInSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Ignore InterruptedException stopping", e);
        }
        exportScheduledExecutor.shutdown();
        try {
            exportScheduledExecutor.awaitTermination(getExportIntervalInSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Ignore InterruptedException stopping", e);
        }

        for (Query query : queries) {
            query.stop();
        }
        logger.info("EmbeddedJmxTrans stopped. Metrics have been collected and exported one last time.");
        running = false;
    }

    @Nonnull
    public List<Query> getQueries() {
        return queries;
    }

    public void addQuery(@Nonnull Query query) {
        query.setMbeanServer(this.mbeanServer);
        this.queries.add(query);
    }

    @Override
    public String toString() {
        return "EmbeddedJmxTrans{" +
                " queries=" + queries +
                ", outputWriters=" + outputWriters +
                ", numQueryThreads=" + numQueryThreads +
                ", queryIntervalInSeconds=" + queryIntervalInSeconds +
                ", numExportThreads=" + numExportThreads +
                ", exportIntervalInSeconds=" + exportIntervalInSeconds +
                ", exportBatchSize=" + exportBatchSize +
                '}';
    }

    public int getNumQueryThreads() {
        return numQueryThreads;
    }

    public void setNumQueryThreads(int numQueryThreads) {
        this.numQueryThreads = numQueryThreads;
    }

    @Override
    public int getQueryIntervalInSeconds() {
        return queryIntervalInSeconds;
    }

    public void setQueryIntervalInSeconds(int queryIntervalInSeconds) {
        this.queryIntervalInSeconds = queryIntervalInSeconds;
    }

    @Override
    public int getExportIntervalInSeconds() {
        return exportIntervalInSeconds;
    }

    public void setExportIntervalInSeconds(int exportIntervalInSeconds) {
        this.exportIntervalInSeconds = exportIntervalInSeconds;
    }

    @Override
    public int getNumExportThreads() {
        return numExportThreads;
    }

    public void setNumExportThreads(int numExportThreads) {
        this.numExportThreads = numExportThreads;
    }

    @Nonnull
    public Set<OutputWriter> getOutputWriters() {
        return outputWriters;
    }

    public void setExportBatchSize(int exportBatchSize) {
        this.exportBatchSize = exportBatchSize;
    }

    @Nonnull
    public MBeanServer getMbeanServer() {
        return mbeanServer;
    }

    @Override
    public int getCollectedMetricsCount() {
        int result = 0;
        for (Query query : queries) {
            result += query.getCollectedMetricsCount();
        }
        return result;
    }

    @Override
    public long getCollectionDurationInNanos() {
        long result = 0;
        for (Query query : queries) {
            result += query.getCollectionDurationInNanos();
        }
        return result;
    }


    @Override
    public long getCollectionDurationInMillis() {
        return TimeUnit.MILLISECONDS.convert(getCollectionDurationInNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public int getCollectionCount() {
        int result = 0;
        for (Query query : queries) {
            result += query.getCollectionCount();
        }
        return result;
    }

    @Override
    public int getExportedMetricsCount() {
        // FIXME: move metrics
        return 0;
    }

    @Override
    public long getExportDurationInNanos() {
        // FIXME: move metrics
        return 0;
    }

    @Override
    public long getExportDurationInMillis() {
        return TimeUnit.MILLISECONDS.convert(getExportDurationInNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public int getExportCount() {
        // FIXME: move metrics
        return 0;
    }

//    public int getDiscardedResultsCount() {
//        int result = 0;
//        for (Query query : queries) {
//            int discardedResultsCount = query.getDiscardedResultsCount();
//            if (discardedResultsCount != -1) {
//                result += discardedResultsCount;
//            }
//        }
//        return result;
//    }
}
