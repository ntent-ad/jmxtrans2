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
package org.jmxtrans.core.output.support;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;

import org.jmxtrans.core.results.QueryResult;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.jmxtrans.utils.io.Charsets.UTF_8;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TcpOutputWriterTest {

    @Mock private QueryResult result;
    private TcpSinkServer server;
    private Charset charset = UTF_8;

    @Before
    public void startTcpServer() throws IOException {
        server = new TcpSinkServer(charset);
        server.start();
    }

    @Test
    public void test() throws IOException, InterruptedException {
        TcpOutputWriter tcpOutputWriter = new TcpOutputWriter(
                server.getLocalSocketAddress(),
                100,
                charset,
                new DummyWriter()
        );
        tcpOutputWriter.beforeBatch();
        int processedResultCount = tcpOutputWriter.write(result);
        tcpOutputWriter.afterBatch();

        assertThat(processedResultCount).isEqualTo(1);
        await().until(server.hasReceived("test"));
    }

    @After
    public void stopTcpServer() throws IOException {
        server.stop();
    }

    private class DummyWriter implements WriterBasedOutputWriter {
        @Override
        public int write(@Nonnull Writer writer, @Nonnull QueryResult result) throws IOException {
            writer.write("test");
            return 1;
        }
    }
}
