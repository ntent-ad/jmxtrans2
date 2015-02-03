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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.jmxtrans.core.scheduler.JmxTransThreadFactory;

import org.junit.Test;

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.assertj.core.api.Assertions.assertThat;

public class ThreadPoolExecutorMetricsTest {

    /**
     * Fairly stupid test to make sure delegation works.
     * 
     * This test does not have much value as a test, but it works well as documentation of the default values for a
     * ThreadPoolExecutor.
     */
    @Test
    public void dummyCheckOfDelegation() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 10,
                120, SECONDS,
                new ArrayBlockingQueue<Runnable>(20),
                new JmxTransThreadFactory("test-executor-thread"),
                new ThreadPoolExecutor.AbortPolicy());

        ThreadPoolExecutorMetrics executorMetrics = new ThreadPoolExecutorMetrics(executor);

        assertThat(executorMetrics.getWorkQueueSize()).isEqualTo(0);
        assertThat(executorMetrics.getWorkQueueRemainingCapacity()).isEqualTo(20);

        assertThat(executorMetrics.getActiveCount()).isZero();
        assertThat(executorMetrics.getCompletedTaskCount()).isZero();
        assertThat(executorMetrics.getCorePoolSize()).isEqualTo(1);
        assertThat(executorMetrics.getLargestPoolSize()).isZero();
        assertThat(executorMetrics.getMaximumPoolSize()).isEqualTo(10);
        assertThat(executorMetrics.getPoolSize()).isZero();
        assertThat(executorMetrics.getTaskCount()).isZero();
    }
    
}
