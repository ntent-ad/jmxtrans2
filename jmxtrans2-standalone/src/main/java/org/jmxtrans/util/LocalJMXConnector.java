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
package org.jmxtrans.util;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnector;
import javax.security.auth.Subject;
import java.io.IOException;
import java.util.Map;

/**
 * Represents a connection to a local {@link javax.management.MBeanServerConnection}
 */
public class LocalJMXConnector implements JMXConnector {
	private final MBeanServerConnection serverConnection;

	public LocalJMXConnector(MBeanServerConnection serverConnection) {
		this.serverConnection = serverConnection;
	}

	public void connect() throws IOException {
	}

	public void connect(Map<String, ?> env) throws IOException {
	}

	public MBeanServerConnection getMBeanServerConnection() throws IOException {
		return serverConnection;
	}

	public MBeanServerConnection getMBeanServerConnection(Subject delegationSubject) throws IOException {
		return serverConnection;
	}

	public void close() throws IOException {
	}

	public void addConnectionNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) {
		// TODO
	}

	public void removeConnectionNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
		// TODO
	}

	public void removeConnectionNotificationListener(NotificationListener l, NotificationFilter f, Object handback) throws ListenerNotFoundException {
		// TODO
	}

	public String getConnectionId() throws IOException {
		return "LocalJMXConnector";
	}
}
