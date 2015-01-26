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
package org.jmxtrans.output.writers;

import org.jmxtrans.core.output.OutputWriter;
import org.jmxtrans.core.results.QueryResult;
import org.jmxtrans.core.results.QueryResultValueComparator;
import org.jmxtrans.log.Logger;
import org.jmxtrans.log.LoggerFactory;
import org.jmxtrans.output.writers.utils.EvictingQueue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
@NotThreadSafe
public class PerMinuteSummarizerOutputWriter implements OutputWriter {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    protected final OutputWriter delegate;
    protected final Map<String, Queue<QueryResult>> previousQueryResultsByMetricName = new HashMap<String, Queue<QueryResult>>();

    public PerMinuteSummarizerOutputWriter(@Nonnull OutputWriter delegate) {
        this.delegate = delegate;
    }

    @Override
    public void write(@Nonnull QueryResult result) throws IOException {

        if ("counter".equals(result.getType())) {

            QueryResult previousResult = getPreviousQueryResult(result);

            storeQueryResult(result);
            QueryResult newCurrentResult = perMinute(result, previousResult);
            logger.debug("Metric " + result.getName() + " is a counter " +
                    "current=" + result + ", " +
                    "previous=" + previousResult + ", " +
                    "newCurrent.value=" + newCurrentResult.getValue());

            delegate.write(newCurrentResult);
        } else {
            logger.debug("Metric " + result.getName() + " is a NOT a counter");
            delegate.write(result);
        }
    }

    protected void storeQueryResult(@Nullable QueryResult currentResult) {
        if (currentResult == null)
            return;

        Queue<QueryResult> queue = previousQueryResultsByMetricName.get(currentResult.getName());
        if (queue == null) {
            queue = new EvictingQueue<>(3);
            previousQueryResultsByMetricName.put(currentResult.getName(), queue);
        }
        queue.add(currentResult);
    }

    @Nullable
    protected QueryResult getPreviousQueryResult(@Nonnull QueryResult currentResult) {
        Queue<QueryResult> queue = previousQueryResultsByMetricName.get(currentResult.getName());
        if (queue == null) {
            return null;
        }

        final long targetTimeInMillis = currentResult.getEpoch(MILLISECONDS) - TimeUnit.MILLISECONDS.convert(60, TimeUnit.SECONDS);
        long closestDistanceToTarget = Long.MAX_VALUE;
        QueryResult closestQueryResultToTarget = null;
        for (QueryResult queryResult : queue) {
            if (new QueryResultValueComparator().compare(queryResult, currentResult) > 0) {
                // skip older result that is greater than current value
                // ever increasing counter must be increasing
            } else {
                long distanceToTarget = Math.abs(queryResult.getEpoch(MILLISECONDS) - targetTimeInMillis);
                if (distanceToTarget < closestDistanceToTarget) {
                    closestQueryResultToTarget = queryResult;
                    closestDistanceToTarget = distanceToTarget;
                }
            }
        }

        return closestQueryResultToTarget;
    }

    @Nonnull
    public QueryResult perMinute(@Nonnull QueryResult currentResult, @Nullable QueryResult previousResult) {

        if (!(currentResult.getValue() instanceof Number)) {
            logger.info("Current value is not a number, cannot calculate derivative " + currentResult);

            return currentResult;
        }

        if (previousResult == null) {
            logger.debug("No previous value found for metric '" + currentResult.getName() + "'");

            return new QueryResult(currentResult.getName(), "gauge", currentResult.getValue(), currentResult.getEpoch(MILLISECONDS));
        }
        if (!(previousResult.getValue() instanceof Number)) {
            logger.info("previous value is not a number, cannot calculate derivative " + previousResult);

            return currentResult;
        }

        BigDecimal durationInMillis = new BigDecimal(currentResult.getEpoch(MILLISECONDS) - previousResult.getEpoch(MILLISECONDS));

        Number currentValue = (Number) currentResult.getValue();
        Number previousValue = (Number) previousResult.getValue();

        if (((Comparable) currentValue).compareTo(previousValue) < 0) {
            logger.debug("Previous value is greater than current value for metric '" + currentResult.getName() + "', ignore it");

            return new QueryResult(currentResult.getName(), "gauge", currentResult.getValue(), currentResult.getEpoch(MILLISECONDS));
        }

        BigDecimal valueDelta;
        if (currentValue instanceof Long) {
            valueDelta = new BigDecimal(currentValue.longValue() - previousValue.longValue());
        } else if (currentValue instanceof Integer) {
            valueDelta = new BigDecimal(currentValue.intValue() - previousValue.intValue());
        } else if (currentValue instanceof Float) {
            valueDelta = new BigDecimal(currentValue.floatValue() - previousValue.floatValue());
        } else if (currentValue instanceof Double) {
            valueDelta = new BigDecimal(currentValue.doubleValue() - previousValue.doubleValue());
        } else {
            logger.info("unsupported value type '" + currentValue.getClass() + ", cannot calculate perMinute " + currentResult);

            return currentResult;
        }

        // multiply by 1000 because duration will be in millis
        // multiply by 60 for per-minute
        BigDecimal perMinute = valueDelta.movePointRight(3).multiply(new BigDecimal(60)).divide(durationInMillis, RoundingMode.HALF_UP);

        Number newCurrentValue;
        if (currentValue instanceof Long) {
            newCurrentValue = perMinute.longValue();
        } else if (currentValue instanceof Integer) {
            newCurrentValue = perMinute.intValue();
        } else if (currentValue instanceof Float) {
            newCurrentValue = perMinute.floatValue();
        } else if (currentValue instanceof Double) {
            newCurrentValue = perMinute.doubleValue();
        } else {
            logger.info("Illegal state " + previousResult);

            return currentResult;
        }

        return new QueryResult(currentResult.getName(), "gauge", newCurrentValue, currentResult.getEpoch(MILLISECONDS));
    }

}
