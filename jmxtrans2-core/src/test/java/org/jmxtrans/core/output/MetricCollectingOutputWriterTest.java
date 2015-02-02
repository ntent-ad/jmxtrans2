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

import javax.annotation.Nonnull;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jmxtrans.core.results.QueryResult;
import org.jmxtrans.utils.time.ManualClock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetricCollectingOutputWriterTest {
    
    @Nonnull private final ManualClock clock = new ManualClock();
    @Mock private OutputWriter outputWriter;
    @Mock private QueryResult result;
    private ObjectName objectName;
    private MetricCollectingOutputWriter metricCollectingOutputWriter;

    @Before
    public void createMetricCollectingOutputWriter() throws MalformedObjectNameException {
        objectName = new ObjectName("org.jmxtrans:type=MetricCollectingOutputWriter");
        metricCollectingOutputWriter = new MetricCollectingOutputWriter(clock, outputWriter, objectName);
    }
    
    @Test
    public void processedResultCountIsIncremented() throws IOException {
        when(outputWriter.write(result)).thenReturn(1);
        metricCollectingOutputWriter.write(result);
        assertThat(metricCollectingOutputWriter.getProcessedResultsCount()).isEqualTo(1);
    }
    
    @Test
    public void processingTimeIsCounted() throws IOException {
        when(outputWriter.write(result)).then(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                clock.waitFor(100, MILLISECONDS);
                return 1;
            }
        });
        
        metricCollectingOutputWriter.write(result);
        
        assertThat(metricCollectingOutputWriter.getProcessingTimeMillis()).isEqualTo(100);
    }
    
    @Test(expected = IOException.class)
    public void processingTimeIsCountedAlsoWhenExceptionIsThrown() throws IOException {
        when(outputWriter.write(result)).then(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                clock.waitFor(100, MILLISECONDS);
                throw new IOException();
            }
        });

        try {
            metricCollectingOutputWriter.write(result);
        } catch (IOException ioe) {
            assertThat(metricCollectingOutputWriter.getProcessingTimeMillis()).isEqualTo(100);
            throw ioe;
        }
    }
}
