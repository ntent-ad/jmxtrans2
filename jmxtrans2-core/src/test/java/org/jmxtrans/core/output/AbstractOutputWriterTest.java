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

import org.jmxtrans.core.output.AbstractOutputWriter;
import org.jmxtrans.core.results.QueryResult;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.logging.Level;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

public class AbstractOutputWriterTest {

    @Test
    public void loggersAreInitializedCorrectlyAtTraceAndFinestLevels() {
        DummyOutputWriter traceOutputWriter = new DummyOutputWriter("trace");
        assertThat(traceOutputWriter.getTraceLevel()).isEqualTo(Level.INFO);
        assertThat(traceOutputWriter.getDebugLevel()).isEqualTo(Level.INFO);
        assertThat(traceOutputWriter.getInfoLevel()).isEqualTo(Level.INFO);

        DummyOutputWriter finestOutputWriter = new DummyOutputWriter("finest");
        assertThat(finestOutputWriter.getTraceLevel()).isEqualTo(Level.INFO);
        assertThat(finestOutputWriter.getDebugLevel()).isEqualTo(Level.INFO);
        assertThat(finestOutputWriter.getInfoLevel()).isEqualTo(Level.INFO);
    }

    @Test
    public void loggersAreInitializedCorrectlyAtDebugAndFinerAndFineLevels() {
        DummyOutputWriter debugOutputWriter = new DummyOutputWriter("debug");
        assertThat(debugOutputWriter.getTraceLevel()).isEqualTo(Level.FINE);
        assertThat(debugOutputWriter.getDebugLevel()).isEqualTo(Level.INFO);
        assertThat(debugOutputWriter.getInfoLevel()).isEqualTo(Level.INFO);

        DummyOutputWriter finerOutputWriter = new DummyOutputWriter("finer");
        assertThat(finerOutputWriter.getTraceLevel()).isEqualTo(Level.FINE);
        assertThat(finerOutputWriter.getDebugLevel()).isEqualTo(Level.INFO);
        assertThat(finerOutputWriter.getInfoLevel()).isEqualTo(Level.INFO);

        DummyOutputWriter fineOutputWriter = new DummyOutputWriter("fine");
        assertThat(fineOutputWriter.getTraceLevel()).isEqualTo(Level.FINE);
        assertThat(fineOutputWriter.getDebugLevel()).isEqualTo(Level.INFO);
        assertThat(fineOutputWriter.getInfoLevel()).isEqualTo(Level.INFO);
    }

    @Test
    public void loggersAreInitializedCorrectlyAtWarnLevel() {
        DummyOutputWriter warnOutputWriter = new DummyOutputWriter("warn");
        assertThat(warnOutputWriter.getTraceLevel()).isEqualTo(Level.FINE);
        assertThat(warnOutputWriter.getDebugLevel()).isEqualTo(Level.FINE);
        assertThat(warnOutputWriter.getInfoLevel()).isEqualTo(Level.FINE);
    }

    @Test
    public void resultsAreDispatched() throws IOException {
        DummyOutputWriter outputWriter = new DummyOutputWriter("warn");
        QueryResult result = new QueryResult("name", "value", 0);

        outputWriter.write(singleton(result));

        assertThat(outputWriter.result).isNotNull();
        assertThat(outputWriter.result).isEqualTo(result);

    }

    private static final class DummyOutputWriter extends AbstractOutputWriter {
        private QueryResult result;
        protected DummyOutputWriter(@Nonnull String logLevel) {
            super(logLevel);
        }

        @Override
        public void write(@Nonnull QueryResult result) throws IOException {
            this.result = result;
        }
    }

}
