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

import java.util.WeakHashMap;

import org.mockito.MockitoAnnotations;
import org.testng.IInvokedMethod;
import org.testng.ITestResult;

/**
 * based on https://github.com/mockito/mockito/tree/master/subprojects/testng/src/main/java/org/mockito/testng
 */
public class MockitoBeforeTestNGMethod {
    private WeakHashMap<Object, Boolean> initializedInstances = new WeakHashMap<Object, Boolean>();

    /**
     * Initialize mocks.
     *
     * @param method Invoked method.
     * @param testResult TestNG Test Result
     */
    public void applyFor(IInvokedMethod method, ITestResult testResult) {
        initMocks(testResult);
        reinitCaptors(method, testResult);
    }

    private void reinitCaptors(IInvokedMethod method, ITestResult testResult) {
        if (method.isConfigurationMethod()) {
            return;
        }
        initializeCaptors(testResult.getInstance());
    }

    private void initMocks(ITestResult testResult) {
        if (alreadyInitialized(testResult.getInstance())) {
            return;
        }
        MockitoAnnotations.initMocks(testResult.getInstance());
        markAsInitialized(testResult.getInstance());
    }

    private void initializeCaptors(Object instance) {
        /*
         * TODO Refactor #processAnnotationOn methods out of DefaultAnnotationEngine
         */
    }

    private void markAsInitialized(Object instance) {
        initializedInstances.put(instance, true);
    }

    private boolean alreadyInitialized(Object instance) {
        return initializedInstances.containsKey(instance);
    }
}
