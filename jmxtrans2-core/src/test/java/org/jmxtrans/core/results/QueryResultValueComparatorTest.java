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
package org.jmxtrans.core.results;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryResultValueComparatorTest {

    private QueryResultValueComparator comparator = new QueryResultValueComparator();

    @Test
    public void twoIsGreaterThanOne() {
        QueryResult resultOne = new QueryResult("one", 1, 0);
        QueryResult resultTwo = new QueryResult("two", 2, 0);
        assertThat(comparator.compare(resultOne, resultTwo)).isNegative();
    }

    @Test
    public void nullValuesAreEquals() {
        QueryResult resultOne = new QueryResult("one", null, 0);
        QueryResult resultTwo = new QueryResult("two", null, 0);
        assertThat(comparator.compare(resultOne, resultTwo)).isZero();
    }

    @Test
    public void nullValuesAreSmallerThanNonNull() {
        QueryResult resultOne = new QueryResult("one", null, 0);
        QueryResult resultTwo = new QueryResult("two", 1, 0);
        assertThat(comparator.compare(resultOne, resultTwo)).isNegative();
    }

    @Test
    public void nonNullValuesAreGreaterThanNull() {
        QueryResult resultOne = new QueryResult("one", 1, 0);
        QueryResult resultTwo = new QueryResult("two", null, 0);
        assertThat(comparator.compare(resultOne, resultTwo)).isPositive();
    }
}
