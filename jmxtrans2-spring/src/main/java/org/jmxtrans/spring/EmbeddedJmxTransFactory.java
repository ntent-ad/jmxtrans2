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
package org.jmxtrans.spring;

import org.jmxtrans.core.config.JmxTransBuilder;
import org.jmxtrans.core.scheduler.NaiveScheduler;
import org.jmxtrans.utils.io.Resource;
import org.jmxtrans.utils.io.StandardResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link org.jmxtrans.spring.SpringEmbeddedJmxTrans} factory for Spring Framework integration.
 * <p/>
 * Default {@linkplain #configurationUrls} :
 * <ul>
 * <li><code>classpath:org.jmxtrans.json</code>: expected to be provided by the application</li>
 * <li><code>classpath:org/jmxtrans/embedded/config/jmxtrans-internals.json</code>: provided by jmxtrans jar for its internal monitoring</li>
 * </ul>
 *
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
@NotThreadSafe
public class EmbeddedJmxTransFactory implements FactoryBean<SpringEmbeddedJmxTrans>, BeanNameAware {

    private final static String DEFAULT_CONFIGURATION_URL = "classpath:org.jmxtrans.json, classpath:org/jmxtrans/embedded/config/jmxtrans-internals.json";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private List<String> configurationUrls;

    private final ResourceLoader resourceLoader;

    private boolean ignoreConfigurationNotFound = false;

    private String beanName = "jmxtrans";

    private SpringEmbeddedJmxTrans embeddedJmxTrans;

    @Autowired
    public EmbeddedJmxTransFactory(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public SpringEmbeddedJmxTrans getObject() throws Exception {
        logger.info("Load JmxTrans with configuration '{}'", configurationUrls);
        if (embeddedJmxTrans == null) {

            if (configurationUrls == null) {
                configurationUrls = Collections.singletonList(DEFAULT_CONFIGURATION_URL);
            }

            NaiveScheduler scheduler = new JmxTransBuilder(ignoreConfigurationNotFound,
                    Collections.<Resource>singletonList(new StandardResource(configurationUrls.get(0)))).build();

            embeddedJmxTrans = new SpringEmbeddedJmxTrans(scheduler);
            logger.info("Created EmbeddedJmxTrans with configuration {})", configurationUrls);
        }
        return embeddedJmxTrans;
    }

    @Override
    public Class<?> getObjectType() {
        return SpringEmbeddedJmxTrans.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * @param configurationUrl coma delimited list
     */
    public void setConfigurationUrl(String configurationUrl) {
        if (this.configurationUrls == null) {
            this.configurationUrls = new ArrayList<>();
        }
        this.configurationUrls.add(configurationUrl);
    }

    public void setConfigurationUrls(List<String> configurationUrls) {
        if (this.configurationUrls == null) {
            this.configurationUrls = new ArrayList<>();
        }
        this.configurationUrls.addAll(configurationUrls);
    }

    public void setIgnoreConfigurationNotFound(boolean ignoreConfigurationNotFound) {
        this.ignoreConfigurationNotFound = ignoreConfigurationNotFound;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
