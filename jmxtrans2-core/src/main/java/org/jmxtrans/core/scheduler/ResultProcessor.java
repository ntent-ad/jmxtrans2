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
package org.jmxtrans.core.scheduler;

import org.jmxtrans.core.log.Logger;
import org.jmxtrans.core.log.LoggerFactory;
import org.jmxtrans.core.output.OutputWriter;
import org.jmxtrans.core.results.QueryResult;
import org.jmxtrans.utils.time.Clock;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.concurrent.Executor;

public class ResultProcessor {

    @Nonnull
    private final Clock clock;
    @Nonnull
    private final Executor resultExecutor;

    public ResultProcessor(@Nonnull Clock clock, @Nonnull Executor resultExecutor) {
        this.clock = clock;
        this.resultExecutor = resultExecutor;
    }

    public void writeResult(
            long deadline,
            @Nonnull QueryResult result,
            @Nonnull OutputWriter outputWriter) {
        resultExecutor.execute(new Processor(clock, deadline, result, outputWriter));
    }

    @ThreadSafe
    public static class Processor extends DeadlineRunnable {
        @Nonnull private final Logger logger = LoggerFactory.getLogger(getClass().getName());
        @Nonnull private final QueryResult result;
        @Nonnull private final OutputWriter outputWriter;

        public Processor(
                @Nonnull Clock clock,
                long deadline,
                @Nonnull QueryResult result,
                @Nonnull OutputWriter outputWriter) {
            super(clock, deadline);
            this.result = result;
            this.outputWriter = outputWriter;
        }

        @Override
        protected void doRun() {
            try {
                logger.debug("Writing results to " + outputWriter);
                outputWriter.write(result);
            } catch (IOException e) {
                logger.warn("Je suis Charlie");
                logger.warn("Sadly, error while drawing results.", e);
            }
        }
    }
}
