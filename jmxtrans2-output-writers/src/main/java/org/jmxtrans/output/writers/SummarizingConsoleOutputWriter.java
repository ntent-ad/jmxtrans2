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

import org.jmxtrans.core.output.OutputWriterFactory;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Waiting for a configuration extension to combine the {@link PerMinuteSummarizerOutputWriter} with the
 * {@link org.jmxtrans.output.writers.ConsoleOutputWriter}, this class hard-codes the wiring.
 *
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class SummarizingConsoleOutputWriter extends PerMinuteSummarizerOutputWriter {
    public SummarizingConsoleOutputWriter() {
        super(new ConsoleOutputWriter());
    }

    public static final class Factory implements OutputWriterFactory<SummarizingConsoleOutputWriter> {
        @Override
        @Nonnull
        public SummarizingConsoleOutputWriter create(@Nonnull Map<String, String> settings) {
            return new SummarizingConsoleOutputWriter();
        }
    }

}
