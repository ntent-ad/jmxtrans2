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

import org.jmxtrans.output.OutputWriter;
import org.jmxtrans.query.Invocation;
import org.jmxtrans.query.embedded.Server;
import org.jmxtrans.utils.time.Interval;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.SECONDS;

@Immutable
@ThreadSafe
public class DefaultConfiguration implements Configuration {

    private static final Configuration INSTANCE = new DefaultConfiguration();

    private DefaultConfiguration() {
    }

    @Nonnull
    @Override
    public Iterable<Server> getServers() {
        return emptyList();
    }

    @Nonnull
    @Override
    public Interval getPeriod() {
        return new Interval(60, SECONDS);
    }

    @Nonnull
    @Override
    public Iterable<OutputWriter> getOutputWriters() {
        return emptyList();
    }

    @Nonnull
    @Override
    public Iterable<Invocation> getInvocations() {
        return emptyList();
    }

    public static Configuration getInstance() {
        return INSTANCE;
    }
}
