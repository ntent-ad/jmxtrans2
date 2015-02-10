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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.exceptions.base.MockitoException;
import org.testng.IInvokedMethod;
import org.testng.ITestResult;

/**
 * based on https://github.com/mockito/mockito/tree/master/subprojects/testng/src/main/java/org/mockito/testng
 */
public class MockitoAfterTestNGMethod {
    public void applyFor(IInvokedMethod method, ITestResult testResult) {
        Mockito.validateMockitoUsage();

        if (method.isTestMethod()) {
            resetMocks(testResult.getInstance());
        }
    }


    private void resetMocks(Object instance) {
        Mockito.reset(instanceMocksOf(instance).toArray());
    }

    private Set<Object> instanceMocksOf(Object instance) {
        Class<?> testClass = instance.getClass();
        Set<Object> instanceMocks = new HashSet<>();

        for (Class<?> clazz = testClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
            instanceMocks.addAll(instanceMocksIn(instance, clazz));
        }
        return instanceMocks;
    }

    private Set<Object> instanceMocksIn(Object instance, Class<?> clazz) {
        Set<Object> instanceMocks = new HashSet<>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(Mock.class) || declaredField.isAnnotationPresent(Spy.class)) {
                declaredField.setAccessible(true);
                try {
                    Object fieldValue = declaredField.get(instance);
                    if (fieldValue != null) {
                        instanceMocks.add(fieldValue);
                    }
                } catch (IllegalAccessException e) {
                    throw new MockitoException("Could not access field " + declaredField.getName());
                }
            }
        }
        return instanceMocks;
    }
}
