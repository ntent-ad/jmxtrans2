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
package org.jmxtrans.embedded.output;

import org.jmxtrans.results.QueryResult;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Basic test for the Stackdriver writer
 */
public class StackdriverWriterTest {
    @Test
    public void testSerialize() throws Exception {

        List<QueryResult> metrics = Arrays.asList(
                new QueryResult("metric1", "counter", 10, System.currentTimeMillis()),
                new QueryResult("metric2", "counter", 11.11, System.currentTimeMillis() - 1000),
                new QueryResult("metric2", "counter", 12.12, System.currentTimeMillis()),
                new QueryResult("metric3", "gauge", 9.9, System.currentTimeMillis()),
                new QueryResult("metric3", "gauge", 12.12, System.currentTimeMillis() - 1000),
                new QueryResult("metric4", "gauge", 12.12, System.currentTimeMillis())

        );
        
        StackdriverWriter writer = new StackdriverWriter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        writer.serialize(metrics, baos);
        baos.flush();

        System.out.println(new String(baos.toByteArray()));

    }
}
