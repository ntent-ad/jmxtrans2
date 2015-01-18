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
package org.jmxtrans.core.query.embedded;

import org.junit.Test;

import java.net.MalformedURLException;

import static org.assertj.core.api.Assertions.assertThat;

public class RemoteServerTest {

    @Test
    public void hostAndPortsAreIgnoredIfUrlIsGiven() throws MalformedURLException {
        RemoteServer server = RemoteServer.builder()
                .withUrl("service:jmx:rmi://host.test.net:1234/jndi/rmi://host.test.net:4321/jmxrmi")
                .withHost("host2.test.net")
                .withPort(7890)
                .build();

        assertThat(server.getHost()).isEqualTo("host.test.net");
    }

    @Test
    public void weblogicSpecificEnvironmentIsUsedIfRequired() throws MalformedURLException {
        RemoteServer server = RemoteServer.builder()
                .withUrl("service:jmx:rmi://host.test.net:1234/jndi/rmi://host.test.net:4321/jmxrmi")
                .withHost("host2.test.net")
                .withPort(7890)
                .withProtocolProviderPackages("com.weblogic.test")
                .withUsername("username")
                .withPassword("password")
                .build();
        assertThat(server).isNotNull();
        // TODO: need an actual assertion
    }

    @Test
    public void nonWeblogicSpecificEnvironmentIsUsedIfRequired() throws MalformedURLException {
        RemoteServer server = RemoteServer.builder()
                .withUrl("service:jmx:rmi://host.test.net:1234/jndi/rmi://host.test.net:4321/jmxrmi")
                .withHost("host2.test.net")
                .withPort(7890)
                .withUsername("username")
                .withPassword("password")
                .build();
        assertThat(server).isNotNull();
        // TODO: need an actual assertion
    }

}
