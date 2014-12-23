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
package org.jmxtrans.query;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.jmxtrans.results.QueryResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class QueryTest {
    static MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
    static ObjectName mockObjectName;
    static Mock mock = new Mock("PS Eden Space", 87359488L);
    private DummyResultNameStrategy resultNameStrategy = new DummyResultNameStrategy();

    @BeforeClass
    public static void beforeClass() throws Exception {
        mockObjectName = new ObjectName("test:type=Mock,name=mock");
        mbeanServer.registerMBean(mock, mockObjectName);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        mbeanServer.unregisterMBean(mockObjectName);
    }

    @Test
    public void basic_attribute_return_simple_result() throws Exception {
        Query query = new Query("test:type=Mock,name=mock", "CollectionUsageThreshold", resultNameStrategy);
        Queue<QueryResult> results = new ArrayBlockingQueue<QueryResult>(1);

        query.collectAndExport(mbeanServer, results);

        assertThat(results).hasSize(1);
        QueryResult queryResult = results.poll();
        assertThat(queryResult).isNotNull();
        assertThat(queryResult.getName()).isEqualTo("CollectionUsageThreshold");
        assertThat(queryResult.getValue()).isInstanceOf(Number.class);
    }

    @Test
    public void result_name_are_processed() {
        Query query = new Query("test:type=Mock,name=mock", "CollectionUsageThreshold", resultNameStrategy);
        Queue<QueryResult> results = new ArrayBlockingQueue<QueryResult>(1);

        query.collectAndExport(mbeanServer, results);

        assertThat(resultNameStrategy.hasBeenCalled()).isTrue();
    }

    @Test
    public void indexed_list_attribute_return_simple_result() throws Exception {
        Query query = new Query("test:type=Mock,name=mock", "IntegerList", 1, resultNameStrategy);
        Queue<QueryResult> results = new ArrayBlockingQueue<QueryResult>(1);

        query.collectAndExport(mbeanServer, results);

        assertThat(results).hasSize(1);
        QueryResult queryResult = results.poll();
        assertThat(queryResult).isNotNull();
        assertThat(queryResult.getName()).isEqualTo("IntegerList");
        assertThat(queryResult.getValue()).isInstanceOf(Number.class);
    }

    @Test
    public void non_indexed_list_attribute_return_simple_result() throws Exception {
        Query query = new Query("test:type=Mock,name=mock", "IntegerList", resultNameStrategy);
        Queue<QueryResult> results = new ArrayBlockingQueue<QueryResult>(mock.getIntegerList().size());

        query.collectAndExport(mbeanServer, results);

        for (int i = 0; i < mock.getIntegerList().size(); i++) {
            final String name = "IntegerList_" + i;
            Optional<QueryResult> result = FluentIterable.from(results).firstMatch(new Predicate<QueryResult>() {
                @Override
                public boolean apply(@Nullable QueryResult input) {
                    return input != null && name.equals(input.getName());
                }
            });

            org.assertj.guava.api.Assertions.assertThat(result).isPresent();
            assertThat(result.get().getValue()).isInstanceOf(Number.class);
        }
    }

    @Test
    public void indexed_int_array_attribute_return_simple_result() throws Exception {
        Query query = new Query("test:type=Mock,name=mock", "IntArray", 1, resultNameStrategy);
        Queue<QueryResult> results = new ArrayBlockingQueue<QueryResult>(1);

        query.collectAndExport(mbeanServer, results);

        assertThat(results).hasSize(1);
        QueryResult queryResult = results.poll();
        assertThat(queryResult).isNotNull();
        assertThat(queryResult.getName()).isEqualTo("IntArray");
        assertThat(queryResult.getValue()).isInstanceOf(Number.class);
    }

    @Test
    public void indexed_integer_array_attribute_return_simple_result() throws Exception {
        Query query = new Query("test:type=Mock,name=mock", "IntegerArray", 1, resultNameStrategy);
        Queue<QueryResult> results = new ArrayBlockingQueue<QueryResult>(1);

        query.collectAndExport(mbeanServer, results);

        assertThat(results).hasSize(1);
        QueryResult queryResult = results.poll();
        assertThat(queryResult).isNotNull();
        assertThat(queryResult.getName()).isEqualTo("IntegerArray");
        assertThat(queryResult.getValue()).isInstanceOf(Number.class);
    }

}
