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
package org.jmxtrans.core.output.writers;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.jmxtrans.core.output.support.MinimalFormatOutputWriter;

import org.junit.Test;

import static java.util.Collections.emptyMap;

import static org.jmxtrans.core.results.QueryResultFixtures.standardQueryResult;
import static org.jmxtrans.core.results.QueryResultFixtures.standardQueryResultMinimallyFormatted;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsoleOutputWriterTest {
    
    @Test
    public void resultsArePrintedAndFormatted() throws IOException {
        StringWriter output = new StringWriter();

        new ConsoleOutputWriter(new MinimalFormatOutputWriter(), output)
                .write(standardQueryResult());
        
        assertThat(output.toString()).isEqualTo(standardQueryResultMinimallyFormatted());
    }
    
    @Test
    public void factoryCreatesConsoleWriter() {
        Map<String, String> settings = emptyMap();
        ConsoleOutputWriter consoleOutputWriter = new ConsoleOutputWriter.Factory().create(settings);
        
        assertThat(consoleOutputWriter).isNotNull();
    }
    
}
