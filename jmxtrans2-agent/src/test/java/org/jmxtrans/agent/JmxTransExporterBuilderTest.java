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
package org.jmxtrans.agent;

import org.jmxtrans.config.Interval;
import org.jmxtrans.config.Invocation;
import org.jmxtrans.naming.ResultNameStrategyImpl;
import org.jmxtrans.output.OutputWriter;
import org.jmxtrans.query.ResultNameStrategy;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class JmxTransExporterBuilderTest {

    @Test
    public void loadConfigurationFromClasspath() throws Exception {
        JmxTransExporterBuilder jmxTransExporterBuilder = spy(new JmxTransExporterBuilder());
        JmxTransExporter jmxTransExporter = jmxTransExporterBuilder.build("classpath:jmxtrans-agent.xml");

        assertThat(jmxTransExporter).isNotNull();

        ArgumentCaptor<ResultNameStrategy> resultNameStrategy = ArgumentCaptor.forClass(ResultNameStrategy.class);
        ArgumentCaptor<Collection> invocations = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Collection> queries = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<OutputWriter> outputWriter = ArgumentCaptor.forClass(OutputWriter.class);
        ArgumentCaptor<Interval> interval = ArgumentCaptor.forClass(Interval.class);

        verify(jmxTransExporterBuilder).createJmxTransExporter(
                resultNameStrategy.capture(),
                invocations.capture(),
                queries.capture(),
                outputWriter.capture(),
                interval.capture()
        );

        assertThat(resultNameStrategy.getValue()).isNotNull();
        assertThat(resultNameStrategy.getValue()).isExactlyInstanceOf(ResultNameStrategyImpl.class);

        assertThat(invocations.getValue()).hasSize(1);
        Invocation invocation = (Invocation) invocations.getValue().iterator().next();
        assertThat(invocation.resultAlias).isEqualTo("jvm.gc");
    }

}
