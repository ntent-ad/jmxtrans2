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

import org.jmxtrans.utils.time.ManualClock;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.lang.System.lineSeparator;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;

import static org.jmxtrans.core.log.Level.DEBUG;
import static org.jmxtrans.core.log.Level.ERROR;
import static org.jmxtrans.core.log.Level.INFO;
import static org.jmxtrans.core.log.Level.WARN;
import static org.jmxtrans.utils.io.Charsets.UTF_8;

import static org.assertj.core.api.Assertions.assertThat;

public class PrintWriterLoggerTest {

    private ManualClock clock = new ManualClock();
    private ByteArrayOutputStream byteArray;
    private PrintStream out;

    @BeforeMethod
    public void initializeClock() {
        clock.setTime(1000, SECONDS);
        byteArray = new ByteArrayOutputStream();
        out = new PrintStream(byteArray);
    }

    @Test
    public void debugLoggerLogsAtAllLevels() throws UnsupportedEncodingException {
        PrintWriterLogger logger = createLogger(DEBUG);
        assertThat(logger.getName()).isEqualTo("testLogger");
        logger.debug("debugMessage");
        logger.info("infoMessage");
        logger.warn("warnMessage");
        logger.error("errorMessage");
        assertThat(byteArray.toString(UTF_8.name()))
                .contains("debugMessage")
                .contains("infoMessage")
                .contains("warnMessage")
                .contains("errorMessage");
    }

    @Test
    public void infoLoggerLogsAtAppropriateLevels() throws UnsupportedEncodingException {
        PrintWriterLogger logger = createLogger(INFO);
        logger.debug("debugMessage");
        logger.info("infoMessage");
        logger.warn("warnMessage");
        logger.error("errorMessage");
        assertThat(byteArray.toString(UTF_8.name()))
                .doesNotContain("debugMessage")
                .contains("infoMessage")
                .contains("warnMessage")
                .contains("errorMessage");
    }

    @Test
    public void warnLoggerLogsAtAppropriateLevels() throws UnsupportedEncodingException {
        PrintWriterLogger logger = createLogger(WARN);
        logger.debug("debugMessage");
        logger.info("infoMessage");
        logger.warn("warnMessage");
        logger.error("errorMessage");
        assertThat(byteArray.toString(UTF_8.name()))
                .doesNotContain("debugMessage")
                .doesNotContain("infoMessage")
                .contains("warnMessage")
                .contains("errorMessage");
    }

    @Test
    public void errorDebuggerOnlyLogsAtErrorLevel() throws UnsupportedEncodingException {
        PrintWriterLogger logger = createLogger(ERROR);
        logger.debug("debugMessage");
        logger.info("infoMessage");
        logger.warn("warnMessage");
        logger.error("errorMessage");
        assertThat(byteArray.toString(UTF_8.name()))
                .doesNotContain("debugMessage")
                .doesNotContain("infoMessage")
                .doesNotContain("warnMessage")
                .contains("errorMessage");
    }

    @Test
    public void logLineContainsAllRequiredInformation() throws UnsupportedEncodingException {
        Logger logger = createLogger(DEBUG);
        logger.debug("testMessage");
        assertThat(byteArray.toString(UTF_8.name()))
                .contains("testMessage")
                .contains(DEBUG.toString())
                .contains("testLogger")
                .matches(compile(".*1970\\.01\\.01 ..:16:40\\.000.*", DOTALL)) // ignore hours to not test timezone
                .endsWith(lineSeparator());
    }

    @Test
    public void logLineContainsException() throws UnsupportedEncodingException {
        Logger logger = createLogger(DEBUG);
        logger.debug("testMessage", new Exception("myException"));
        assertThat(byteArray.toString(UTF_8.name()))
                .contains("testMessage")
                .contains(DEBUG.toString())
                .contains("testLogger")
                .matches(compile(".*1970\\.01\\.01 ..:16:40\\.000.*", DOTALL)) // ignore hours to not test timezone
                .contains("myException")
                .endsWith(lineSeparator());
    }

    @Test
    public void logLineContainsLogLevel() throws UnsupportedEncodingException {
        Logger logger = createLogger(DEBUG);

        logger.debug("debugMessage");
        assertThat(byteArray.toString(UTF_8.name())).contains(DEBUG.toString());
        byteArray.reset();

        logger.info("infoMessage");
        assertThat(byteArray.toString(UTF_8.name())).contains(INFO.toString());
        byteArray.reset();

        logger.warn("warnMessage");
        assertThat(byteArray.toString(UTF_8.name())).contains(WARN.toString());
        byteArray.reset();

        logger.error("errorMessage");
        assertThat(byteArray.toString(UTF_8.name())).contains(ERROR.toString());
        byteArray.reset();
    }

    private PrintWriterLogger createLogger(Level level) {
        return new PrintWriterLogger("testLogger", level, clock, out);
    }

    @AfterMethod
    public void closePrintWriter() {
        out.close();
    }

}
