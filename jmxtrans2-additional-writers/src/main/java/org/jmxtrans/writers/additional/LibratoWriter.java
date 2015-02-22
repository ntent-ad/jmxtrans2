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
package org.jmxtrans.writers.additional;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import org.jmxtrans.core.log.Logger;
import org.jmxtrans.core.log.LoggerFactory;
import org.jmxtrans.core.output.OutputWriterFactory;
import org.jmxtrans.core.output.support.BatchingOutputWriter;
import org.jmxtrans.core.output.support.HttpOutputWriter;
import org.jmxtrans.core.output.support.OutputStreamBasedOutputWriter;
import org.jmxtrans.core.results.QueryResult;
import org.jmxtrans.utils.appinfo.AppInfo;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import static java.lang.String.format;
import static java.net.Proxy.Type.HTTP;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import static org.jmxtrans.core.output.support.HttpOutputWriter.builder;
import static org.jmxtrans.utils.ConfigurationUtils.getInt;
import static org.jmxtrans.utils.ConfigurationUtils.getString;

import static com.fasterxml.jackson.core.JsonEncoding.UTF8;

@ThreadSafe
public class LibratoWriter implements OutputStreamBasedOutputWriter {

    @Nonnull private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Nonnull private final ThreadLocal<ResultsClassifier> resultsClassifier = new ThreadLocal<ResultsClassifier>() {
        @Override
        protected ResultsClassifier initialValue() {
            return new ResultsClassifier();
        }
    };
    @Nonnull private final JsonFactory jsonFactory;
    @Nullable private final String source;

    public LibratoWriter(@Nonnull JsonFactory jsonFactory, @Nullable String source) {
        this.jsonFactory = jsonFactory;
        this.source = source;
    }

    @Override
    public void beforeBatch(@Nonnull OutputStream out) throws IOException {
        resultsClassifier.get().clear();
    }

    @Override
    public int write(@Nonnull OutputStream out, @Nonnull QueryResult result) throws IOException {
        resultsClassifier.get().addResult(result);
        return 0;
    }

    @Override
    public int afterBatch(@Nonnull OutputStream out) throws IOException {
        try(JsonGenerator jsonGenerator = jsonFactory.createGenerator(out, UTF8)) {
            int resultsWritten = 0;
            jsonGenerator.writeStartObject();
            resultsWritten += writeResultsAs("counters", jsonGenerator, resultsClassifier.get().getCounters());
            resultsWritten += writeResultsAs("gauges", jsonGenerator, resultsClassifier.get().getGauges());
            jsonGenerator.writeEndObject();
            jsonGenerator.flush();
            return resultsWritten;
        } finally {
            resultsClassifier.remove();
        }
    }

    private int writeResultsAs(String name, JsonGenerator jsonGenerator, Iterable<QueryResult> results) throws IOException {
        int counter = 0;
        jsonGenerator.writeArrayFieldStart(name);
        for (QueryResult result : results) {
            jsonGenerator.writeStartObject();
            
            jsonGenerator.writeStringField("name", result.getName());
            
            if (source != null && !source.isEmpty()) {
                jsonGenerator.writeStringField("source", source);
            }
            
            jsonGenerator.writeNumberField("measure_time", result.getEpoch(SECONDS));
            
            if (result.getValue() instanceof Number) {
                writeNumberField(jsonGenerator, "value", (Number)result.getValue());
            } else {
                logger.info(format("Value for result [%s] is not a number, cannot send it to Librato", result));
            }
            
            jsonGenerator.writeEndObject();
            counter++;
        }
        jsonGenerator.writeEndArray();
        return counter;
    }

    private void writeNumberField(JsonGenerator jsonGenerator, String name, Number value) throws IOException {
        if (value instanceof Integer) {
            jsonGenerator.writeNumberField(name, (Integer)value);
        } else if (value instanceof Long) {
            jsonGenerator.writeNumberField(name, (Long)value);
        } else if (value instanceof Float) {
            jsonGenerator.writeNumberField(name, (Float)value);
        } else if (value instanceof Double) {
            jsonGenerator.writeNumberField(name, (Double)value);
        } else if (value instanceof AtomicInteger) {
            jsonGenerator.writeNumberField(name, ((AtomicInteger)value).get());
        } else if (value instanceof AtomicLong) {
            jsonGenerator.writeNumberField(name, ((AtomicLong) value).get());
        }
    }

    @NotThreadSafe
    private static final class ResultsClassifier {
        @Nonnull private final Logger logger = LoggerFactory.getLogger(getClass().getName());
        
        @Nonnull private final Queue<QueryResult> counters = new ArrayDeque<>();
        @Nonnull private final Queue<QueryResult> gauges = new ArrayDeque<>();

        public void addResult(@Nonnull QueryResult result) {
            if ("counter".equals(result.getType())) {
                counters.add(result);
            } else if ("gauge".equals(result.getType())) {
                gauges.add(result);
            } else if (result.getType() == null) {
                logger.info(format("Unspecified type for result [%s], export it as counter", result));
                counters.add(result);
            } else {
                logger.info(format("Unsupported metric type [%s] for result [%s], export it as counter", result.getType(), result));
                counters.add(result);
            }
        }
        
        public Iterable<QueryResult> getCounters() {
            return counters;
        }
        
        public Iterable<QueryResult> getGauges() {
            return gauges;
        }
        
        public void clear() {
            counters.clear();
            gauges.clear();
        }
    }
    
    @ThreadSafe
    public static final class Factory implements OutputWriterFactory<BatchingOutputWriter<HttpOutputWriter<LibratoWriter>>> {

        @Nonnull
        @Override
        public BatchingOutputWriter<HttpOutputWriter<LibratoWriter>> create(@Nonnull Map settings) {
            int batchSize = getInt(settings, "batchSize", 100);
            URL url = parseUrl(getString(settings, "libratoUrl", "https://metrics-api.librato.com/v1/metrics"));
            int timeoutInMillis = getInt(settings, "timeoutInMillis", 1000);
            Proxy proxy = getProxy(settings);
            String source = "hostname"; // FIXME: correct handling of source should be implemented after #60 is done.
            String username = getString(settings, "username", null);
            String token = getString(settings, "token", null);
            
            HttpOutputWriter.Builder<LibratoWriter> httpOutputWriter =
                    builder(url, loadAppInfo(), new LibratoWriter(new JsonFactory(), source))
                            .withContentType("application/json; charset=utf-8")
                            .withTimeout(timeoutInMillis, MILLISECONDS);
            
            if (username != null && !username.isEmpty()) {
                httpOutputWriter.withAuthentication(username, token);
            }
            if (proxy != null) {
                httpOutputWriter.withProxy(proxy);
            }
            
            return new BatchingOutputWriter<>(batchSize, httpOutputWriter.build());
        }

        @Nullable
        private Proxy getProxy(@Nonnull Map<String, String> settings) {
            String proxyHost = getString(settings, "proxyHost", null);
            Integer proxyPort = getInt(settings, "proxyPort", 0);
            
            if (proxyHost == null || proxyHost.isEmpty()) return null;
            
            return new Proxy(HTTP, new InetSocketAddress(proxyHost, proxyPort));
        }

        private AppInfo loadAppInfo() {
            try {
                return AppInfo.load(LibratoWriter.class);
            } catch (IOException e) {
                throw new IllegalStateException("Could not load AppInfo", e);
            }
        }

        private URL parseUrl(String urlString) {
            try {
                return new URL(urlString);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(format("Malformed Librato URL [%s]", urlString), e);
            }
        }
    }
}
