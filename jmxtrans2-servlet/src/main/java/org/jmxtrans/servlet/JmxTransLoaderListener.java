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

import org.jmxtrans.core.config.JmxTransBuilder;
import org.jmxtrans.core.config.JmxTransException;
import org.jmxtrans.core.scheduler.NaiveScheduler;
import org.jmxtrans.utils.VisibleForTesting;
import org.jmxtrans.utils.io.Resource;
import org.jmxtrans.utils.io.StandardResource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Bootstrap listener to start up and shut down JmxTrans.
 * <p/>
 * {@code embedded-jmxtrans} configuration files are specified by a coma/line-break
 * delimited list of jmxtrans json configuration file declared in the {@code web.xml}
 * {@code &lt;context-param&gt;} element named "{@value #CONFIG_LOCATION_PARAM}".
 * <p/>
 * Sample:
 * <code><pre>
 * &lt;web-app ...&gt;
 *   &lt;context-param&gt;
 *     &lt;param-name&gt;org.jmxtrans.config&lt;/param-name&gt;
 *     &lt;param-value&gt;
 *       classpath:orgjmxtrans.json
 *       classpath:org/jmxtrans/embedded/config/jmxtrans-internals-servlet-container.json
 *       classpath:org/jmxtrans/embedded/config/tomcat-7.json
 *       classpath:org/jmxtrans/embedded/config/jvm-sun-hotspot.json
 *     &lt;/param-value&gt;
 *   &lt;/context-param&gt;
 *   &lt;listener&gt;
 *     &lt;listener-class&gt;org.jmxtrans.embedded.servlet.EmbeddedJmxTransLoaderListener&lt;/listener-class&gt;
 *   &lt;/listener&gt;
 * &lt;/web-app&gt;
 * </pre>
 * </code>
 *
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
public class JmxTransLoaderListener implements ServletContextListener {

    private final Pattern CONFIG_SPLIT = Pattern.compile("[\n\r,]");

    /**
     * Config param for the embedded-jmxtrans configuration urls.
     */
    public static final String CONFIG_LOCATION_PARAM = "org.jmxtrans.config";
    public static final String SYSTEM_CONFIG_LOCATION_PARAM = "org.jmxtrans.system.config";
    private NaiveScheduler scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().log("Start embedded-jmxtrans ...");

        try {
            scheduler = new JmxTransBuilder(false, getConfigurations(sce.getServletContext())).build();
            scheduler.start();
        } catch (Exception e) {
            String message = "Exception starting jmxtrans for application '" + sce.getServletContext().getContextPath() + "'";
            sce.getServletContext().log(message, e);
            throw new JmxTransException(message, e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().log("Stop embedded-jmxtrans ...");

        try {
            scheduler.stop();
        } catch (Exception e) {
            throw new JmxTransException("Exception stopping JmxTrans", e);
        }
    }

    @VisibleForTesting
    List<Resource> getConfigurations(ServletContext servletContext) {
        List<Resource> configurations = configureFromSystemProperty(servletContext);
        if (!configurations.isEmpty()) return configurations;

        configurations = configureFromWebXmlParam(servletContext);
        if (!configurations.isEmpty()) return configurations;

        return parseStringConfigurations("classpath:jmxtrans.json, classpath:org/jmxtrans/embedded/config/jmxtrans-internals-servlet-container.json");
    }

    @Nonnull
    private List<Resource> configureFromSystemProperty(ServletContext servletContext){
        String configSystemProperty = servletContext.getInitParameter(SYSTEM_CONFIG_LOCATION_PARAM);

        if (configSystemProperty == null || configSystemProperty.isEmpty()) return Collections.emptyList();

        String prop = System.getProperty(configSystemProperty);
        return parseStringConfigurations(prop);
    }

    @Nonnull
    private List<Resource> configureFromWebXmlParam(ServletContext servletContext){
        return parseStringConfigurations(servletContext.getInitParameter(CONFIG_LOCATION_PARAM));
    }

    @Nonnull
    private List<Resource> parseStringConfigurations(@Nullable String configurations) {
        if (configurations == null) return Collections.emptyList();

        List<Resource> results = new ArrayList<>();
        for (String configuration : CONFIG_SPLIT.split(configurations)) {
            String trimmed = configuration.trim();
            if (!trimmed.isEmpty()) {
                results.add(new StandardResource(trimmed));
            }
        }
        return results;
    }
}
