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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// TODO : Very naive implementation, definitely needs to be improved !!!
@ThreadSafe
@Immutable
public class ConfigurationMerger {

    @Nonnull
    public Configuration merge(@Nonnull Collection<Configuration> configurations) {
        ModifiableConfiguration result = new ModifiableConfiguration();
        List<Configuration> configurationsWithDefault = new ArrayList<>();
        configurationsWithDefault.add(DefaultConfiguration.getInstance());
        configurationsWithDefault.addAll(configurations);
        for (Configuration configuration : configurationsWithDefault) {
            appendQueries(result, configuration.getQueries());
            appendInvocations(result, configuration.getInvocations());
            appendOutputWriters(result, configuration.getOutputWriters());
            result.setQueryPeriod(configuration.getQueryPeriod());
            result.setInvocationPeriod(configuration.getInvocationPeriod());
        }
        return result;
    }

    private void appendQueries(@Nonnull ModifiableConfiguration configuration, @Nonnull Iterable<Query> queries) {
        for (Query query : queries) {
            configuration.getQueries().add(query);
        }
    }

    private void appendInvocations(@Nonnull ModifiableConfiguration configuration, @Nonnull Iterable<Invocation> invocations) {
        for (Invocation invocation : invocations) {
            configuration.getInvocations().add(invocation);
        }
    }

    private void appendOutputWriters(@Nonnull ModifiableConfiguration configuration, @Nonnull Iterable<OutputWriter> outputWriters) {
        for (OutputWriter outputWriter : outputWriters) {
            configuration.getOutputWriters().add(outputWriter);
        }
    }

}
