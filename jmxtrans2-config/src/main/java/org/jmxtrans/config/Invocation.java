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
package org.jmxtrans.config;

import org.jmxtrans.output.OutputWriter;
import org.jmxtrans.results.QueryResult;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
@Immutable
@ThreadSafe
public class Invocation {

    public final ObjectName objectName;
    public final String operationName;
    public final String resultAlias;
    private final Object[] params;
    private final String[] signature;
    private final Logger logger = Logger.getLogger(getClass().getName());

    public Invocation(String objectName, String operationName, Object[] params, String[] signature, String resultAlias) {
        try {
            this.objectName = new ObjectName(objectName);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Invalid objectName '" + objectName + "'", e);
        }
        this.operationName = operationName;
        this.params = params.clone();
        this.signature = signature.clone();
        this.resultAlias = resultAlias;
    }

    public void invoke(MBeanServer mbeanServer, OutputWriter outputWriter) {
        Set<ObjectName> objectNames = mbeanServer.queryNames(objectName, null);
        for (ObjectName on : objectNames) {
            try {
                Object result = mbeanServer.invoke(on, operationName, params, signature);
                outputWriter.write(new QueryResult(resultAlias, result, System.currentTimeMillis()));
            } catch (Exception e) {
                logger.log(Level.WARNING, "Exception invoking " + on + "#" + operationName + "(" + Arrays.toString(params) + ")", e);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Invocation that = (Invocation) o;

        if (objectName != null ? !objectName.equals(that.objectName) : that.objectName != null) return false;
        if (operationName != null ? !operationName.equals(that.operationName) : that.operationName != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(params, that.params)) return false;
        if (resultAlias != null ? !resultAlias.equals(that.resultAlias) : that.resultAlias != null) return false;
        if (!Arrays.equals(signature, that.signature)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = objectName != null ? objectName.hashCode() : 0;
        result = 31 * result + (operationName != null ? operationName.hashCode() : 0);
        result = 31 * result + (resultAlias != null ? resultAlias.hashCode() : 0);
        result = 31 * result + (params != null ? Arrays.hashCode(params) : 0);
        result = 31 * result + (signature != null ? Arrays.hashCode(signature) : 0);
        return result;
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
