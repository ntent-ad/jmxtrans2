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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.jmxtrans.core.results.QueryResult;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LibratoWriterTest {

    private LibratoWriter libratoWriter;
    private ByteArrayOutputStream out;
    private QueryResult counterResult1;
    private QueryResult counterResult2;
    private QueryResult gaugeResult1;
    private QueryResult gaugeResult2;
    private QueryResult unkownType;
    private QueryResult noType;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeClass
    public void createLibratoWriter() {
        libratoWriter = new LibratoWriter(new JsonFactory(), "myHost.test.net");
    }

    @BeforeMethod
    public void createOutputStream() {
        out = new ByteArrayOutputStream();
    }

    @BeforeClass
    public void createResults() {
        counterResult1 = new QueryResult("counter1", "counter", 1, 1000L);
        counterResult2 = new QueryResult("counter2", "counter", 2L, 2000L);
        gaugeResult1 = new QueryResult("gauge1", "gauge", 3.3, 3000L);
        gaugeResult2 = new QueryResult("gauge2", "gauge", 4.4f, 4000L);
        unkownType = new QueryResult("unkownType", "unkownType", 5, 5000L);
        noType = new QueryResult("noType", null, 6, 6000L);
    }

    @Test
    public void canWriteSimpleResults() throws IOException {
        libratoWriter.beforeBatch(out);
        libratoWriter.write(out, counterResult1);
        libratoWriter.write(out, counterResult2);
        libratoWriter.write(out, gaugeResult1);
        libratoWriter.write(out, gaugeResult2);
        libratoWriter.afterBatch(out);

        JsonNode tree = objectMapper.readTree(out.toByteArray());
        JsonNode counters = tree.findPath("counters");
        assertThat(counters.isArray()).isTrue();
        assertThat(counters.size()).isEqualTo(2);

        JsonNode counter1 = counters.get(0);
        assertThat(counter1.get("name").textValue()).isEqualTo("counter1");
        assertThat(counter1.get("source").textValue()).isEqualTo("myHost.test.net");
        assertThat(counter1.get("measure_time").intValue()).isEqualTo(1);
        assertThat(counter1.get("value").intValue()).isEqualTo(1);

        JsonNode counter2 = counters.get(1);
        assertThat(counter2.get("name").textValue()).isEqualTo("counter2");
        assertThat(counter2.get("source").textValue()).isEqualTo("myHost.test.net");
        assertThat(counter2.get("measure_time").intValue()).isEqualTo(2);
        assertThat(counter2.get("value").longValue()).isEqualTo(2L);

        JsonNode gauges = tree.findPath("gauges");
        assertThat(gauges.isArray()).isTrue();

        JsonNode gauge1 = gauges.get(0);
        assertThat(gauge1.get("name").textValue()).isEqualTo("gauge1");
        assertThat(gauge1.get("source").textValue()).isEqualTo("myHost.test.net");
        assertThat(gauge1.get("measure_time").intValue()).isEqualTo(3);
        assertThat(gauge1.get("value").doubleValue()).isEqualTo(3.3);

        JsonNode gauge2 = gauges.get(1);
        assertThat(gauge2.get("name").textValue()).isEqualTo("gauge2");
        assertThat(gauge2.get("source").textValue()).isEqualTo("myHost.test.net");
        assertThat(gauge2.get("measure_time").intValue()).isEqualTo(4);
        assertThat(gauge2.get("value").floatValue()).isEqualTo(4.4f);
    }

    @Test
    public void resultCountIsCorrect() throws IOException {
        int counter = 0;
        libratoWriter.beforeBatch(out);
        counter += libratoWriter.write(out, counterResult1);
        counter += libratoWriter.write(out, gaugeResult1);
        counter += libratoWriter.afterBatch(out);

        assertThat(counter).isEqualTo(2);
    }

    @Test
    public void nonNumericValuesAreSentEmpty() throws IOException {
        libratoWriter.beforeBatch(out);
        libratoWriter.write(out, new QueryResult("nonNumeric", "counter", "stringValue", 1000));
        libratoWriter.afterBatch(out);

        JsonNode tree = objectMapper.readTree(out.toByteArray());
        JsonNode counters = tree.findPath("counters");
        assertThat(counters.isArray()).isTrue();
        assertThat(counters.size()).isEqualTo(1);

        JsonNode counter1 = counters.get(0);
        assertThat(counter1.get("name").textValue()).isEqualTo("nonNumeric");
        assertThat(counter1.get("value")).isNull();
    }

    @Test
    public void atomicIntegerAreSentAsNumbers() throws IOException {
        libratoWriter.beforeBatch(out);
        libratoWriter.write(out, new QueryResult("atomicInteger", "counter", new AtomicInteger(1), 1000));
        libratoWriter.afterBatch(out);

        JsonNode tree = objectMapper.readTree(out.toByteArray());
        JsonNode counters = tree.findPath("counters");
        assertThat(counters.isArray()).isTrue();
        assertThat(counters.size()).isEqualTo(1);

        JsonNode counter1 = counters.get(0);
        assertThat(counter1.get("name").textValue()).isEqualTo("atomicInteger");
        assertThat(counter1.get("value").intValue()).isEqualTo(1);
    }

    @Test
    public void atomicLongAreSentAsNumbers() throws IOException {
        libratoWriter.beforeBatch(out);
        libratoWriter.write(out, new QueryResult("atomicLong", "counter", new AtomicLong(2), 1000));
        libratoWriter.afterBatch(out);

        JsonNode tree = objectMapper.readTree(out.toByteArray());
        JsonNode counters = tree.findPath("counters");
        assertThat(counters.isArray()).isTrue();
        assertThat(counters.size()).isEqualTo(1);

        JsonNode counter1 = counters.get(0);
        assertThat(counter1.get("name").textValue()).isEqualTo("atomicLong");
        assertThat(counter1.get("value").longValue()).isEqualTo(2);
    }
    
    @Test
    public void nullAndUnknownTypesAreTreatedAsCounters() throws IOException {
        libratoWriter.beforeBatch(out);
        libratoWriter.write(out, unkownType);
        libratoWriter.write(out, noType);
        libratoWriter.afterBatch(out);

        JsonNode tree = objectMapper.readTree(out.toByteArray());
        JsonNode counters = tree.findPath("counters");
        assertThat(counters.size()).isEqualTo(2);
    }

    @Test
    public void factoryCanCreateLibratoWriter() {
        Map<String, String> settings = new HashMap<>();
        assertThat(new LibratoWriter.Factory().create(settings)).isNotNull();
    }
}
