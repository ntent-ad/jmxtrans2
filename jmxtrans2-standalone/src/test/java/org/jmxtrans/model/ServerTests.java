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
package org.jmxtrans.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * @author lanyonm
 */
public class ServerTests {

	@Test
	public void testGetUrl() {
		// test with host and port
		Server server = Server.builder().setHost("mysys.mydomain").setPort("8004").build();
		assertEquals("should be 'service:jmx:rmi:///jndi/rmi://mysys.mydomain:8004/jmxrmi'", "service:jmx:rmi:///jndi/rmi://mysys.mydomain:8004/jmxrmi", server.getUrl());
		// test with url
		server = Server.builder()
				.setUrl("service:jmx:remoting-jmx://mysys.mydomain:8004")
				.build();
		assertEquals("should be 'service:jmx:remoting-jmx://mysys.mydomain:8004'", "service:jmx:remoting-jmx://mysys.mydomain:8004", server.getUrl());

		server = Server.builder()
				.setUrl("service:jmx:rmi:///jndi/rmi://mysys.mydomain:8004/jmxrmi")
				.build();
		assertEquals("shold be 'service:jmx:rmi:///jndi/rmi://mysys.mydomain:8004/jmxrmi'", "service:jmx:rmi:///jndi/rmi://mysys.mydomain:8004/jmxrmi", server.getUrl());
	}

	@Test
	public void testGetHostAndPortFromUrl() {
		Server server = Server.builder()
				.setUrl("service:jmx:remoting-jmx://mysys.mydomain:8004")
				.build();
		assertEquals("server host should be 'mysys.mydomain'", "mysys.mydomain", server.getHost());
		assertEquals("server port should be '8004'", "8004", server.getPort());
		// test with a different url
		server = Server.builder()
				.setUrl("service:jmx:rmi:///jndi/rmi://mysys.mydomain:8004/jmxrmi")
				.build();
		assertEquals("server host should be 'mysys.mydomain'", "mysys.mydomain", server.getHost());
		assertEquals("server port should be '8004'", "8004", server.getPort());
	}

	@Test
	public void testEquals() {
		Server s1 = Server.builder()
				.setAlias("alias")
				.setHost("host")
				.setPort("8008")
				.setCronExpression("cron")
				.setNumQueryThreads(123)
				.setPassword("pass")
				.setUsername("user")
				.build();

		Server s2 = Server.builder()
				.setAlias("alias")
				.setHost("host")
				.setPort("8008")
				.setCronExpression("cron")
				.setNumQueryThreads(123)
				.setPassword("pass")
				.setUsername("user")
				.build();

		Server s3 = Server.builder()
				.setAlias("alias")
				.setHost("host3")
				.setPort("8008")
				.setCronExpression("cron")
				.setNumQueryThreads(123)
				.setPassword("pass")
				.setUsername("user")
				.build();

		assertFalse(s1.equals(null));
		assertEquals(s1, s1);
		assertFalse(s1.equals("hi"));
		assertEquals(s1, s2);
		assertTrue(s1.equals(s2));
		assertNotSame(s1, s3);
	}

	@Test
	public void testHashCode() {
		Server s1 = Server.builder()
				.setUrl("service:jmx:remoting-jmx://mysys.mydomain:8004")
				.build();
		Server s2 = Server.builder()
				.setUrl("service:jmx:remoting-jmx://mysys.mydomain:8004")
				.build();
		Server s3 = Server.builder()
				.setUrl("service:jmx:remoting-jmx://mysys3.mydomain:8004")
				.build();
		assertEquals(s1.hashCode(), s2.hashCode());
		assertFalse(s1.hashCode() == s3.hashCode());
	}
}
