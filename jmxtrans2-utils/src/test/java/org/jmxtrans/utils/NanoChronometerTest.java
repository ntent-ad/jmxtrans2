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
package org.jmxtrans.utils;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public class NanoChronometerTest {

    @Test
    public void chronometerMeasureIncrementingTime() throws InterruptedException {
        AtomicLong counter = new AtomicLong(0);
        try(NanoChronometer chronometer = new NanoChronometer(counter)) {
            Thread.sleep(1);
        }
        assertThat(counter.get()).isGreaterThan(0);
    }

    @Test
    public void chronometerMeasuresMoreOrLessTheCorrectTime() throws InterruptedException {
        AtomicLong counter = new AtomicLong(0);
        long sleepTimeInMillis = 1;
        long sleepTimeInNanos = NANOSECONDS.convert(sleepTimeInMillis, MILLISECONDS);
        long precision = NANOSECONDS.convert(100, MICROSECONDS); // yes, nano time is not very precise and overhead is significant
        try(NanoChronometer chronometer = new NanoChronometer(counter)) {
            Thread.sleep(sleepTimeInMillis);
        }
        assertThat(counter.get()).isBetween(sleepTimeInNanos - precision, sleepTimeInNanos + precision);
    }
}
