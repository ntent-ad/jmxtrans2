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
package org.jmxtrans.scheduler;

import org.jmxtrans.log.Logger;
import org.jmxtrans.log.LoggerFactory;
import org.jmxtrans.output.OutputWriter;
import org.jmxtrans.query.embedded.Query;
import org.jmxtrans.results.QueryResult;
import org.jmxtrans.utils.time.Clock;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.management.MBeanServer;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

public class QueryProcessor {

    @Nonnull private final Clock clock;
    @Nonnull private final MBeanServer mBeanServer;
    @Nonnull private final Iterable<OutputWriter> outputWriters;
    @Nonnull private final Executor queryExecutor;
    @Nonnull private final ResultProcessor resultProcessor;

    public QueryProcessor(
            @Nonnull Clock clock,
            @Nonnull MBeanServer mBeanServer,
            @Nonnull Iterable<OutputWriter> outputWriters,
            @Nonnull Executor queryExecutor,
            @Nonnull ResultProcessor resultProcessor) {
        this.clock = clock;
        this.mBeanServer = mBeanServer;
        this.outputWriters = outputWriters;
        this.queryExecutor = queryExecutor;
        this.resultProcessor = resultProcessor;
    }

    @Nonnull
    public void process(long deadline, @Nonnull Query query) {
        queryExecutor.execute(new Processor(clock, deadline, mBeanServer, query, outputWriters, resultProcessor));
    }

    @ThreadSafe
    private static class Processor extends DeadlineRunnable {
        @Nonnull private final Logger logger = LoggerFactory.getLogger(getClass().getName());
        @Nonnull private final Query query;
        @Nonnull private final MBeanServer mBeanServer;
        @Nonnull private final Iterable<OutputWriter> outputWriters;
        @Nonnull private final ResultProcessor resultProcessor;

        public Processor(
                @Nonnull Clock clock,
                long deadline,
                @Nonnull MBeanServer mBeanServer,
                @Nonnull Query query,
                @Nonnull Iterable<OutputWriter> outputWriters,
                @Nonnull ResultProcessor resultProcessor) {
            super(clock, deadline);
            this.query = query;
            this.mBeanServer = mBeanServer;
            this.outputWriters = outputWriters;
            this.resultProcessor = resultProcessor;
        }

        @Override
        protected void doRun() {
            try {
                logger.debug("Collecting metrics for " + query);
                Iterable<QueryResult> results = query.collectMetrics(mBeanServer);
                for (OutputWriter outputWriter : outputWriters) {
                    try {
                        resultProcessor.writeResults(getDeadline(), results, outputWriter);
                    } catch (RejectedExecutionException e) {
                        logger.warn("Could not enqueue result to writers.", e);
                    }
                }
            } catch (Exception e) {
                // TODO
            }
        }
    }
}
