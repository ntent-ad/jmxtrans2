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
package org.jmxtrans.utils.concurrent;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NamedThreadFactoryTest {

    public static final String PREFIX = "prefix";

    @Test
    public void newThreadIsNamedAccordingToPrefix() {
        NamedThreadFactory namedThreadFactory = new NamedThreadFactory(PREFIX, true);

        Thread t1 = namedThreadFactory.newThread(new Thread());

        assertThat(t1.getName()).isEqualTo(PREFIX + 1);

        Thread t2 = namedThreadFactory.newThread(new Thread());

        assertThat(t2.getName()).isEqualTo(PREFIX + 2);
    }

    @Test
    public void createDaemonThread() {
        NamedThreadFactory namedThreadFactory = new NamedThreadFactory(PREFIX, true);

        Thread thread = namedThreadFactory.newThread(new Thread());

        assertThat(thread.isDaemon()).isTrue();
    }

    @Test
    public void createNonDaemonThread() {
        NamedThreadFactory namedThreadFactory = new NamedThreadFactory(PREFIX, false);

        Thread thread = namedThreadFactory.newThread(new Thread());

        assertThat(thread.isDaemon()).isFalse();
    }

}
