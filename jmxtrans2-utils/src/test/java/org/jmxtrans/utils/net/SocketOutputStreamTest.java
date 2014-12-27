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
package org.jmxtrans.utils.net;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SocketOutputStreamTest {

    @Test
    public void socketIsAccessibleFromSocketOutputStream() throws IOException {
        Socket socket = mock(Socket.class);
        OutputStream out = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(out);

        SocketOutputStream socketOutputStream = new SocketOutputStream(socket);

        assertThat(socketOutputStream.getSocket()).isEqualTo(socket);
    }

    @Test
    public void writesArePropagatedToSocket() throws IOException {
        Socket socket = mock(Socket.class);
        OutputStream out = mock(OutputStream.class);
        when(socket.getOutputStream()).thenReturn(out);

        SocketOutputStream socketOutputStream = new SocketOutputStream(socket);
        socketOutputStream.write(1);

        verify(out).write(1);
    }

    @Test
    public void SocketWriterHasReasonableToString() throws IOException {
        Socket socket = mock(Socket.class);
        OutputStream out = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.toString()).thenReturn("some string");

        SocketOutputStream socketOutputStream = new SocketOutputStream(socket);


        assertThat(socketOutputStream.toString()).startsWith("SocketOutputStream");
        assertThat(socketOutputStream.toString()).contains("some string");
    }
}
