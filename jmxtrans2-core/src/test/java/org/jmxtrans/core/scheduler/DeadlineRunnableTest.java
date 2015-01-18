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

import org.jmxtrans.core.scheduler.DeadlineRunnable;
import org.jmxtrans.utils.time.Clock;
import org.jmxtrans.utils.time.ManualClock;
import org.junit.Test;

import javax.annotation.Nonnull;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public class DeadlineRunnableTest {

    @Test
    public void jobIsRunBeforeDeadline() {
        ManualClock clock = new ManualClock();
        long deadline = clock.currentTimeMillis() + 1000;
        DummyJob job = new DummyJob(clock, deadline);
        job.run();
        assertThat(job.hasRun).isTrue();
    }

    @Test
    public void jodIsNotRunAfterDeadline() {
        ManualClock clock = new ManualClock();
        long deadline = clock.currentTimeMillis() + 1000;
        DummyJob job = new DummyJob(clock, deadline);
        clock.waitFor(2, SECONDS);
        job.run();
        assertThat(job.hasRun).isFalse();
    }

    private static final class DummyJob extends DeadlineRunnable {
        private boolean hasRun = false;
        public DummyJob(@Nonnull Clock clock, long deadline) {
            super(clock, deadline);
        }
        @Override
        protected void doRun() {
            hasRun = true;
        }
    }
}
