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
package org.jmxtrans.core.scheduler;

import org.jmxtrans.log.Logger;
import org.jmxtrans.log.LoggerFactory;
import org.jmxtrans.core.query.embedded.Query;
import org.jmxtrans.core.query.embedded.Server;
import org.jmxtrans.utils.time.Clock;
import org.jmxtrans.utils.time.Interval;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@ThreadSafe
public class QueryGenerator implements Runnable {
    @Nonnull private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Nonnull private final Clock clock;
    @Nonnull private final Interval queryPeriod;
    @Nonnull private final Iterable<Server> servers;
    @Nonnull private final QueryProcessor queryProcessor;
    @Nonnull private final ScheduledExecutorService queryTimer;
    private volatile boolean running = false;

    public QueryGenerator(
            @Nonnull Clock clock,
            @Nonnull Interval queryPeriod,
            @Nonnull Iterable<Server> servers,
            @Nonnull QueryProcessor queryProcessor,
            @Nonnull ScheduledExecutorService queryTimer) {
        this.clock = clock;
        this.queryPeriod = queryPeriod;
        this.servers = servers;
        this.queryProcessor = queryProcessor;
        this.queryTimer = queryTimer;
    }

    @Override
    public void run() {
            long deadline = clock.currentTimeMillis() + queryPeriod.getDuration(MILLISECONDS);
        for (Server server : servers) {
            for (final Query query : server.getQueries()) {
                try {
                    logger.debug("Enquing query " + query);
                    queryProcessor.process(deadline, server, query);
                } catch (RejectedExecutionException e) {
                    logger.warn("Could not enqueue query " + query, e);
                }
            }
        }

        if (running) {
            logger.debug("Scheduling next run in " + queryPeriod.getDuration(SECONDS) + " seconds.");
            try {
                queryTimer.schedule(
                        this,
                        queryPeriod.getDuration(MILLISECONDS),
                        MILLISECONDS);
            } catch (RejectedExecutionException e) {
                logger.error("Could not schedule next task");
            }
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
