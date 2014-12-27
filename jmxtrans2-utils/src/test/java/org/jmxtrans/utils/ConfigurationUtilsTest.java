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

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationUtilsTest {

    @Test
    public void validIntegerIsParsed() {
        Map<String, String> settings = ImmutableMap.of("integerKey", "1");
        assertThat(ConfigurationUtils.getInt(settings, "integerKey")).isEqualTo(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidIntegerThrowsException() {
        Map<String, String> settings = ImmutableMap.of("integerKey", "hello");
        try {
            ConfigurationUtils.getInt(settings, "integerKey");
        } catch (IllegalArgumentException e) {
            assertThat(e)
                    .hasMessageContaining("hello")
                    .hasMessageContaining("integerKey")
                    .hasMessageContaining("is not an integer");
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonExistingIntegerThrowsException() {
        Map<String, String> settings = ImmutableMap.of();
        try {
            ConfigurationUtils.getInt(settings, "integerKey");
        } catch (IllegalArgumentException e) {
            assertThat(e)
                    .hasMessageContaining("No setting")
                    .hasMessageContaining("integerKey");
            throw e;
        }
    }

    @Test
    public void validIntegerWithDefaultValueIsParsed() {
        Map<String, String> settings = ImmutableMap.of("integerKey", "1");
        assertThat(ConfigurationUtils.getInt(settings, "integerKey", 2)).isEqualTo(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidIntegerWithDefaultValueThrowsException() {
        Map<String, String> settings = ImmutableMap.of("integerKey", "hello");
        try {
            ConfigurationUtils.getInt(settings, "integerKey", 1);
        } catch (IllegalArgumentException e) {
            assertThat(e)
                    .hasMessageContaining("hello")
                    .hasMessageContaining("integerKey")
                    .hasMessageContaining("is not an integer");
            throw e;
        }
    }

    @Test
    public void nonExistingIntegerReturnsDefaultValue() {
        Map<String, String> settings = ImmutableMap.of();
        assertThat(ConfigurationUtils.getInt(settings, "integerKey", 1)).isEqualTo(1);
    }

    @Test
    public void validLongWithDefaultValueIsParsed() {
        Map<String, String> settings = ImmutableMap.of("longKey", "1");
        assertThat(ConfigurationUtils.getLong(settings, "longKey", 2L)).isEqualTo(1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidLongWithDefaultValueThrowsException() {
        Map<String, String> settings = ImmutableMap.of("longKey", "hello");
        try {
            ConfigurationUtils.getLong(settings, "longKey", 1L);
        } catch (IllegalArgumentException e) {
            assertThat(e)
                    .hasMessageContaining("hello")
                    .hasMessageContaining("longKey")
                    .hasMessageContaining("is not a long");
            throw e;
        }
    }

    @Test
    public void nonExistingLongReturnsDefaultValue() {
        Map<String, String> settings = ImmutableMap.of();
        assertThat(ConfigurationUtils.getLong(settings, "longKey", 1L)).isEqualTo(1L);
    }

    @Test
    public void validBooleanWithDefaultValueIsParsed() {
        Map<String, String> settings = ImmutableMap.of("booleanKey", "true");
        assertThat(ConfigurationUtils.getBoolean(settings, "booleanKey", false)).isEqualTo(true);
    }

    @Test
    public void invalidBooleanWithDefaultValueReturnsFalse() {
        Map<String, String> settings = ImmutableMap.of("booleanKey", "hello");
        assertThat(ConfigurationUtils.getBoolean(settings, "booleanKey", true)).isEqualTo(false);
    }

    @Test
    public void nonExistingBooleanReturnsDefaultValue() {
        Map<String, String> settings = ImmutableMap.of();
        assertThat(ConfigurationUtils.getBoolean(settings, "booleanKey", true)).isEqualTo(true);
    }

    @Test
    public void existingStringIsFound() {
        Map<String, String> settings = ImmutableMap.of("stringKey", "hello");
        assertThat(ConfigurationUtils.getString(settings, "stringKey")).isEqualTo("hello");
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonExistingStringThrowsException() {
        Map<String, String> settings = ImmutableMap.of();
        try {
            ConfigurationUtils.getString(settings, "stringKey");
        } catch (IllegalArgumentException e) {
            assertThat(e)
                    .hasMessageContaining("No setting")
                    .hasMessageContaining("stringKey");
            throw e;
        }
    }

    @Test
    public void existingStringWithDefaultValueIsFound() {
        Map<String, String> settings = ImmutableMap.of("stringKey", "hello");
        assertThat(ConfigurationUtils.getString(settings, "stringKey", "default")).isEqualTo("hello");
    }

    @Test
    public void nonExistingStringWithDefaultValueReturnsDefaultValue() {
        Map<String, String> settings = ImmutableMap.of();
        assertThat(ConfigurationUtils.getString(settings, "stringKey", "default")).isEqualTo("default");
    }

}
