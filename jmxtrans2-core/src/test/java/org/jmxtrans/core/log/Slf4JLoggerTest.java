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
package org.jmxtrans.core.log;

import org.jmxtrans.utils.mockito.MockitoTestNGListener;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class Slf4JLoggerTest {

    @Mock private Logger slf4jLogger;
    
    @Test
    public void allMethodsAreDelegatedToSlf4J() {
        Slf4JLogger logger = new Slf4JLogger(slf4jLogger);
        Exception exception = new Exception();
        logger.isDebugEnabled();
        logger.debug("msg");
        logger.debug("msg", exception);
        logger.isInfoEnabled();
        logger.info("msg");
        logger.info("msg", exception);
        logger.isWarnEnabled();
        logger.warn("msg");
        logger.warn("msg", exception);
        logger.isErrorEnabled();
        logger.error("msg");
        logger.error("msg", exception);

        Mockito.verify(slf4jLogger).isDebugEnabled();
        Mockito.verify(slf4jLogger).debug("msg");
        Mockito.verify(slf4jLogger).debug("msg", exception);
        Mockito.verify(slf4jLogger).isInfoEnabled();
        Mockito.verify(slf4jLogger).info("msg");
        Mockito.verify(slf4jLogger).info("msg", exception);
        Mockito.verify(slf4jLogger).isWarnEnabled();
        Mockito.verify(slf4jLogger).warn("msg");
        Mockito.verify(slf4jLogger).warn("msg", exception);
        Mockito.verify(slf4jLogger).isErrorEnabled();
        Mockito.verify(slf4jLogger).error("msg");
        Mockito.verify(slf4jLogger).error("msg", exception);
    }

}
