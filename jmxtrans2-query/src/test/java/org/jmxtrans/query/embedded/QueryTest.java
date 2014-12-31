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

import org.jmxtrans.results.QueryResult;
import org.jmxtrans.utils.concurrent.DiscardingBlockingQueue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
public class QueryTest {

    static MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
    static ObjectName mockEdenSpacePool;
    static ObjectName mockPermGenPool;


    @BeforeClass
    public static void beforeClass() throws Exception {
        mockEdenSpacePool = new ObjectName("test:type=MemoryPool,name=PS Eden Space");
        mbeanServer.registerMBean(new MockMemoryPool("PS Eden Space", 87359488L), mockEdenSpacePool);
        mockPermGenPool = new ObjectName("test:type=MemoryPool,name=PS Perm Gen");
        mbeanServer.registerMBean(new MockMemoryPool("PS Perm Gen", 87752704L), mockPermGenPool);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        mbeanServer.unregisterMBean(mockEdenSpacePool);
        mbeanServer.unregisterMBean(mockPermGenPool);
    }

    @Test
    public void basic_jmx_attribute_return_simple_result() throws Exception {
        Query query = new Query("test:type=MemoryPool,name=PS Eden Space").addAttribute("CollectionUsageThreshold");
        BlockingQueue<QueryResult> results = new DiscardingBlockingQueue<QueryResult>(10);
        query.collectMetrics(mbeanServer, results);
        assertThat(results).hasSize(1);

        QueryResult result = results.poll();
        assertThat(result.getValue()).isInstanceOf(Number.class);
    }

    @Test
    public void test_composite_jmx_attribute() throws Exception {
        Query query = new Query("test:type=MemoryPool,name=PS Perm Gen");
        query.addAttribute(QueryAttribute.builder("Usage")
                .withKeys(Arrays.asList("committed", "init", "max", "used"))
                .build());
        BlockingQueue<QueryResult> results = new DiscardingBlockingQueue<QueryResult>(10);
        query.collectMetrics(mbeanServer, results);
        assertThat(results).hasSize(4);

        QueryResult result1 = results.poll();
        assertThat(result1.getValue()).isInstanceOf(Number.class);

        QueryResult result2 = results.poll();
        assertThat(result2.getValue()).isInstanceOf(Number.class);
    }
}
