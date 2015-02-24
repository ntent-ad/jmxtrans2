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

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jmxtrans.core.log.Logger;
import org.jmxtrans.core.log.LoggerFactory;
import org.jmxtrans.core.results.QueryResult;
import org.jmxtrans.utils.time.Clock;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
@ThreadSafe
@EqualsAndHashCode(exclude = {"logger", "clock"})
@ToString(exclude = {"logger", "clock"})
public class Invocation {

    @Nonnull private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Nonnull private final ObjectName objectName;
    @Nonnull private final String operationName;
    @Nonnull private final String resultAlias;
    @Nonnull private final Object[] params;
    @Nonnull private final String[] signature;
    @Nonnull private final Clock clock;

    public Invocation(
            @Nonnull ObjectName objectName,
            @Nonnull String operationName,
            @Nonnull Object[] params,
            @Nonnull String[] signature,
            @Nonnull String resultAlias,
            @Nonnull Clock clock) {
        this.objectName = objectName;
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
}
