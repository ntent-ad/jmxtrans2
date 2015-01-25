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

import org.jmxtrans.core.log.Logger;
import org.jmxtrans.core.log.LoggerFactory;
import org.jmxtrans.core.results.QueryResult;
import org.jmxtrans.utils.time.Clock;
import org.jmxtrans.utils.time.NanoChronometer;
import org.jmxtrans.utils.time.SystemClock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.util.Objects.hash;

/**
 * Describe a JMX query on which metrics are collected.
 *
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 * @author Jon Stevens
 */
public class Query implements QueryMBean {

    @Nonnull
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    /**
     * ObjectName of the Query MBean(s) to monitor, can contain
     */
    @Nonnull
    private final ObjectName objectName;

    @Nullable
    private final String resultAlias;
    /**
     * JMX attributes to collect. As an array for {@link javax.management.MBeanServer#getAttributes(javax.management.ObjectName, String[])}
     */
    @Nonnull
    private final Map<String, QueryAttribute> attributesByName;
    /**
     * Copy of {@link #attributesByName}'s {@link java.util.Map#entrySet()} for performance optimization
     */
    @Nonnull
    private final String[] attributeNames;

    @Nonnull
    private final QueryMetrics metrics;

    /**
     * {@link javax.management.ObjectName} of this {@link QueryMBean}
     */
    @Nonnull
    private final ObjectName queryMbeanObjectName;

    @Nullable
    private MBeanServer mbeanServer;

    @Nonnull private final Clock clock;

    private Query(@Nonnull ObjectName objectName,
                  @Nullable String resultAlias,
                  @Nonnull List<QueryAttribute> attributes,
                  @Nonnull ObjectName queryMbeanObjectName,
                  @Nonnull Clock clock) {
        this.objectName = objectName;
        this.resultAlias = resultAlias;
        this.attributesByName = new HashMap<>();
        for (QueryAttribute attribute : attributes) {
            attributesByName.put(attribute.getName(), attribute);
        }
        this.attributeNames = attributesByName.keySet().toArray(new String[0]);
        this.queryMbeanObjectName = queryMbeanObjectName;
        metrics = new QueryMetrics(clock);
        this.clock = clock;
    }

    public Iterable<QueryResult> collectMetrics(@Nonnull MBeanServerConnection mbeanServer, @Nonnull ResultNameStrategy resultNameStrategy) throws IOException {
        try (NanoChronometer chrono = metrics.collectionDurationChronometer()) {
            Collection<QueryResult> results = new ArrayList<>();
            /*
             * Optimisation tip: no need to skip 'mbeanServer.queryNames()' if the ObjectName is not a pattern
             * (i.e. not '*' or '?' wildcard) because the mbeanserver internally performs the check.
             * Seen on com.sun.jmx.interceptor.DefaultMBeanServerInterceptor
             */
            Set<ObjectName> matchingObjectNames = mbeanServer.queryNames(this.objectName, null);
            logger.debug(format("Query %s returned %s", objectName, matchingObjectNames));

            for (ObjectName matchingObjectName : matchingObjectNames) {
                long epochInMillis = clock.currentTimeMillis();
                try {
                    AttributeList jmxAttributes = mbeanServer.getAttributes(matchingObjectName, this.attributeNames);
                    logger.debug(format("Query %s returned %s", matchingObjectName, jmxAttributes));
                    for (Attribute jmxAttribute : jmxAttributes.asList()) {
                        QueryAttribute queryAttribute = this.attributesByName.get(jmxAttribute.getName());
                        Object value = jmxAttribute.getValue();
                        int count = queryAttribute.collectMetrics(matchingObjectName, value, epochInMillis, results, this, resultNameStrategy);
                        metrics.incrementCollected(count);
                    }
                } catch (Exception e) {
                    logger.warn(format("Exception processing query %s", this), e);
                }
            }
            metrics.incrementCollectionsCount();
            return results;
        }
    }

    @PostConstruct
    public void start() throws Exception {
        if (mbeanServer != null) {
            mbeanServer.registerMBean(this, queryMbeanObjectName);
        }
    }

    @PreDestroy
    public void stop() throws Exception {
        if (mbeanServer != null) {
            mbeanServer.unregisterMBean(queryMbeanObjectName);
        }
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

    @Override
    @Nullable
    public String getResultAlias() {
        return resultAlias;
    }

    @Nonnull
    public ObjectName getQueryMbeanObjectName() {
        return queryMbeanObjectName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Query that = (Query) o;

        return Objects.equals(attributesByName, that.attributesByName)
                && Objects.equals(objectName, that.objectName)
                && Objects.equals(resultAlias, that.resultAlias);
    }

    @Override
    public int hashCode() {
        return hash(objectName, resultAlias, attributesByName);
    }

    @Override
    @Nonnull
    public String toString() {
        return "Query{" +
                "objectName=" + objectName +
                ", resultAlias='" + resultAlias + '\'' +
                ", attributes=" + attributesByName.values() +
                '}';
    }

    @Override
    public int getCollectedMetricsCount() {
        return metrics.getCollectedCount();
    }

    @Override
    public long getCollectionDurationInNanos() {
        return metrics.getCollectionDurationNano();
    }

    @Override
    public int getCollectionCount() {
        return metrics.getCollectionsCount();
    }

    @Override
    @Nonnull
    public String getId() {
        return queryMbeanObjectName.getCanonicalName();
    }

    public void setMbeanServer(@Nonnull MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        @Nonnull private static final AtomicInteger queryIdSequence = new AtomicInteger();

        @Nullable private ObjectName objectName;
        @Nullable private String resultAlias;
        @Nonnull private final List<QueryAttribute> attributes = new ArrayList<>();
        @Nonnull private final Clock clock;

        private Builder() {
            this.clock = new SystemClock();
        }

        @Nonnull public Builder withObjectName(@Nonnull String objectName) {
            try {
                withObjectName(new ObjectName(objectName));
                return this;
            } catch (MalformedObjectNameException e) {
                throw new RuntimeException("Object name [" + objectName + "] is not valid, cannot build query.");
            }
        }

        @Nonnull
        public Builder withObjectName(@Nonnull ObjectName objectName) {
            this.objectName = objectName;
            return this;
        }

        public Builder withResultAlias(@Nullable String resultAlias) {
            this.resultAlias = resultAlias;
            return this;
        }

        public Builder addAttribute(@Nonnull String attributeName) {
            addAttribute(QueryAttribute.builder(attributeName).build());
            return this;
        }

        public Builder addAttribute(@Nonnull QueryAttribute attribute) {
            attributes.add(attribute);
            return this;
        }

        public Builder addAttributes(@Nonnull Collection<QueryAttribute> attributes) {
            this.attributes.addAll(attributes);
            return this;
        }

        @Nonnull
        public Query build() {
            try {
                if (objectName == null) {
                    throw new RuntimeException("Cannot create query without an object name, please check your code");
                }
                return new Query(
                        objectName,
                        resultAlias,
                        attributes,
                        new ObjectName("org.jmxtrans.embedded:Type=Query,id=" + queryIdSequence.incrementAndGet()),
                        clock
                );
            } catch (MalformedObjectNameException e) {
                throw new RuntimeException("Object name [" + objectName + "] is not valid, cannot expose MBean for this query.");
            }
        }
    }
}
