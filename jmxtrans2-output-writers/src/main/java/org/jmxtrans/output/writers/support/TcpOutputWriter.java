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
package org.jmxtrans.output.writers.support;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.jmxtrans.core.results.QueryResult;

/**
 * Provide base functionality to write a TCP based OutputWriter.
 *
 * To ensure optimal use of network resources, TcpOutputWriter is implemented as a
 * {@link BatchedOutputWriter} and the socket writer is wrapped in a
 * {@link java.io.BufferedWriter}. Socket is opened and connected when writing the first result and closed after the
 * batch.
 *
 * In case of error on the TCP connection during the write of the batch, a new socket is created when writing the next
 * result. Previous results might be lost. Implementation of this class may choose to flush the writer to improve
 * predictability at the cost of performance.
 *
 * Note: At this point a TCP connection is created and closed for each batch. It would probably make sense to pool the
 * connections instead.
 */
@ThreadSafe
public class TcpOutputWriter<T extends WriterBasedOutputWriter> implements BatchedOutputWriter {

    @Nonnull private final ThreadLocal<BufferedWriter> threadLocalWriter = new ThreadLocal<>();
    @Nonnull private final ThreadLocal<Socket> threadLocalSocket = new ThreadLocal<>();
    @Nonnull private final InetSocketAddress server;
    @Nonnull private final Charset charset;
    private final int socketTimeoutMillis;
    @Nonnull private final T target;

    public TcpOutputWriter(
            @Nonnull InetSocketAddress server,
            int socketTimeoutMillis,
            @Nonnull Charset charset,
            @Nonnull T target) {
        this.server = server;
        this.charset = charset;
        this.socketTimeoutMillis = socketTimeoutMillis;
        this.target = target;
    }

    @Override
    public void beforeBatch() {}

    @Override
    public int write(@Nonnull QueryResult result) throws IOException {
        target.write(getWriter(), result);
        return 1;
    }

    @Nonnull
    protected Writer getWriter() throws IOException {
        ensureConnected();
        Writer writer = threadLocalWriter.get();
        if (writer == null) {
            throw new IllegalStateException("Writer has not been initialized");
        }
        return writer;
    }

    private void ensureConnected() throws IOException {
        if (!connectionHealthy()) {
            releaseSocket();
            connectToServer();
        }
    }

    private boolean connectionHealthy() {
        try {
            Socket socket = threadLocalSocket.get();
            return socket != null
                    && socket.isConnected()
                    && socket.isBound()
                    && !socket.isClosed()
                    && !socket.isInputShutdown()
                    && !socket.isOutputShutdown();
        } catch (Exception e) {
            return false;
        }
    }

    @Nonnull
    private void connectToServer() throws IOException {
        // create new InetSocketAddress to ensure name resolution is done again
        SocketAddress serverAddress = new InetSocketAddress(server.getHostName(), server.getPort());
        Socket socket = new Socket();
        socket.setKeepAlive(false);
        socket.connect(serverAddress, socketTimeoutMillis);
        threadLocalWriter.set(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), charset)));
    }

    private void releaseSocket() throws IOException {
        try (Socket socket = threadLocalSocket.get();
             BufferedWriter writer = threadLocalWriter.get()) {
            threadLocalWriter.remove();
            threadLocalSocket.remove();
        }
    }

    @Override
    public void afterBatch() throws IOException {
        releaseSocket();
    }
}
