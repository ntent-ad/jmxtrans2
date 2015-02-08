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
package org.jmxtrans.core.output.support;

import java.util.Arrays;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EvictingQueueTest {

    @Test(expected = IllegalArgumentException.class)
    public void creating_negative_evictingQueue_fails() throws Exception {
        EvictingQueue.create(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void creating_zeroSize_evictingQueue_fails() throws Exception {
        EvictingQueue.create(0);
    }

    @Test
    public void testEvictingAfterOne() throws Exception {
        EvictingQueue<String> queue = EvictingQueue.create(1);

        assertThat(queue).isEmpty();

        {
            boolean added = queue.add("one");
            assertThat(added).isTrue();
            assertThat(queue).hasSize(1);
            assertThat(queue.peek()).isEqualTo("one");
        }
        {
            boolean added = queue.add("two");
            assertThat(added).isTrue();
            assertThat(queue).hasSize(1);
            assertThat(queue.peek()).isEqualTo("two");
            String polled = queue.poll();
            assertThat(polled).isEqualTo("two");
            assertThat(queue).isEmpty();
        }
    }

    @Test
    public void testEvictingAfterThree() throws Exception {
        EvictingQueue<String> queue = EvictingQueue.create(3);
        assertThat(queue).isEmpty();

        {
            boolean added = queue.addAll(Arrays.asList("one", "two", "three"));
            assertThat(added).isTrue();
            assertThat(queue.element()).isEqualTo("one");
            assertThat(queue.peek()).isEqualTo("one");
            assertThat(queue).hasSize(3);
        }
        {
            boolean added = queue.addAll(Arrays.asList("four", "five"));
            assertThat(added).isTrue();
            assertThat(queue.peek()).isEqualTo("three");
            assertThat(queue).hasSize(3);
        }
        {
            String polled = queue.poll();
            assertThat(polled).isEqualTo("three");
            assertThat(queue).hasSize(2);
        }
    }

    public void method_addAll_behaves_as_multi_add() throws Exception {
        EvictingQueue<String> queue = EvictingQueue.create(3);
        assertThat(queue).isEmpty();

        {
            boolean added = queue.addAll(Arrays.asList("one", "two", "three"));
            assertThat(added).isTrue();
            assertThat(queue.element()).isEqualTo("one");
            assertThat(queue.peek()).isEqualTo("one");
            assertThat(queue).hasSize(3);
        }
        {
            boolean added = queue.addAll(Arrays.asList("four", "five"));
            assertThat(added).isTrue();
            assertThat(queue.peek()).isEqualTo("three");
            assertThat(queue).hasSize(3);
        }
        {
            String polled = queue.poll();
            assertThat(polled).isEqualTo("three");
            assertThat(queue).hasSize(2);
        }
    }
}
