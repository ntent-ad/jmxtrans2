package org.jmxtrans.core.query;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jmxtrans.utils.mockito.MockitoTestNGListener;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Listeners(MockitoTestNGListener.class)
public class ResultNameStrategyTest {

    @Mock private Query query;

    private ResultNameStrategy resultNameStrategy;

    @BeforeMethod
    public void initResultNameStrategy() {
        resultNameStrategy = new ResultNameStrategy();
    }

    @Test
    public void defaultMetricNameIsTypeAndName() throws MalformedObjectNameException {
        Hashtable<String, String> properties = new Hashtable<>();
        properties.put("type", "BufferPool");
        properties.put("name", "direct");
        ObjectName objectName = new ObjectName("java.nio", properties);

        assertThat(resultNameStrategy.getResultName(query, objectName, QueryAttribute.builder("Count").build()))
                .isEqualTo("java.nio.BufferPool.direct.Count");
    }

    @Test
    public void defaultMetricNameIsTypeAndNameAndOtherAttributes() throws MalformedObjectNameException {
        Hashtable<String, String> properties = new Hashtable<>();
        properties.put("type", "BufferPool");
        properties.put("name", "direct");
        properties.put("other", "value");
        ObjectName objectName = new ObjectName("java.nio", properties);

        assertThat(resultNameStrategy.getResultName(query, objectName, QueryAttribute.builder("Count").build()))
                .isEqualTo("java.nio.BufferPool.direct.other__value.Count");
    }

    @Test
    public void typeButNoNameAttribute() throws MalformedObjectNameException {
        Hashtable<String, String> properties = new Hashtable<>();
        properties.put("type", "Memory");
        ObjectName objectName = new ObjectName("java.lang", properties);

        assertThat(resultNameStrategy.getResultName(query, objectName, QueryAttribute.builder("ObjectPendingFinalizationCount").build()))
                .isEqualTo("java.lang.Memory.ObjectPendingFinalizationCount");
    }
}
