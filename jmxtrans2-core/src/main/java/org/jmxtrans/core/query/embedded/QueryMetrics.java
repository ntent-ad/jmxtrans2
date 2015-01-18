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
package org.jmxtrans.core.query.embedded;

import org.jmxtrans.utils.time.Clock;
import org.jmxtrans.utils.time.NanoChronometer;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class QueryMetrics {

    @Nonnull
    private final AtomicInteger collectedMetricsCount = new AtomicInteger();

    @Nonnull
    private final AtomicLong collectionDurationInNanos = new AtomicLong();

    @Nonnull
    private final AtomicInteger collectionCount = new AtomicInteger();

    @Nonnull
    private final Clock clock;

    public QueryMetrics(@Nonnull Clock clock) {
        this.clock = clock;
    }

    public int incrementCollected(int count) {
        return collectedMetricsCount.addAndGet(count);
    }

    @Nonnull
    public NanoChronometer collectionDurationChronometer() {
        return new NanoChronometer(collectionDurationInNanos, clock);
    }

    public int incrementCollectionsCount() {
        return collectionCount.incrementAndGet();
    }

    public int getCollectedCount() {
        return collectedMetricsCount.get();
    }

    public long getCollectionDurationNano() {
        return collectionDurationInNanos.get();
    }

    public int getCollectionsCount() {
        return collectionCount.get();
    }
}
