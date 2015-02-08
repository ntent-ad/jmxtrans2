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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.inOrder;

@RunWith(MockitoJUnitRunner.class)
public class ForwardingCollectionTest {
    
    @Mock private Collection<String> collection;
    
    @Test
    public void methodsAreDelegated() {
        InOrder inOrder = inOrder(collection);
        ForwardingCollection<String> forwardingCollection = new ForwardingCollection<String>() {
            @Nonnull
            @Override
            protected Collection<String> delegate() {
                return collection;
            }
        };
        
        List<String> emptyList = Collections.emptyList();

        forwardingCollection.size();
        forwardingCollection.isEmpty();
        forwardingCollection.contains("contains");
        forwardingCollection.iterator();
        forwardingCollection.toArray();
        forwardingCollection.toArray(new String[0]);
        forwardingCollection.add("add");
        forwardingCollection.remove("remove");
        forwardingCollection.containsAll(emptyList);
        forwardingCollection.addAll(emptyList);
        forwardingCollection.removeAll(emptyList);
        forwardingCollection.retainAll(emptyList);
        forwardingCollection.clear();

        inOrder.verify(collection).size();
        inOrder.verify(collection).isEmpty();
        inOrder.verify(collection).contains("contains");
        inOrder.verify(collection).iterator();
        inOrder.verify(collection).toArray();
        inOrder.verify(collection).toArray(new String[0]);
        inOrder.verify(collection).add("add");
        inOrder.verify(collection).remove("remove");
        inOrder.verify(collection).containsAll(emptyList);
        inOrder.verify(collection).addAll(emptyList);
        inOrder.verify(collection).removeAll(emptyList);
        inOrder.verify(collection).retainAll(emptyList);
        inOrder.verify(collection).clear();
    }
    
}
