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

import java.util.Queue;

import javax.annotation.Nonnull;

import org.jmxtrans.utils.mockito.MockitoTestNGListener;

import org.mockito.InOrder;
import org.mockito.Mock;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.inOrder;

@Listeners(MockitoTestNGListener.class)
public class ForwardingQueueTest {
    
    @Mock private Queue<String> queue;

    @Test
    public void methodsAreDelegated() {
        InOrder inOrder = inOrder(queue);
        ForwardingQueue<String> forwardingQueue = new ForwardingQueue<String>() {
            @Nonnull
            @Override
            protected Queue<String> delegate() {
                return queue;
            }
        };

        forwardingQueue.add("add");
        forwardingQueue.offer("offer");
        forwardingQueue.remove();
        forwardingQueue.poll();
        forwardingQueue.element();
        forwardingQueue.peek();
        
        inOrder.verify(queue).add("add");
        inOrder.verify(queue).offer("offer");
        inOrder.verify(queue).remove();
        inOrder.verify(queue).poll();
        inOrder.verify(queue).element();
        inOrder.verify(queue).peek();
    }
    
}
