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
package org.jmxtrans.servlet;

import org.jmxtrans.utils.io.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jmxtrans.servlet.JmxTransLoaderListener.CONFIG_LOCATION_PARAM;
import static org.jmxtrans.servlet.JmxTransLoaderListener.SYSTEM_CONFIG_LOCATION_PARAM;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JmxTransLoaderListenerTest {

    @Mock private ServletContext context;

    @Test
    public void canLoadConfigFromWebContext() throws IOException {
        when(context.getInitParameter(CONFIG_LOCATION_PARAM)).thenReturn("classpath:org/jmxtrans/servlet/test.config");

        List<Resource> configurations = new JmxTransLoaderListener().getConfigurations(context);

        assertThat(configurations).isNotNull();
        assertThat(configurations).hasSize(1);
        Resource config = configurations.get(0);

        assertThat(config.getPath()).isEqualTo("classpath:org/jmxtrans/servlet/test.config");
        try (InputStream in = config.getInputStream()) {
            assertThat(in).hasContentEqualTo(new ByteArrayInputStream("hello world".getBytes("UTF-8")));
        }
    }

    @Test
    public void canLoadConfigFromSystemProperty() throws IOException {
        try {
            System.setProperty("jmxtrans.config", "classpath:org/jmxtrans/servlet/test.config");
            when(context.getInitParameter(SYSTEM_CONFIG_LOCATION_PARAM)).thenReturn("jmxtrans.config");

            List<Resource> configurations = new JmxTransLoaderListener().getConfigurations(context);

            assertThat(configurations).isNotNull();
            assertThat(configurations).hasSize(1);
            Resource config = configurations.get(0);

            assertThat(config.getPath()).isEqualTo("classpath:org/jmxtrans/servlet/test.config");
            try (InputStream in = config.getInputStream()) {
                assertThat(in).hasContentEqualTo(new ByteArrayInputStream("hello world".getBytes("UTF-8")));
            }
        } finally {
            System.getProperties().remove("jmxtrans.config");
        }
    }

    @Test
    public void multipleConfiurationsAreParsed() throws IOException {
        when(context.getInitParameter(CONFIG_LOCATION_PARAM)).thenReturn("config1, config2");

        List<Resource> configurations = new JmxTransLoaderListener().getConfigurations(context);

        assertThat(configurations).isNotNull();
        assertThat(configurations).hasSize(2);

        Resource config1 = configurations.get(0);
        assertThat(config1.getPath()).isEqualTo("config1");

        Resource config2 = configurations.get(1);
        assertThat(config2.getPath()).isEqualTo("config2");
    }
}
