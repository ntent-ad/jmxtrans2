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
import org.jmxtrans.query.embedded.InProcessServer;
import org.jmxtrans.query.embedded.Query;
import org.jmxtrans.query.embedded.Server;
import org.jmxtrans.utils.time.Interval;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@NotThreadSafe
final class ModifiableConfiguration implements Configuration {

    @Nonnull
    private final Collection<Query> queries = new ArrayList<>();
    private Interval queryPeriod;
    @Nonnull
    private final Collection<OutputWriter> outputWriters = new ArrayList<>();
    @Nonnull
    private final Collection<Invocation> invocations = new ArrayList<>();
    private Interval invocationPeriod;

    @Nonnull
    @Override
    public Collection<Query> getQueries() {
        return queries;
    }

    @Nonnull
    @Override
    public Iterable<Server> getServers() {
        return Collections.<Server>singletonList(new InProcessServer(queries));
    }

    @Nonnull
    @Override
    public Interval getQueryPeriod() {
        if (queryPeriod == null) return DefaultConfiguration.getInstance().getQueryPeriod();
        return queryPeriod;
    }

    public void setQueryPeriod(@Nonnull Interval queryPeriod) {
        this.queryPeriod = queryPeriod;
    }

    @Nonnull
    @Override
    public Collection<OutputWriter> getOutputWriters() {
        return outputWriters;
    }

    @Nonnull
    @Override
    public Collection<Invocation> getInvocations() {
        return invocations;
    }

    @Nonnull
    @Override
    public Interval getInvocationPeriod() {
        if (invocationPeriod == null) return DefaultConfiguration.getInstance().getInvocationPeriod();
        return invocationPeriod;
    }

    public void setInvocationPeriod(@Nonnull Interval invocationPeriod) {
        this.invocationPeriod = invocationPeriod;
    }
}
