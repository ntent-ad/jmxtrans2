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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Inspired by <code>com.google.common.base.Preconditions</code>
 *
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public final class Preconditions2 {

    private Preconditions2() {
    }

    @Nonnull
    @SuppressFBWarnings(value = "NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE", justification = "Don't understand why this is reported as bug...")
    public static String checkNotEmpty(@Nullable String str) {
        String internalSrt = requireNonNull(str);
        if (internalSrt.isEmpty())
            throw new IllegalArgumentException("Can not be empty");
        return internalSrt;
    }

    public static void checkArgument(boolean expression) throws IllegalArgumentException {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    public static void checkArgument(boolean expression, @Nullable String msg) throws IllegalArgumentException {
        if (!expression) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * @param expression
     * @param msgFormat
     * @param msgArgs
     * @throws IllegalArgumentException
     * @see String#format(String, Object...)
     */
    public static void checkArgument(boolean expression, @Nullable String msgFormat, @Nullable Object... msgArgs) throws IllegalArgumentException {
        if (!expression) {
            if (msgFormat == null) {
                throw new IllegalArgumentException();
            } else {
                throw new IllegalArgumentException(String.format(msgFormat, msgArgs));
            }
        }
    }
}
