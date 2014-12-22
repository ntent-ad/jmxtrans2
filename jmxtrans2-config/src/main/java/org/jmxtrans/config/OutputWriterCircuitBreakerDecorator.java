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
package org.jmxtrans.config;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jmxtrans.utils.ConfigurationUtils.getBoolean;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class OutputWriterCircuitBreakerDecorator implements OutputWriter {
    private final static String SETTING_ENABLED = "enabled";
    private static final int MAX_FAILURES = 5;
    private final Logger logger;
    // visible for testing
    public final OutputWriter delegate;
    private boolean enabled = true;
    private static final long DISABLE_DURATION_MILLIS = 60 * 1000;
    private final AtomicInteger failuresCounter = new AtomicInteger();
    private long disabledUntil = 0;

    public OutputWriterCircuitBreakerDecorator(OutputWriter delegate) {
        this.delegate = delegate;
        logger = Logger.getLogger(delegate.getClass().getName() + "CircuitBreaker");
    }

    @Override
    public void postConstruct(@Nonnull Map<String, String> settings) {
        enabled = getBoolean(settings, SETTING_ENABLED, true);
        delegate.postConstruct(settings);
    }

    @Override
    public void preDestroy() {
        delegate.preDestroy();
    }

    @Override
    public void preCollect() throws IOException {
        if (isDisabled()) {
            return;
        }
        try {
            delegate.preCollect();
            incrementOutputWriterSuccess();
        } catch (RuntimeException e) {
            incrementOutputWriterFailures();
            throw e;
        } catch (IOException e) {
            incrementOutputWriterFailures();
            throw e;
        }
    }

    @Override
    public void write(Iterable<QueryResult> results) throws IOException {
        for (QueryResult result : results) {
            write(result);
        }
    }

    @Override
    public void write(QueryResult result) throws IOException {
        if (isDisabled()) {
            return;
        }
        try {
            delegate.write(new QueryResult(result.getName(), result.getType(), System.currentTimeMillis()));
            incrementOutputWriterSuccess();
        } catch (RuntimeException e) {
            incrementOutputWriterFailures();
            throw e;
        } catch (IOException e) {
            incrementOutputWriterFailures();
            throw e;
        }
    }

    @Override
    public void writeInvocationResult(@Nonnull String invocationName, @Nonnull Object value) throws IOException {
        if (isDisabled()) {
            return;
        }
        try {
            delegate.writeInvocationResult(invocationName, value);
            incrementOutputWriterSuccess();
        } catch (RuntimeException e) {
            incrementOutputWriterFailures();
            throw e;
        } catch (IOException e) {
            incrementOutputWriterFailures();
            throw e;
        }
    }

    @Override
    public void postCollect() throws IOException {
        if (isDisabled()) {
            return;
        }
        try {
            delegate.postCollect();
            incrementOutputWriterSuccess();
        } catch (RuntimeException e) {
            incrementOutputWriterFailures();
            throw e;
        } catch (IOException e) {
            incrementOutputWriterFailures();
            throw e;
        }
    }

    public boolean isDisabled() {
        if (!enabled) {
            logger.finer("OutputWriter is globally disabled");
            return true;
        } else if (disabledUntil == 0) {
            logger.finer("OutputWriter is not temporarily disabled");
            return false;
        } else if (disabledUntil < System.currentTimeMillis()) {
            logger.fine("re-enable OutputWriter");
            // reset counter
            disabledUntil = 0;
            return false;
        } else {
            if (logger.isLoggable(Level.FINE))
                logger.fine("OutputWriter is disabled until " + new Timestamp(disabledUntil));
            return true;
        }
    }

    public void incrementOutputWriterFailures() {
        int failuresCount = failuresCounter.incrementAndGet();
        if (failuresCount >= MAX_FAILURES) {
            disabledUntil = System.currentTimeMillis() + DISABLE_DURATION_MILLIS;
            failuresCounter.set(0);
            logger.warning("Too many exceptions, disable writer until " + new Timestamp(disabledUntil));
        }
    }

    public void incrementOutputWriterSuccess() {
        if (failuresCounter.get() > 0) {
            logger.fine("Reset failures counter to 0");
            failuresCounter.set(0);
        }
    }
}
