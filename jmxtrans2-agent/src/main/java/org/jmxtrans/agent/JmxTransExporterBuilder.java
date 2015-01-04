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
package org.jmxtrans.agent;

import org.jmxtrans.config.Configuration;
import org.jmxtrans.config.XmlConfigParser;
import org.jmxtrans.utils.PropertyPlaceholderResolver;
import org.jmxtrans.utils.time.SystemClock;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * XML configuration parser.
 *
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class JmxTransExporterBuilder {

    @Nonnull
    public JmxTransExporter build(@Nonnull String configurationFilePath) throws ParserConfigurationException, SAXException, IOException {
        XmlConfigParser configParser = new XmlConfigParser(
                new PropertyPlaceholderResolver(),
                new SystemClock());
        configParser.setConfiguration(loadDocument(configurationFilePath));

        return createJmxTransExporter(configParser.parseConfiguration());
    }

    protected JmxTransExporter createJmxTransExporter(Configuration configuration) {
        return new JmxTransExporter(configuration);
    }

    @Nonnull
    private Document loadDocument(@Nonnull String configurationFilePath) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        try (InputStream in = getInputStream(configurationFilePath)) {
            return dBuilder.parse(in);
        }
    }

    @Nonnull
    private InputStream getInputStream(@Nonnull String configurationFilePath) throws IOException {
        if (configurationFilePath.toLowerCase().startsWith("classpath:")) {
            return getClasspathInputStream(configurationFilePath);
        } else if (configurationFilePath.toLowerCase().startsWith("file://") ||
                configurationFilePath.toLowerCase().startsWith("http://") ||
                configurationFilePath.toLowerCase().startsWith("https://")
                ) {
            return getURLInputStream(configurationFilePath);
        } else {
            return getFileInputStream(configurationFilePath);
        }
    }

    @Nonnull
    private FileInputStream getFileInputStream(@Nonnull String configurationFilePath) throws FileNotFoundException {
        File xmlFile = new File(configurationFilePath);
        if (!xmlFile.exists()) {
            throw new IllegalArgumentException("Configuration file '" + xmlFile.getAbsolutePath() + "' not found");
        }
        return new FileInputStream(xmlFile);
    }

    @Nonnull
    private InputStream getURLInputStream(@Nonnull String configurationFilePath) throws IOException {
        URL url = new URL(configurationFilePath);
        return url.openStream();
    }

    @Nonnull
    private InputStream getClasspathInputStream(@Nonnull String configurationFilePath) {
        String classpathResourcePath = configurationFilePath.substring("classpath:".length());
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathResourcePath);
    }

}
