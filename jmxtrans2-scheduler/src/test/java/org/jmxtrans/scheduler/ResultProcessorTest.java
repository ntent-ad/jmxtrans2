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
package org.jmxtrans.scheduler;

import org.jmxtrans.output.OutputWriter;
import org.jmxtrans.results.QueryResult;
import org.jmxtrans.utils.time.ManualClock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ResultProcessorTest {

    private ManualClock clock = new ManualClock();
    private Executor resultExecutor = directExecutor();
    private ResultProcessor resultProcessor;
    private QueryResult result = new QueryResult("name", new Object(), 0);
    private BlockingQueue<QueryResult> results = new ArrayBlockingQueue<>(1);
    @Mock private OutputWriter outputWriter;

    @Before
    public void createResultProcessor() {
        resultProcessor = new ResultProcessor(clock, resultExecutor);
        results.add(result);
    }

    @Test
    public void resultsAreProcessed() throws IOException {
        resultProcessor.writeResults(1, results, outputWriter);
        verify(outputWriter).write(results);
    }

    @Test
    public void exceptionsFromWriterAreManaged() throws IOException {
        doThrow(new IOException()).when(outputWriter).write(any(Iterable.class));
        ResultProcessor.Processor processor = new ResultProcessor.Processor(clock, 10, results, outputWriter);
        processor.run();
    }

}
