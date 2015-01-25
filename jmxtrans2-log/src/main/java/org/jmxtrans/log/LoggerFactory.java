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
package org.jmxtrans.log;

public final class LoggerFactory {

    private LoggerFactory() {}

    private static final LogProvider provider = instantiateLogProvider();

    public static Logger getLogger(String name) {
        return provider.getLogger(name);
    }

    private static LogProvider instantiateLogProvider() {
        try {
            Class.forName("org.slf4j.LoggerFactory");
            return new Slf4JLogProvider();
        } catch (ClassNotFoundException e) {
            System.out.println("JmxTrans: SLF4J provider not on classpath, defaulting to console logging");
        }
        return new ConsoleLogProvider();
    }

}
