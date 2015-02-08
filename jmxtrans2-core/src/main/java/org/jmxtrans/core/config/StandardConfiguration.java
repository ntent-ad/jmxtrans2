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
package org.jmxtrans.core.config;

import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.jmxtrans.core.output.OutputWriter;
import org.jmxtrans.core.query.Invocation;
import org.jmxtrans.core.query.Server;
import org.jmxtrans.utils.time.Interval;

@ThreadSafe // TODO: synchronization is overly aggressive
public class StandardConfiguration implements Configuration {

    @Nonnull
    private final CopyOnWriteArrayList<Server> servers = new CopyOnWriteArrayList<>();
    @Nonnull
    private volatile Interval period;
    @Nonnull
    private final CopyOnWriteArrayList<OutputWriter> outputWriters = new CopyOnWriteArrayList<>();
    @Nonnull
    private final CopyOnWriteArrayList<Invocation> invocations = new CopyOnWriteArrayList<>();

    public StandardConfiguration(Configuration configuration) {
        servers.clear();
        for (Server server : configuration.getServers()) {
            servers.add(server);
        }
        period = configuration.getPeriod();
        outputWriters.clear();
        for (OutputWriter outputWriter : configuration.getOutputWriters()) {
            outputWriters.add(outputWriter);
        }
        invocations.clear();
        for (Invocation invocation : configuration.getInvocations()) {
            invocations.add(invocation);
        }
    }

    @Nonnull
    @Override
    public Iterable<Server> getServers() {
        return servers;
    }

    @Override
    @Nonnull
    public synchronized Interval getPeriod() {
        return period;
    }

    @Override
    @Nonnull
    public synchronized Iterable<OutputWriter> getOutputWriters() {
        return outputWriters;
    }

    @Override
    @Nonnull
    public synchronized Iterable<Invocation> getInvocations() {
        return invocations;
    }
}
