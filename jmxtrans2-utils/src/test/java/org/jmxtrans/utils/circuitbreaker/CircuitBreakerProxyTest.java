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
package org.jmxtrans.utils.circuitbreaker;

import org.jmxtrans.utils.time.ManualClock;
import org.junit.Before;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.jmxtrans.utils.circuitbreaker.CircuitBreakerProxy.create;

public class CircuitBreakerProxyTest {

    private final ManualClock clock = new ManualClock();

    @Before
    public void setTime() {
        clock.setTime(100, SECONDS);
    }

    @Test
    public void operationsAreCalledOnProxiedInstance() {
        Counter counter = create(clock, Counter.class, new ExceptionThrowingCounter(), 10, 1000);

        assertThat(counter.getCurrentValue()).isEqualTo(0);
        assertThat(counter.incrementAndGet()).isEqualTo(1);
    }

    @Test
    public void circuitIsNotOpenedForSingleException() {
        ExceptionThrowingCounter target = new ExceptionThrowingCounter();
        Counter counter = create(clock, Counter.class, target, 10, 1000);

        assertThat(counter.getCurrentValue()).isEqualTo(0);
        incrementIgnoringExceptions(counter, target);
        assertThat(counter.getCurrentValue()).isEqualTo(0);
    }

    @Test(expected = CircuitBreakerOpenException.class)
    public void circuitBreakerIsOpenedForMultipleExceptions() {
        ExceptionThrowingCounter target = new ExceptionThrowingCounter();
        Counter counter = create(clock, Counter.class, target, 2, 1000);

        assertThat(counter.getCurrentValue()).isEqualTo(0);
        incrementIgnoringExceptions(counter, target);
        incrementIgnoringExceptions(counter, target);

        try {
            counter.incrementAndGet();
        } catch (CircuitBreakerOpenException e) {
            assertThat(e.getDisabledUntil()).isEqualTo(101000);
            assertThat(e).hasMessageContaining("disabledUntil=101000");
            throw e;
        }
    }

    @Test
    public void circuitBreakerIsClosedAgainAfterDisableDuration() {
        ExceptionThrowingCounter target = new ExceptionThrowingCounter();
        Counter counter = create(clock, Counter.class, target, 2, 1000);

        assertThat(counter.getCurrentValue()).isEqualTo(0);
        incrementIgnoringExceptions(counter, target);
        incrementIgnoringExceptions(counter, target);

        clock.waitFor(2, SECONDS);

        counter.incrementAndGet();
    }

    private void incrementIgnoringExceptions(Counter counter, ExceptionThrowingCounter target) {
        try {
            target.throwExceptionOnNextCall();
            counter.incrementAndGet();
            fail("RuntimeException should have been thrown");
        } catch (IllegalStateException ignore) {
        }
    }

    private static interface Counter {
        int incrementAndGet();
        int getCurrentValue();
    }

    private static final class ExceptionThrowingCounter implements Counter {
        private int value = 0;
        private boolean throwExceptionOnNextCall = false;
        @Override
        public int incrementAndGet() {
            throwAsNeeded();
            return ++value;
        }

        @Override
        public int getCurrentValue() {
            throwAsNeeded();
            return value;
        }

        public void throwExceptionOnNextCall() {
            throwExceptionOnNextCall = true;
        }

        private void throwAsNeeded() {
            if (throwExceptionOnNextCall) {
                throwExceptionOnNextCall = false;
                throw new IllegalStateException();
            }
        }
    }

}
