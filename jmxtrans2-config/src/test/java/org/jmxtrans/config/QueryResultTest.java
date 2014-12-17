package org.jmxtrans.config;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

public class QueryResultTest {

    @Test
    public void twoIsGreaterThanOne() {
        QueryResult resultOne = new QueryResult("one", 1, 0);
        QueryResult resultTwo = new QueryResult("two", 2, 0);
        assertThat(resultTwo.isValueGreaterThan(resultOne)).isTrue();
    }


}
