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
package org.jmxtrans.output.writers.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Nonnull;

import org.jmxtrans.core.log.Logger;
import org.jmxtrans.core.log.LoggerFactory;
import org.jmxtrans.core.output.OutputWriter;
import org.jmxtrans.core.results.QueryResult;
import org.jmxtrans.core.results.QueryResultTimeComparator;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.sort;

public class BatchingOutputWriter<T extends BatchedOutputWriter> implements OutputWriter {

    @Nonnull private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Nonnull private final LinkedBlockingQueue<QueryResult> resultQueue;
    private final int batchSize;
    @Nonnull private final T outputWriter;
    @Nonnull private final Comparator<QueryResult> batchOrder = new QueryResultTimeComparator();

    public BatchingOutputWriter(int batchSize, @Nonnull T outputWriter) {
        this.batchSize = batchSize;
        resultQueue = new LinkedBlockingQueue<>(batchSize);
        this.outputWriter = outputWriter;
    }


    @Override
    public int write(@Nonnull QueryResult result) throws IOException {
        List<QueryResult> batch = enqueueAndGetBatch(result);
        if (batch.isEmpty()) return 0;
        return processBatch(batch);
    }

    // FIXME: There is a problem with synchronization here. All operations on the resultQueue are synchronized so using
    // a concurrent collection does not make much sense. But we still need to ensure that the queue is drained only
    // once if multiple results arrive when the queue is full. This needs some more love ...
    @Nonnull
    private synchronized List<QueryResult> enqueueAndGetBatch(@Nonnull QueryResult result) {
        if (!resultQueue.offer(result)) {
            List<QueryResult> batch = new ArrayList<>(batchSize);
            resultQueue.drainTo(batch);
            // I cannot see any reason why this offer could fail, so let's not check the result
            resultQueue.offer(result);
            return batch;
        }
        return emptyList();
    }

    private int processBatch(@Nonnull List<QueryResult> batch) throws IOException {
        try {
            int counter = 0;
            outputWriter.beforeBatch();
            sort(batch, batchOrder);
            for (QueryResult result : batch) {
                try {
                    outputWriter.write(result);
                    counter++;
                } catch (IOException ioe) {
                    logger.warn(format("Error writing result [%s] to output writer [%s].", result, outputWriter), ioe);
                }
            }
            return counter;
        } finally {
            outputWriter.afterBatch();
        }
    }
}
