package org.jmxtrans.query.embedded;

import org.jmxtrans.utils.NanoChronometer;

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

    public int incrementCollected(int count) {
        return collectedMetricsCount.addAndGet(count);
    }

    public NanoChronometer collectionDurationChronometer() {
        return new NanoChronometer(collectionDurationInNanos);
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
