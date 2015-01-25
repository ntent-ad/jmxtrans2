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

import org.jmxtrans.core.log.Logger;
import org.jmxtrans.core.log.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

@ThreadSafe
public class TcpSinkServer {

    @Nonnull private final Logger log = LoggerFactory.getLogger(getClass().getName());

    @Nullable private Thread thread = null;
    private boolean started = false;
    @Nullable private ServerSocket server;

    @Nonnull private final Object lock = new Object();
    @Nonnull private final Charset charset;
    @Nonnull private final List<String> messages = new CopyOnWriteArrayList<>();

    public TcpSinkServer(@Nonnull Charset charset) {
        this.charset = charset;
    }


    public void start() {
        if (thread != null) throw new IllegalStateException("Server already started");
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    try(ServerSocket s = new ServerSocket(0)) {
                        synchronized (lock) {
                            server = s;
                            started = true;
                            lock.notifyAll();
                        }
                        while (true) {
                            processRequests(server);
                        }
                    } finally {
                        synchronized (lock) {
                            server = null;
                        }
                    }
                } catch (IOException ioe) {
                    log.error("Exception in TCP echo server", ioe);
                }

            }
        });
        thread.start();
        try {
            synchronized (lock) {
                if (!started) {
                    log.debug("Waiting for server to start");
                    lock.wait(1000);
                }
                if (!started) throw new IllegalStateException("Server has not started");
            }
        } catch (InterruptedException interrupted) {
            throw new IllegalStateException("TCP Echo server seems to take too long to start", interrupted);
        }
    }

    private void processRequests(@Nonnull ServerSocket server) throws IOException {
        try (Socket socket = server.accept()) {
            try( BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), charset))) {
                String line;
                while ((line = in.readLine()) != null) {
                    messages.add(line);
                }
            }
        }
    }

    public void stop() {
        synchronized (lock) {
            if (!started) throw new IllegalStateException("Server not started");
        }
        thread.interrupt();
    }

    @Nonnull
    public InetSocketAddress getLocalSocketAddress()  {
        synchronized (lock) {
            if (!started) throw new IllegalStateException("Server not started");
            return new InetSocketAddress("localhost", server.getLocalPort());
        }
    }

    @Nonnull
    public Callable<Boolean> hasReceived(@Nonnull final String message) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return messages.contains(message);
            }
        };
    }
}
