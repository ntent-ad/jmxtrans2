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

import org.jmxtrans.results.QueryResult;
import org.jmxtrans.utils.collections.ArrayUtils;
import org.jmxtrans.utils.collections.Iterables2;
import org.jmxtrans.utils.Preconditions2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
@Immutable
@ThreadSafe
public class Query {

    private final Logger logger = Logger.getLogger(getClass().getName());

    @Nonnull
    private final ResultNameStrategy resultNameStrategy;

    @Nonnull
    private final ObjectName objectName;
    @Nonnull
    private final String resultAlias;
    /**
     * The attribute to retrieve ({@link javax.management.MBeanServer#getAttribute(javax.management.ObjectName, String)})
     */
    @Nonnull
    private final String attribute;
    /**
     * If the MBean attribute value is a {@link javax.management.openmbean.CompositeData}, the key to lookup.
     */
    @Nullable
    private final String key;
    /**
     * If the returned value is a {@link java.util.Collection}or an array, the position of the entry to lookup.
     */
    @Nullable
    private final Integer position;
    /**
     * Attribute type like '{@code gauge}' or '{@code counter}'. Used by monitoring systems like Librato who require this information.
     */
    @Nullable
    private final String type;

    /**
     * @see #Query(String, String, String, Integer, String, String, ResultNameStrategy)
     */
    public Query(@Nonnull String objectName, @Nonnull String attribute, @Nonnull ResultNameStrategy resultNameStrategy) {
        this(objectName, attribute, null, null, null, attribute, resultNameStrategy);
    }

    /**
     * @see #Query(String, String, String, Integer, String, String, ResultNameStrategy)
     */
    public Query(@Nonnull String objectName, @Nonnull String attribute, int position, @Nonnull ResultNameStrategy resultNameStrategy) {
        this(objectName, attribute, null, position, null, attribute, resultNameStrategy);
    }

    /**
     * @see #Query(String, String, String, Integer, String, String, ResultNameStrategy)
     */
    public Query(@Nonnull String objectName, @Nonnull String attribute, @Nonnull String resultAlias, @Nonnull ResultNameStrategy resultNameStrategy) {
        this(objectName, attribute, null, null, null, resultAlias, resultNameStrategy);
    }

    /**
     * @param objectName         The {@link javax.management.ObjectName} to search for
     *                           ({@link javax.management.MBeanServer#queryMBeans(javax.management.ObjectName, javax.management.QueryExp)}),
     *                           can contain wildcards and return several entries.
     * @param attribute          The attribute to retrieve ({@link javax.management.MBeanServer#getAttribute(javax.management.ObjectName, String)})
     * @param key                if the MBean attribute value is a {@link javax.management.openmbean.CompositeData}, the key to lookup.
     * @param position           if the returned value is a {@link java.util.Collection} or an array, the position of the entry to lookup.
     * @param type               type of the metric ('counter', 'gauge', ...)
     * @param resultAlias
     * @param resultNameStrategy the {@link ResultNameStrategy} used during the
     *                           {@link #collectAndExport(javax.management.MBeanServer, java.util.Queue)} phase.
     */
    public Query(
            @Nonnull String objectName,
            @Nonnull String attribute,
            @Nullable String key,
            @Nullable Integer position,
            @Nullable String type,
            @Nonnull String resultAlias,
            @Nonnull ResultNameStrategy resultNameStrategy) {
        try {
            this.objectName = new ObjectName(Preconditions2.checkNotNull(objectName));
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Invalid objectName '" + objectName + "'", e);
        }
        this.attribute = Preconditions2.checkNotNull(attribute);
        this.key = key;
        this.resultAlias = Preconditions2.checkNotNull(resultAlias);
        this.position = position;
        this.type = type;
        this.resultNameStrategy = Preconditions2.checkNotNull(resultNameStrategy, "resultNameStrategy");
    }

    public void collectAndExport(@Nonnull MBeanServer mbeanServer, @Nonnull Queue<QueryResult> resultQueue) {
        if (resultNameStrategy == null)
            throw new IllegalStateException("resultNameStrategy is not defined, query object is not properly initialized");

        Set<ObjectName> objectNames = mbeanServer.queryNames(objectName, null);

        for (ObjectName on : objectNames) {

            try {
                Object attributeValue = mbeanServer.getAttribute(on, attribute);

                processAttributeValues(on, attributeValue, resultQueue);
            } catch (AttributeNotFoundException e) {
                logCollectingException(on, e);
            } catch (MBeanException e) {
                logCollectingException(on, e);
            } catch (ReflectionException e) {
                logCollectingException(on, e);
            } catch (InstanceNotFoundException e) {
                logCollectingException(on, e);
            }
        }
    }

    private void processAttributeValues(
            @Nonnull ObjectName on,
            @Nullable Object attributeValue,
            @Nonnull Collection<QueryResult> resultQueue) {
        if (attributeValue == null) {
            // skip null values
            return;
        }

        if (attributeValue instanceof CompositeData && key == null) {
            logger.warning("Ignore compositeData without key specified for '" + on + "'#" + attribute + ": " + attributeValue);
            return;
        }

        if (!(attributeValue instanceof CompositeData) && key != null) {
            logger.warning("Ignore NON compositeData for specified key for '" + on + "'#" + attribute + "#" + key + ": " + attributeValue);
            return;
        }

        Object value;
        if (attributeValue instanceof CompositeData) {
            value = ((CompositeData) attributeValue).get(key);
        } else {
            value = attributeValue;
        }

        value = ArrayUtils.transformToListIfIsArray(value);

        String resultName = resultNameStrategy.getResultName(this, on, key);
        if (value instanceof Iterable) {
            Iterable iterable = (Iterable) value;
            if (position == null) {
                int idx = 0;
                for (Object entry : iterable) {
                    addResult(new QueryResult(resultName + "_" + idx++, type, entry, System.currentTimeMillis()), resultQueue);
                }
            } else {
                value = Iterables2.get(iterable, position);
                addResult(new QueryResult(resultName, type, value, System.currentTimeMillis()), resultQueue);
            }
        } else {
            addResult(new QueryResult(resultName, type, value, System.currentTimeMillis()), resultQueue);
        }
    }

    private void addResult(QueryResult queryResult, Collection<QueryResult> resultQueue) {
        try {
            resultQueue.add(queryResult);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Result queue is full, could not add query " + queryResult
                    + " further results for this query will be ignored.");
        }
    }

    private void logCollectingException(ObjectName on, Exception e) {
        logger.log(Level.WARNING, "Exception collecting " + on + "#" + attribute + (key == null ? "" : "#" + key), e);
    }

    @Override
    public String toString() {
        return "Query{" +
                "objectName=" + objectName +
                ", resultAlias='" + resultAlias + '\'' +
                ", attribute='" + attribute + '\'' +
                ", key='" + key + '\'' +
                '}';
    }

    @Nonnull
    public ObjectName getObjectName() {
        return objectName;
    }

    @Nonnull
    public String getResultAlias() {
        return resultAlias;
    }

    @Nonnull
    public String getAttribute() {
        return attribute;
    }

    @Nullable
    public String getKey() {
        return key;
    }

    @Nullable
    public Integer getPosition() {
        return position;
    }

    @Nullable
    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Query query = (Query) o;

        if (!attribute.equals(query.attribute)) return false;
        if (key != null ? !key.equals(query.key) : query.key != null) return false;
        if (!objectName.equals(query.objectName)) return false;
        if (position != null ? !position.equals(query.position) : query.position != null) return false;
        if (!resultAlias.equals(query.resultAlias)) return false;
        if (!resultNameStrategy.equals(query.resultNameStrategy)) return false;
        if (type != null ? !type.equals(query.type) : query.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = resultNameStrategy.hashCode();
        result = 31 * result + objectName.hashCode();
        result = 31 * result + resultAlias.hashCode();
        result = 31 * result + attribute.hashCode();
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
