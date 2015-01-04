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
import org.jmxtrans.query.embedded.Query;
import org.jmxtrans.utils.time.Interval;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.concurrent.TimeUnit.SECONDS;

@ThreadSafe
public class StandardConfiguration implements Configuration {

    @Nonnull
    private final CopyOnWriteArrayList<Query> queries = new CopyOnWriteArrayList<>();
    @Nonnull
    private volatile Interval queryPeriod = new Interval(60, SECONDS);
    @Nonnull
    private final CopyOnWriteArrayList<OutputWriter> outputWriters = new CopyOnWriteArrayList<>();
    @Nonnull
    private final CopyOnWriteArrayList<Invocation> invocations = new CopyOnWriteArrayList<>();
    @Nonnull
    private volatile Interval invocationPeriod = new Interval(60, SECONDS);

    @Override
    @Nonnull
    public Iterable<Query> getQueries() {
        return queries;
    }

    @Override
    @Nonnull
    public Interval getQueryPeriod() {
        return queryPeriod;
    }

    @Override
    @Nonnull
    public Iterable<OutputWriter> getOutputWriters() {
        return outputWriters;
    }

    @Override
    @Nonnull
    public Iterable<Invocation> getInvocations() {
        return invocations;
    }

    @Override
    @Nonnull
    public Interval getInvocationPeriod() {
        return invocationPeriod;
    }

    public void addQueries(@Nonnull Collection<Query> queries) {
        this.queries.addAll(queries);
    }

    public void addInvocations(@Nonnull Collection<Invocation> invocations) {
        this.invocations.addAll(invocations);
    }

    public void addOutputWriters(@Nonnull Collection<OutputWriter> outputWriters) {
        this.outputWriters.addAll(outputWriters);
    }

    public void setQueryPeriod(@Nonnull Interval queryPeriod) {
        this.queryPeriod = queryPeriod;
    }
}
