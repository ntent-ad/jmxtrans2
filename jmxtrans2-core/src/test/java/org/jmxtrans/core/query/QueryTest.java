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
package org.jmxtrans.core.query;

import java.lang.management.ManagementFactory;
import java.util.Iterator;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jmxtrans.core.results.QueryResult;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;

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
        Iterable<QueryResult> results = Query.builder()
                .withObjectName("test:type=MemoryPool,name=PS Eden Space")
                .addAttribute("CollectionUsageThreshold")
                .build()
                .collectMetrics(mbeanServer, new ResultNameStrategy());
        assertThat(results).hasSize(1);

        QueryResult result = results.iterator().next();
        assertThat(result.getValue()).isInstanceOf(Number.class);
    }

    @Test
    public void attributesAreAvailableInNamingStrategy() throws Exception {
        Iterable<QueryResult> results = Query.builder()
                .withObjectName("test:type=*,name=PS Eden Space")
                .withResultAlias("memory.%type%")
                .addAttribute("CollectionUsageThreshold")
                .build()
                .collectMetrics(mbeanServer, new ResultNameStrategy());
        assertThat(results).hasSize(1);

        QueryResult result = results.iterator().next();
        assertThat(result.getValue()).isInstanceOf(Number.class);
        assertThat(result.getName()).isEqualTo("memory.MemoryPool.CollectionUsageThreshold");
    }

    @Test
    public void test_composite_jmx_attribute() throws Exception {
        Query query = Query.builder()
                .withObjectName("test:type=MemoryPool,name=PS Perm Gen")
                .addAttribute(QueryAttribute.builder("Usage")
                        .withKeys(asList("committed", "init", "max", "used"))
                        .build())
                .build();
        Iterable<QueryResult> results = query.collectMetrics(mbeanServer, new ResultNameStrategy());
        assertThat(results).hasSize(4);

        Iterator<QueryResult> queryResultIterator = results.iterator();

        QueryResult result1 = queryResultIterator.next();
        assertThat(result1.getValue()).isInstanceOf(Number.class);

        QueryResult result2 = queryResultIterator.next();
        assertThat(result2.getValue()).isInstanceOf(Number.class);
    }

    @Test
    public void maxResultsIsHonored() throws Exception {
        Query query = Query.builder()
                .withObjectName("test:type=MemoryPool,name=PS Perm Gen")
                .withMaxResults(2)
                .addAttribute(QueryAttribute.builder("Usage")
                        .withKeys(asList("committed", "init", "max", "used"))
                        .build())
                .build();

        Iterable<QueryResult> results = query.collectMetrics(mbeanServer, new ResultNameStrategy());

        assertThat(results).hasSize(2);
    }

    @Test
    public void queryWithSameNameAreEquals() {
        Query query1 = Query.builder()
                .withObjectName("test:type=MemoryPool,name=PS Perm Gen")
                .build();
        Query query2 = Query.builder()
                .withObjectName("test:type=MemoryPool,name=PS Perm Gen")
                .build();

        assertThat(query1).isEqualTo(query2);
    }
}
