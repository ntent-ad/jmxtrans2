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
package org.jmxtrans.query.embedded;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.HashMap;
import java.util.Map;

import static javax.management.remote.JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES;
import static javax.naming.Context.SECURITY_CREDENTIALS;
import static javax.naming.Context.SECURITY_PRINCIPAL;

public class RemoteServer implements Server {

    @Nonnull private final JMXServiceURL url;
    @Nullable private final String username;
    @Nullable private final String password;
    @Nonnull private final String protocolProviderPackages;
    @Nonnull private final Iterable<Query> queries;

    public RemoteServer(
            @Nonnull JMXServiceURL url,
            @Nullable String username,
            @Nullable String password,
            @Nonnull String protocolProviderPackages,
            @Nonnull Iterable<Query> queries) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.protocolProviderPackages = protocolProviderPackages;
        this.queries = queries;
    }

    @Override
    public String getHost() {
        return url.getHost();
    }

    private Map<String, ?> getEnvironment() {
        if (protocolProviderPackages != null && protocolProviderPackages.contains("weblogic")) {
            Map<String, String> environment = new HashMap<>();
            if ((username != null) && (password != null)) {
                environment.put(PROTOCOL_PROVIDER_PACKAGES, protocolProviderPackages);
                environment.put(SECURITY_PRINCIPAL, username);
                environment.put(SECURITY_CREDENTIALS, password);
            }
            return environment;
        }

        Map<String, String[]> environment = new HashMap<>();
        if ((username != null) && (password != null)) {
            String[] credentials = new String[] {
                    username,
                    password
            };
            environment.put(JMXConnector.CREDENTIALS, credentials);
        }
        return environment;
    }

    @Override
    public MBeanServerConnection getServerConnection() throws Exception {
        return JMXConnectorFactory.connect(url, this.getEnvironment()).getMBeanServerConnection();
    }

    @Override
    public Iterable<Query> getQueries() {
        return queries;
    }


}
