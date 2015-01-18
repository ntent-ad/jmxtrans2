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

import org.jmxtrans.core.query.embedded.Query;
import org.jmxtrans.core.query.embedded.Server;
import org.jmxtrans.core.scheduler.QueryGenerator;
import org.jmxtrans.core.scheduler.QueryProcessor;
import org.jmxtrans.utils.time.Interval;
import org.jmxtrans.utils.time.ManualClock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueryGeneratorTest {

    private ManualClock clock = new ManualClock();
    private Interval queryPeriod = new Interval(10, SECONDS);
    @Mock private Server server;
    @Mock private Query query;
    @Mock private QueryProcessor queryProcessor;
    @Mock private ScheduledExecutorService queryTimer;
    private QueryGenerator queryGenerator;

    @Before
    public void createQueryGenerator() {
        clock.setTime(1, SECONDS);
        when(server.getQueries()).thenReturn(singleton(query));
        queryGenerator = new QueryGenerator(clock, queryPeriod, singletonList(server), queryProcessor, queryTimer);
    }

    @Test
    public void queryAreEnqueued() {
        queryGenerator.run();
        verify(queryProcessor).process(11000, server, query);
    }

    @Test
    public void nextTaskIsScheduled() {
        queryGenerator.start();
        verify(queryTimer).schedule(any(Runnable.class), eq(10000L), eq(MILLISECONDS));
    }

    @Test
    public void nextTaskIsNotScheduledIfGeneratorIsStopped() {
        queryGenerator.stop();
        queryGenerator.run();
        verify(queryTimer, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

}
