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

import org.jmxtrans.utils.jmx.JmxUtils2;
import org.jmxtrans.results.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Describe a JMX query on which metrics are collected.
 *
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 * @author Jon Stevens
 */
public class Query implements QueryMBean {

    private static final AtomicInteger queryIdSequence = new AtomicInteger();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Mainly used for monitoring.
     */
    private final String id = "query-" + queryIdSequence.getAndIncrement();

    /**
     * ObjectName of the Query MBean(s) to monitor, can contain
     */
    @Nonnull
    private ObjectName objectName;

    @Nullable
    private String resultAlias;
    /**
     * JMX attributes to collect. As an array for {@link javax.management.MBeanServer#getAttributes(javax.management.ObjectName, String[])}
     */
    @Nonnull
    private Map<String, QueryAttribute> attributesByName = new HashMap<String, QueryAttribute>();
    /**
     * Copy of {@link #attributesByName}'s {@link java.util.Map#entrySet()} for performance optimization
     */
    @Nonnull
    private String[] attributeNames = new String[0];

    @Nonnull
    private final AtomicInteger collectedMetricsCount = new AtomicInteger();

    @Nonnull
    private final AtomicLong collectionDurationInNanos = new AtomicLong();

    @Nonnull
    private final AtomicInteger collectionCount = new AtomicInteger();

    @Nonnull
    private final AtomicInteger exportedMetricsCount = new AtomicInteger();

    @Nonnull
    private final AtomicLong exportDurationInNanos = new AtomicLong();

    @Nonnull
    private final AtomicInteger exportCount = new AtomicInteger();

    /**
     * {@link javax.management.ObjectName} of this {@link QueryMBean}
     */
    @Nullable
    private ObjectName queryMbeanObjectName;
    private MBeanServer mbeanServer;

    /**
     * Creates a {@linkplain Query} on the given <code>objectName</code>.
     *
     * @param objectName {@link javax.management.ObjectName} to query, can contain wildcards ('*' or '?')
     */
    public Query(@Nonnull String objectName) {
        try {
            this.objectName = new ObjectName(objectName);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException("Exception parsing '" + objectName + "'", e);
        }
    }

    /**
     * Creates a {@linkplain Query} on the given <code>objectName</code>.
     *
     * @param objectName {@link javax.management.ObjectName} to query, can contain wildcards ('*' or '?')
     */
    public Query(@Nonnull ObjectName objectName) {
        this.objectName = objectName;
    }


    @Override
    public void collectMetrics(@Nonnull MBeanServer mbeanServer, @Nonnull BlockingQueue<QueryResult> results) {
        long nanosBefore = System.nanoTime();
        /*
         * Optimisation tip: no need to skip 'mbeanServer.queryNames()' if the ObjectName is not a pattern
         * (i.e. not '*' or '?' wildcard) because the mbeanserver internally performs the check.
         * Seen on com.sun.jmx.interceptor.DefaultMBeanServerInterceptor
         */
        Set<ObjectName> matchingObjectNames = mbeanServer.queryNames(this.objectName, null);
        logger.trace("Query {} returned {}", objectName, matchingObjectNames);

        for (ObjectName matchingObjectName : matchingObjectNames) {
            long epochInMillis = System.currentTimeMillis();
            try {
                AttributeList jmxAttributes = mbeanServer.getAttributes(matchingObjectName, this.attributeNames);
                logger.trace("Query {} returned {}", matchingObjectName, jmxAttributes);
                for (Attribute jmxAttribute : jmxAttributes.asList()) {
                    QueryAttribute queryAttribute = this.attributesByName.get(jmxAttribute.getName());
                    Object value = jmxAttribute.getValue();
                    int count = queryAttribute.collectMetrics(matchingObjectName, value, epochInMillis, results, this, new ResultNameStrategy());
                    collectedMetricsCount.addAndGet(count);
                }
            } catch (Exception e) {
                logger.warn("Exception processing query {}", this, e);
            }
        }
        collectionCount.incrementAndGet();
        long nanosAfter = System.nanoTime();
        collectionDurationInNanos.addAndGet(nanosAfter - nanosBefore);
    }

    @PostConstruct
    public void start() throws Exception {
        queryMbeanObjectName = JmxUtils2.registerObject(this, "org.jmxtrans.embedded:Type=Query,id=" + id, mbeanServer);
    }

    @PreDestroy
    public void stop() throws Exception {
        JmxUtils2.unregisterObject(queryMbeanObjectName, mbeanServer);
    }

    @Override
    @Nonnull
    public ObjectName getObjectName() {
        return objectName;
    }

    @Nonnull
    public Collection<QueryAttribute> getQueryAttributes() {
        return attributesByName.values();
    }

    /**
     * Add the given attribute to the list attributes of this query
     *
     * @param attribute attribute to add
     * @return this
     */
    @Nonnull
    public Query addAttribute(@Nonnull QueryAttribute attribute) {
        attributesByName.put(attribute.getName(), attribute);
        attributeNames = attributesByName.keySet().toArray(new String[0]);
        return this;
    }

    /**
     * Create a basic {@link QueryAttribute}, add it to the list attributes of this query
     *
     * @param attributeName attribute to add
     * @return this
     */
    @Nonnull
    public Query addAttribute(@Nonnull String attributeName) {
        return addAttribute(QueryAttribute.builder(attributeName).build());
    }

    public void setResultAlias(@Nullable String resultAlias) {
        this.resultAlias = resultAlias;
    }

    public void setMbeanServer(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    @Override
    @Nullable
    public String getResultAlias() {
        return resultAlias;
    }

    @Override
    public String toString() {
        return "Query{" +
                "objectName=" + objectName +
                ", resultAlias='" + resultAlias + '\'' +
                ", attributes=" + attributesByName.values() +
                '}';
    }

    @Override
    public int getCollectedMetricsCount() {
        return collectedMetricsCount.get();
    }

    @Override
    public long getCollectionDurationInNanos() {
        return collectionDurationInNanos.get();
    }

    @Override
    public int getCollectionCount() {
        return collectionCount.get();
    }

    @Override
    public int getExportedMetricsCount() {
        return exportedMetricsCount.get();
    }

    @Override
    public long getExportDurationInNanos() {
        return exportDurationInNanos.get();
    }

    @Override
    public int getExportCount() {
        return exportCount.get();
    }

    @Override
    public String getId() {
        return id;
    }

}
