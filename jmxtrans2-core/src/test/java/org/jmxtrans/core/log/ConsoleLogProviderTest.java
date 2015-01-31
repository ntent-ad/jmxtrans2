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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.junit.After;
import org.junit.Test;

import static org.jmxtrans.core.log.ConsoleLogProvider.JMXTRANS_LOG_LEVEL_PROP;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsoleLogProviderTest {

    private final ConsoleLogProvider consoleLogProvider = new ConsoleLogProvider();

    @Test
    public void defaultLogLevelIsWarn() {
        Logger logger = consoleLogProvider.getLogger("testLogger");

        assertThat(logger.isWarnEnabled()).isTrue();
        assertThat(logger.isInfoEnabled()).isFalse();
    }

    @Test
    public void logLevelCanBeSet() {
        System.setProperty(JMXTRANS_LOG_LEVEL_PROP, "debug");

        Logger logger = consoleLogProvider.getLogger("testLogger");

        assertThat(logger.isDebugEnabled()).isTrue();
    }

    @Test
    public void defaultingToWarnWhenLogLevelIsInvalid() {
        System.setProperty(JMXTRANS_LOG_LEVEL_PROP, "invalid");

        Logger logger = consoleLogProvider.getLogger("testLogger");

        assertThat(logger.isWarnEnabled()).isTrue();
        assertThat(logger.isInfoEnabled()).isFalse();
    }

    @Test
    public void messagesAreSentToStdOut() throws UnsupportedEncodingException {
        PrintStream backup = System.out;
        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(byteArray);
            System.setOut(out);

            Logger logger = consoleLogProvider.getLogger("testLogger");
            logger.error("errorMessage");

            assertThat(byteArray.toString("UTF-8")).contains("errorMessage");
        } finally {
            System.setOut(backup);
        }
    }

    @After
    public void resetJmxTransLogLevelEnv() {
        System.getProperties().remove(JMXTRANS_LOG_LEVEL_PROP);
    }

}
