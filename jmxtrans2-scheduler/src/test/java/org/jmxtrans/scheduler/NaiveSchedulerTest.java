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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.assertj.core.api.Assertions.assertThat;

public class NaiveSchedulerTest {

    private NaiveScheduler scheduler;
    private ExecutorService queryExecutor;
    private ExecutorService resultExecutor;
    private ScheduledExecutorService queryTimer;

    @Before
    public void createScheduler() {
        queryExecutor = newSingleThreadExecutor();
        resultExecutor = newSingleThreadExecutor();
        queryTimer = newSingleThreadScheduledExecutor();
        QueryGenerator queryGenerator = Mockito.mock(QueryGenerator.class);
        scheduler = new NaiveScheduler(queryExecutor, resultExecutor, queryTimer, queryGenerator, 1);
    }

    @Test(expected = IllegalStateException.class)
    public void cannotStopServiceIfNotRunning() throws InterruptedException {
        scheduler.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void cannotStartServiceTwice() throws InterruptedException {
        scheduler.start();
        scheduler.start();
    }

    @Test
    public void nothingIsRunningAfterStop() throws InterruptedException {
        scheduler.start();
        scheduler.stop();
        assertThat(queryExecutor.isShutdown()).isTrue();
        assertThat(resultExecutor.isShutdown()).isTrue();
        assertThat(queryTimer.isShutdown()).isTrue();
    }

    @After
    public void stopScheduler() {
        try {
            scheduler.stop();
        } catch (InterruptedException ignore) {
        } catch (IllegalStateException ignore) {
        }
    }

}
