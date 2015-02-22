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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jmxtrans.core.log.Logger;
import org.jmxtrans.core.results.QueryResult;
import org.jmxtrans.utils.appinfo.AppInfo;
import org.jmxtrans.utils.io.NullOutputStream;

import static java.lang.String.format;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import static org.jmxtrans.core.log.LoggerFactory.getLogger;
import static org.jmxtrans.utils.io.Charsets.US_ASCII;
import static org.jmxtrans.utils.io.IoUtils.copy;

public class HttpOutputWriter<T extends OutputStreamBasedOutputWriter> implements BatchedOutputWriter {

    @Nonnull private final Logger logger = getLogger(getClass().getName());
    
    @Nonnull private final ThreadLocal<HttpURLConnection> connection = new ThreadLocal<>();

    @Nonnull final private URL url;
    private final int timeoutInMillis;
    @Nullable final private Proxy proxy;
    @Nullable final private String contentType;
    @Nullable final private String basicAuthentication;
    @Nonnull final private AppInfo<?> appInfo;
    @Nonnull final private T target;

    private HttpOutputWriter(
            @Nonnull URL url,
            int timeoutInMillis,
            @Nullable Proxy proxy,
            @Nullable String contentType,
            @Nullable String basicAuthentication,
            @Nonnull AppInfo<?> appInfo,
            @Nonnull T target) {
        this.url = url;
        this.timeoutInMillis = timeoutInMillis;
        this.proxy = proxy;
        this.contentType = contentType;
        this.basicAuthentication = basicAuthentication;
        this.appInfo = appInfo;
        this.target = target;
    }

    @Override
    public void beforeBatch() throws IOException {
        HttpURLConnection urlConnection = openConnection();
        configureConnection(urlConnection);
        connection.set(urlConnection);
        target.beforeBatch(getURLConnection().getOutputStream());
    }

    @Override
    public int write(@Nonnull QueryResult result) throws IOException {
        HttpURLConnection urlConnection = getURLConnection();
        int count = target.write(urlConnection.getOutputStream(), result);
        return count;
    }

    private HttpURLConnection getURLConnection() {
        HttpURLConnection urlConnection = connection.get();
        if (urlConnection == null) throw new IllegalStateException("Connection has not been initialized");
        return urlConnection;
    }

    @Override
    public int afterBatch() throws IOException {
        HttpURLConnection urlConnection = getURLConnection();
        try {
            return target.afterBatch(urlConnection.getOutputStream());
        } finally {
            if (urlConnection.getResponseCode() != HTTP_OK) {
                throw new IOException("Error connecting to server, response code is not OK but " + urlConnection.getResponseCode());
            }
            try {
                disposeOfConnection(connection.get());
            } finally {
                connection.remove();
            }
        }
    }

    @Nonnull
    private HttpURLConnection openConnection() throws IOException {
        if (proxy == null) return (HttpURLConnection) url.openConnection();
        return (HttpURLConnection) url.openConnection(proxy);
    }

    private void configureConnection(@Nonnull HttpURLConnection urlConnection) throws ProtocolException {
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setReadTimeout(timeoutInMillis);
        if (contentType != null) {
            urlConnection.setRequestProperty("content-type", contentType);
        }
        if (basicAuthentication != null) {
            urlConnection.setRequestProperty("Authorization", "Basic " + basicAuthentication);
        }
        urlConnection.setRequestProperty("User-Agent", appInfo.getUserAgent());
    }

    private void disposeOfConnection(@Nullable HttpURLConnection urlConnection) {
        if (urlConnection != null) {
            consumeInputStream(urlConnection);
            consumeErrorStream(urlConnection);
        }
    }

    private void consumeInputStream(@Nonnull HttpURLConnection urlConnection) {
        try (InputStream in = urlConnection.getInputStream()) {
            if (in != null) copy(in, new NullOutputStream());
        } catch (FileNotFoundException ignore) {
        } catch (IOException e) {
            logger.error("Could not consume input stream", e);
        }
    }

    private void consumeErrorStream(@Nonnull HttpURLConnection urlConnection) {
        try (InputStream in = urlConnection.getErrorStream()) {
            if (in != null) copy(in, new NullOutputStream());
        } catch (IOException e) {
            logger.error("Could not consume error stream", e);
        }
    }
    
    @Nonnull
    public static <T extends OutputStreamBasedOutputWriter> Builder<T> builder(
            @Nonnull URL url, @Nonnull AppInfo<?> appInfo, @Nonnull T target) {
        return new Builder(url, appInfo, target);
    }

    public static final class Builder<T extends OutputStreamBasedOutputWriter> {
        @Nonnull private final List<String> VALID_PROTOCOLS = asList("HTTP", "HTTPS");

        @Nonnull private final URL url;
        @Nullable private final AppInfo<?> appInfo;
        @Nonnull private final T target;

        private int timeoutInMillis = 1000;
        @Nullable private Proxy proxy;
        @Nullable private String contentType;
        @Nullable private String basicAuthentication;

        public Builder(URL url, AppInfo<?> appInfo, T target) {
            this.url = validateHttp(url);
            this.appInfo = appInfo;
            this.target = target;
        }

        @Nonnull
        public Builder<T> withAuthentication(@Nonnull String username, @Nonnull String password) {
            basicAuthentication = printBase64Binary((username + ":" + password).getBytes(US_ASCII));
            return this;
        }

        @Nonnull
        public Builder<T> withTimeout(int value, @Nonnull TimeUnit unit) {
            timeoutInMillis = (int) MILLISECONDS.convert(value, unit);
            return this;
        }

        @Nonnull
        public Builder<T> withProxy(@Nonnull Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        @Nonnull
        public Builder<T> withContentType(@Nonnull String contentType) {
            this.contentType = contentType;
            return this;
        }

        @Nonnull
        public HttpOutputWriter<T> build() {
            return new HttpOutputWriter<>(url, timeoutInMillis, proxy, contentType, basicAuthentication, appInfo, target);
        }

        @Nonnull
        private URL validateHttp(@Nonnull URL url) {
            if (!VALID_PROTOCOLS.contains(url.getProtocol().toUpperCase())) {
                throw new IllegalArgumentException(format("URL needs to be HTTP or HTTPS [%s]", url));
            }
            return url;
        }
    }
}
