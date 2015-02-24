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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectNameFactoryTest {

    @Test
    public void objectNameIsCreatedWithAppropriateParams() throws MalformedObjectNameException {
        ObjectName objectName = new ObjectNameFactory("query").create("myQuery");

        assertThat(objectName.getDomain()).isEqualTo("org.jmxtrans");
        assertThat(objectName.getKeyProperty("id")).isEqualTo("query-0");
        assertThat(objectName.getKeyProperty("name")).isEqualTo("myQuery");
        assertThat(objectName.getKeyProperty("type")).isEqualTo("query");
    }

    @Test
    public void idIsIncrementedAtEachObjectCreation() throws MalformedObjectNameException {
        ObjectNameFactory objectNameFactory = new ObjectNameFactory("query");
        
        assertThat(objectNameFactory.create("myQuery").getKeyProperty("id")).isEqualTo("query-0");
        assertThat(objectNameFactory.create("myOtherQuery").getKeyProperty("id")).isEqualTo("query-1");
    }
    
    @Test
    public void objectNameIsAValidName() throws MalformedObjectNameException {
        ObjectNameFactory objectNameFactory = new ObjectNameFactory("query");
        
        objectNameFactory.create("test:type=*,name=PS Eden Space");
    }
}
