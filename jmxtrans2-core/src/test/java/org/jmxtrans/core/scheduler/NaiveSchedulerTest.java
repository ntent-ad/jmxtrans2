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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.jmxtrans.core.lifecycle.LifecycleAware;
import org.jmxtrans.utils.mockito.MockitoTestNGListener;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static java.util.Collections.singletonList;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Listeners(MockitoTestNGListener.class)
public class NaiveSchedulerTest {

    private NaiveScheduler scheduler;
    private ExecutorService queryExecutor;
    private ExecutorService resultExecutor;
    private ScheduledExecutorService queryTimer;
    @Mock private LifecycleAware lifecycleListener;

    @BeforeMethod
    public void createScheduler() {
        queryExecutor = newSingleThreadExecutor();
        resultExecutor = newSingleThreadExecutor();
        queryTimer = newSingleThreadScheduledExecutor();
        QueryGenerator queryGenerator = Mockito.mock(QueryGenerator.class);
        scheduler = new NaiveScheduler(queryExecutor, resultExecutor, queryTimer, queryGenerator, singletonList(lifecycleListener), 1);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void cannotStopServiceIfNotRunning() throws Exception {
        scheduler.stop();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void cannotStartServiceTwice() throws Exception {
        scheduler.start();
        scheduler.start();
    }

    @Test
    public void nothingIsRunningAfterStop() throws Exception {
        scheduler.start();
        scheduler.stop();
        assertThat(queryExecutor.isShutdown()).isTrue();
        assertThat(resultExecutor.isShutdown()).isTrue();
        assertThat(queryTimer.isShutdown()).isTrue();
    }

    @Test
    public void lifecycleListenersAreNotifiedAtStartup() throws Exception {
        scheduler.start();
        verify(lifecycleListener).start();
        verify(lifecycleListener, never()).stop();
    }

    @Test
    public void lifecycleListenersAreNotifiedAtShutdown() throws Exception {
        scheduler.start();
        scheduler.stop();
        verify(lifecycleListener).stop();
    }

    @AfterMethod
    public void stopScheduler() throws Exception {
        try {
            scheduler.stop();
        } catch (InterruptedException|IllegalStateException ignore) {
        }
    }

}
