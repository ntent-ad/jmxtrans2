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

import org.jmxtrans.core.results.QueryResult;
import org.jmxtrans.log.Logger;
import org.jmxtrans.log.LoggerFactory;
import org.jmxtrans.utils.time.Clock;
import org.jmxtrans.utils.time.SystemClock;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import static java.util.Objects.hash;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
@Immutable
@ThreadSafe
public class Invocation {

    @Nonnull
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Nonnull
    public final ObjectName objectName;
    @Nonnull
    public final String operationName;
    @Nonnull
    public final String resultAlias;
    @Nonnull
    private final Object[] params;
    @Nonnull
    private final String[] signature;
    @Nonnull
    private final Clock clock;

    public Invocation(
            @Nonnull String objectName,
            @Nonnull String operationName,
            @Nonnull Object[] params,
            @Nonnull String[] signature,
            @Nonnull String resultAlias,
            @Nonnull SystemClock clock) {
        try {
            this.objectName = new ObjectName(objectName);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Invalid objectName '" + objectName + "'", e);
        }
        this.operationName = operationName;
        this.params = params.clone();
        this.signature = signature.clone();
        this.resultAlias = resultAlias;
        this.clock = clock;
    }

    public void invoke(@Nonnull MBeanServer mbeanServer, @Nonnull BlockingQueue<QueryResult> resultQueue) {
        Set<ObjectName> objectNames = mbeanServer.queryNames(objectName, null);
        for (ObjectName on : objectNames) {
            try {
                Object result = mbeanServer.invoke(on, operationName, params, signature);
                resultQueue.add(new QueryResult(resultAlias, result, clock.currentTimeMillis()));
            } catch (Exception e) {
                logger.warn("Exception invoking " + on + "#" + operationName + "(" + Arrays.toString(params) + ")", e);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Invocation that = (Invocation) o;
        return Objects.equals(objectName, that.objectName)
                && Objects.equals(operationName, that.operationName)
                && Objects.deepEquals(params, that.params)
                && Objects.deepEquals(signature, that.signature)
                && Objects.equals(resultAlias, that.resultAlias);

    }

    @Override
    public int hashCode() {
        return hash(objectName, operationName, params, signature, resultAlias);
    }

    @Override
    public String toString() {
        return "Invocation{" +
                "objectName=" + objectName +
                ", operationName='" + operationName + '\'' +
                ", resultAlias='" + resultAlias + '\'' +
                ", params=" + Arrays.toString(params) +
                ", signature=" + Arrays.toString(signature) +
                '}';
    }
}
