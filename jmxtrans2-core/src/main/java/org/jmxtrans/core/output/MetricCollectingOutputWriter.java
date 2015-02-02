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
package org.jmxtrans.core.output;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jmxtrans.core.monitoring.SelfNamedMBean;
import org.jmxtrans.core.results.QueryResult;
import org.jmxtrans.utils.time.Clock;
import org.jmxtrans.utils.time.NanoChronometer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

@ThreadSafe
public class MetricCollectingOutputWriter implements OutputWriter, MetricCollectingOutputWriterMBean, SelfNamedMBean {

    @Nonnull private final Clock clock;
    @Nonnull private final OutputWriter delegate;
    @Nonnull private final AtomicInteger processedCount = new AtomicInteger();
    @Nonnull private final ObjectName objectName;
    @Nonnull private final AtomicLong processingTimeCounter = new AtomicLong();

    public MetricCollectingOutputWriter(@Nonnull Clock clock, @Nonnull OutputWriter delegate, @Nonnull ObjectName objectName) {
        this.clock = clock;
        this.delegate = delegate;
        this.objectName = objectName;
    }
    
    @Override
    public int write(@Nonnull QueryResult result) throws IOException {
        try (NanoChronometer chronometer = getProcessingTimeChronometer()) {
            int count = delegate.write(result);
            processedCount.addAndGet(count);
            return count;
        }
    }

    @Nonnull
    private NanoChronometer getProcessingTimeChronometer() {
        return new NanoChronometer(processingTimeCounter, clock);
    }

    @Override
    public int getProcessedResultsCount() {
        return processedCount.get();
    }

    @Override
    public long getProcessingTimeMillis() {
        return MILLISECONDS.convert(processingTimeCounter.get(), NANOSECONDS);
    }

    @Nonnull
    @Override
    public ObjectName getObjectName() throws MalformedObjectNameException {
        return objectName;
    }
}
