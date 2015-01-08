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

import org.jmxtrans.query.embedded.Query;
import org.jmxtrans.utils.time.Clock;
import org.jmxtrans.utils.time.Interval;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@ThreadSafe
public class QueryGenerator implements Runnable {
    @Nonnull
    private final Clock clock;
    @Nonnull
    private final Interval queryPeriod;
    @Nonnull
    private final Iterable<Query> queries;
    @Nonnull
    private final QueryProcessor queryProcessor;
    @Nonnull
    private final ScheduledExecutorService queryTimer;
    private volatile boolean running = false;

    public QueryGenerator(
            @Nonnull Clock clock,
            @Nonnull Interval queryPeriod,
            @Nonnull Iterable<Query> queries,
            @Nonnull QueryProcessor queryProcessor, ScheduledExecutorService queryTimer) {
        this.clock = clock;
        this.queryPeriod = queryPeriod;
        this.queries = queries;
        this.queryProcessor = queryProcessor;
        this.queryTimer = queryTimer;
    }

    @Override
    public void run() {
        long startTimeMillis = clock.currentTimeMillis();
        long deadline = startTimeMillis + queryPeriod.getDuration(MILLISECONDS);
        for (final Query query : queries) {
            try {
                queryProcessor.process(deadline, query);
            } catch (RejectedExecutionException e) {
                // todo
            }
        }

        if (running) {
            queryTimer.schedule(
                    this,
                    startTimeMillis + queryPeriod.getDuration(MILLISECONDS),
                    MILLISECONDS);
        }
    }

    public void start() {
        running = true;
        run();
    }

    public void stop() {
        running = false;
    }

}
