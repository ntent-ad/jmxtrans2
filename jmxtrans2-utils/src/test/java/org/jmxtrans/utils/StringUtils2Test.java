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

import java.util.Arrays;
import java.util.List;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class StringUtils2Test {

    @Test
    public void testDelimitedStringToList() {
        assertThat(StringUtils2.delimitedStringToList("a,b;c\nd,,e,;f"))
                .contains("a", "b", "c", "d", "e", "f");
    }

    @Test
    public void delimitedStringToListReturnsNullWhenArgumentIsNull() {
        assertThat(StringUtils2.delimitedStringToList(null)).isNull();
    }

    @Test
    public void testJoin() {
        List<String> tokens = Arrays.asList("com", "mycompany", "ecommerce", "server1");
        assertThat(StringUtils2.join(tokens, ".")).isEqualTo("com.mycompany.ecommerce.server1");
    }

    @Test
    public void joiningNullIsNull() {
        assertThat(StringUtils2.join(null, ".")).isEqualTo(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void joiningWithNullDelimiterIsNotAllowed() {
        StringUtils2.join(Arrays.asList("com", "mycompany"), null);
    }

    @Test
    public void testReverseTokens() {
        assertThat(StringUtils2.reverseTokens("server1.ecommerce.mycompany.com", "."))
                .isEqualTo("com.mycompany.ecommerce.server1");
    }

    @Test
    public void reversingNullTokensReturnsNull() {
        assertThat(StringUtils2.reverseTokens(null, ".")).isNull();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void reversingWithNullDelimiterIsNotAllowed() {
        try {
            StringUtils2.reverseTokens("com.mycompany", null);
        } catch (NullPointerException npe) {
            assertThat(npe).hasMessageContaining("given delimiter can not be null");
            throw npe;
        }
    }

    @Test
    public void nonAlphaNumericCharacters() {
        StringBuilder escaped = new StringBuilder();
        StringUtils2.appendEscapedNonAlphaNumericChars("abc._!", true, escaped);

        assertThat(escaped.toString()).isEqualTo("abc___");
    }

    @Test
    public void dotIsNotEscaped() {
        StringBuilder escaped = new StringBuilder();
        StringUtils2.appendEscapedNonAlphaNumericChars(".", false, escaped);

        assertThat(escaped.toString()).isEqualTo(".");
    }

    @Test
    public void dotIsEscaped() {
        StringBuilder escaped = new StringBuilder();
        StringUtils2.appendEscapedNonAlphaNumericChars(".", true, escaped);

        assertThat(escaped.toString()).isEqualTo("_");
    }

    @Test
    public void leadingAndTrailingQuotesAreIgnored() {
        StringBuilder escaped = new StringBuilder();
        StringUtils2.appendEscapedNonAlphaNumericChars("\"abc\"", true, escaped);

        assertThat(escaped.toString()).isEqualTo("abc");
    }
}
