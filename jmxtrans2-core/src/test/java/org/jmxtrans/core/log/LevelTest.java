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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jmxtrans.core.log.Level.DEBUG;
import static org.jmxtrans.core.log.Level.ERROR;
import static org.jmxtrans.core.log.Level.WARN;
import static org.jmxtrans.core.log.Level.INFO;

public class LevelTest {

    @Test
    public void debugEnablesAllLevels() {
        Level level = DEBUG;

        assertThat(level.isEnabled(DEBUG)).isTrue();
        assertThat(level.isEnabled(INFO)).isTrue();
        assertThat(level.isEnabled(WARN)).isTrue();
        assertThat(level.isEnabled(ERROR)).isTrue();
    }

    @Test
    public void infoLogsExceptDebug() {
        Level level = INFO;

        assertThat(level.isEnabled(DEBUG)).isFalse();
        assertThat(level.isEnabled(INFO)).isTrue();
        assertThat(level.isEnabled(WARN)).isTrue();
        assertThat(level.isEnabled(ERROR)).isTrue();
    }

    @Test
    public void errorOnlyEnablesErrorLevel() {
        Level level = ERROR;

        assertThat(level.isEnabled(DEBUG)).isFalse();
        assertThat(level.isEnabled(INFO)).isFalse();
        assertThat(level.isEnabled(WARN)).isFalse();
        assertThat(level.isEnabled(ERROR)).isTrue();
    }

}
