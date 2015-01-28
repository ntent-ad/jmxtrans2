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
package org.jmxtrans.core.monitoring;

import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.concurrent.ThreadSafe;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.jmxtrans.core.lifecycle.LifecycleAware;
import org.jmxtrans.core.log.Logger;
import org.jmxtrans.core.log.LoggerFactory;

import static java.lang.String.format;
import static java.util.Collections.synchronizedMap;

@ThreadSafe
public class MBeanRegistry implements LifecycleAware {
    
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    private final Map<Object, ObjectName> mBeans = synchronizedMap(new WeakHashMap<Object, ObjectName>());
    
    private final MBeanServer mBeanServer;

    public MBeanRegistry(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    public void register(SelfNamedMBean mBean) throws MalformedObjectNameException {
        register(mBean.getObjectName(), mBean);
    }
    
    public void register(ObjectName name, Object mBean) {
        mBeans.put(mBean, name);
    }
    
    @Override
    public void start() throws InstanceAlreadyExistsException, MBeanRegistrationException, InstanceNotFoundException {
        for (Map.Entry<Object, ObjectName> entry : mBeans.entrySet()) {
            Object mBean = entry.getKey();
            ObjectName name = entry.getValue();
            
            if (mBeanServer.isRegistered(name)) mBeanServer.unregisterMBean(name);
            
            try {
                logger.debug("Registering mbean " + name);
                mBeanServer.registerMBean(mBean, name);
            } catch (NotCompliantMBeanException e) {
                logger.error(format("MBean [%s], named [%s] is not compliant.", mBean.toString(), name), e);
            }
        }
    }
    
    @Override
    public void stop() throws MBeanRegistrationException {
        for (ObjectName name : mBeans.values()) {
            try {
                mBeanServer.unregisterMBean(name);
            } catch (InstanceNotFoundException ignore) {
            }
        }
    }
}
