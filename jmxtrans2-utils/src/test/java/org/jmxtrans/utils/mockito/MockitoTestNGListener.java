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
package org.jmxtrans.utils.mockito;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.annotations.Listeners;

/**
 * based on https://github.com/mockito/mockito/tree/master/subprojects/testng/src/main/java/org/mockito/testng
 */
public class MockitoTestNGListener implements IInvokedMethodListener {

    private MockitoBeforeTestNGMethod beforeTest = new MockitoBeforeTestNGMethod();
    private MockitoAfterTestNGMethod afterTest = new MockitoAfterTestNGMethod();

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (hasMockitoTestNGListenerInTestHierarchy(testResult.getTestClass().getRealClass())) {
            beforeTest.applyFor(method, testResult);
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (hasMockitoTestNGListenerInTestHierarchy(testResult.getTestClass().getRealClass())) {
            afterTest.applyFor(method, testResult);
        }
    }

    private boolean hasMockitoTestNGListenerInTestHierarchy(Class<?> testClass) {
        for (Class<?> clazz = testClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
            if (hasMockitoTestNGListener(clazz)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMockitoTestNGListener(Class<?> clazz) {
        Listeners listeners = clazz.getAnnotation(Listeners.class);
        if (listeners == null) {
            return false;
        }

        for (Class<? extends ITestNGListener> listenerClass : listeners.value()) {
            if (listenerClass() == listenerClass) {
                return true;
            }
        }
        return false;
    }

    private Class<MockitoTestNGListener> listenerClass() {
        return MockitoTestNGListener.class;
    }
}
