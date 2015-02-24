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

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.lang.management.ManagementFactory.getPlatformMBeanServer;

import static org.assertj.core.api.Assertions.assertThat;

public class MBeanRegistryTest {

    private MBeanRegistry registry;

    @BeforeMethod
    public void createRegistry() {
        registry = new MBeanRegistry(getPlatformMBeanServer());
    }

    @Test
    public void mBeanIsRegistered() throws MalformedObjectNameException, InstanceNotFoundException, InstanceAlreadyExistsException, MBeanRegistrationException {
        Counter counter = new Counter();
        registry.register(counter);

        registry.start();

        assertThat(getPlatformMBeanServer().isRegistered(counter.getObjectName())).isTrue();
    }
    
    @Test
    public void beanIsReturnedAfterRegistration() throws MalformedObjectNameException {
        Counter counter = new Counter();
        Counter registeredBean = registry.register(counter);

        assertThat(registeredBean == counter).isTrue();
    }

    @Test
    public void mBeanIsUnregistered() throws MalformedObjectNameException, InstanceNotFoundException, InstanceAlreadyExistsException, MBeanRegistrationException {
        Counter counter = new Counter();
        registry.register(counter);

        registry.start();

        assertThat(getPlatformMBeanServer().isRegistered(counter.getObjectName())).isTrue();
        
        registry.stop();

        assertThat(getPlatformMBeanServer().isRegistered(counter.getObjectName())).isFalse();
    }

    @Test
    public void registeringTwoBeansWithSameNameIsSilentyIgnored() throws MalformedObjectNameException, InstanceNotFoundException, InstanceAlreadyExistsException, MBeanRegistrationException {
        Counter counter1 = new Counter();
        Counter counter2 = new Counter();
        
        registry.register(counter1);
        registry.register(counter2);
        
        registry.start();
    }
    
    @AfterMethod
    public void stopRegistry() throws MBeanRegistrationException {
        registry.stop();
    }
}
