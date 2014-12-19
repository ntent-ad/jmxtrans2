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

import org.jmxtrans.config.ConfigParser;
import org.jmxtrans.config.Interval;
import org.jmxtrans.config.OutputWriter;
import org.jmxtrans.config.PropertyPlaceholderResolver;
import org.jmxtrans.config.ResultNameStrategy;
import org.jmxtrans.config.XmlConfigParser;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * XML configuration parser.
 *
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class JmxTransExporterBuilder {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final PropertyPlaceholderResolver placeholderResolver = new PropertyPlaceholderResolver();

    public JmxTransExporter build(String configurationFilePath) throws ParserConfigurationException, SAXException, IOException {
        if (configurationFilePath == null) {
            throw new NullPointerException("configurationFilePath cannot be null");
        }

        ConfigParser configParser = new XmlConfigParser(
                placeholderResolver,
                loadDocument(configurationFilePath).getDocumentElement());

        // Collection interval
        Interval collectionInterval = configParser.parseInterval();
        if (collectionInterval == null) {
            collectionInterval = new Interval(10, TimeUnit.SECONDS);
        }
        // Result name strategy
        ResultNameStrategy resultNameStrategy = configParser.parseResultNameStrategy();
        if (resultNameStrategy == null) {
            resultNameStrategy = new ResultNameStrategyImpl();
        }
        Collection<OutputWriter> outputWriters = configParser.parseOutputWriters();
        if (outputWriters.size() == 0) {
            logger.warning("No outputwriter defined.");
        }

        return new JmxTransExporter()
                .withResultNameStrategy(resultNameStrategy)
                .withInvocations(configParser.parseInvocations())
                .withQueries(configParser.parseQueries(resultNameStrategy))
                .withOutputWriter(new OutputWritersChain(outputWriters))
                .withCollectInterval(collectionInterval);
    }

    private Document loadDocument(String configurationFilePath) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document;
        if (configurationFilePath.toLowerCase().startsWith("classpath:")) {
            String classpathResourcePath = configurationFilePath.substring("classpath:".length());
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathResourcePath);
            document = dBuilder.parse(in);
        } else if (configurationFilePath.toLowerCase().startsWith("file://") ||
                configurationFilePath.toLowerCase().startsWith("http://") ||
                configurationFilePath.toLowerCase().startsWith("https://")
                ) {
            URL url = new URL(configurationFilePath);
            document = dBuilder.parse(url.openStream());
        } else {
            File xmlFile = new File(configurationFilePath);
            if (!xmlFile.exists()) {
                throw new IllegalArgumentException("Configuration file '" + xmlFile.getAbsolutePath() + "' not found");
            }
            document = dBuilder.parse(xmlFile);
        }
        return document;
    }

}
