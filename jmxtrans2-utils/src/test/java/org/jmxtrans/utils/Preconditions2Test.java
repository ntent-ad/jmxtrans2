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
package org.jmxtrans.utils;

import java.util.Objects;

import org.junit.Test;

import static org.jmxtrans.utils.Preconditions2.checkArgument;
import static org.jmxtrans.utils.Preconditions2.checkNotEmpty;

import static org.assertj.core.api.Assertions.assertThat;

public class Preconditions2Test {

    @Test(expected = NullPointerException.class)
    public void checkNotNullThrowsExceptionOnNull() {
        Objects.requireNonNull(null);
    }

    @Test
    public void checkNotNullReturnsGivenObject() {
        Object argument = new Object();
        assertThat(Objects.requireNonNull(argument)).isEqualTo(argument);
    }

    @Test(expected = NullPointerException.class)
    public void checkNotNullThrowsExceptionWithMessage() {
        try {
            Objects.requireNonNull(null, "message");
        } catch (NullPointerException npe) {
            assertThat(npe).hasMessage("message");
            throw  npe;
        }
    }

    @Test(expected = NullPointerException.class)
    public void checkNotEmptyThrowsExceptionOnNullArgument() {
        checkNotEmpty(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkNotEmptyThrowsExceptionOnEmptyArgument() {
        checkNotEmpty("");
    }

    @Test
    public void checkNotEmptyReturnGivenArgument() {
        assertThat(checkNotEmpty("argument")).isEqualTo("argument");
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkArgumentThrowsExceptionOnFalse() {
        checkArgument(false);
    }

    @Test
    public void checkArgumentDoesNotThrowExceptionOnTrue() {
        checkArgument(true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkArgumentPublishMessageInException() {
        try {
            checkArgument(false, "message");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("message");
            throw  e;
        }
    }
}
