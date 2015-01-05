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
package org.jmxtrans.embedded.config;


import org.jmxtrans.embedded.EmbeddedJmxTrans;
import org.jmxtrans.embedded.TestUtils;
import org.jmxtrans.query.embedded.Query;
import org.jmxtrans.query.embedded.QueryAttribute;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
public class ConfigurationMergeTest {

    static Map<String, Query> queriesByResultName;

    static EmbeddedJmxTrans embeddedJmxTrans;

    @BeforeClass
    public static void beforeClass() throws Exception {
        ConfigurationParser configurationParser = new ConfigurationParser();
        Config config = configurationParser.loadConfigurations(Arrays.asList(
                "classpath:org/jmxtrans/embedded/jmxtrans-config-merge-1-test.json",
                "classpath:org/jmxtrans/embedded/jmxtrans-config-merge-2-test.json"));
        embeddedJmxTrans = new EmbeddedJmxTrans();
        config.configure(embeddedJmxTrans);
        queriesByResultName = TestUtils.indexQueriesByAliasOrName(embeddedJmxTrans.getQueries());
    }

    @Test
    public void validateBasicQuery() throws MalformedObjectNameException {
        ObjectName objectName = new ObjectName("java.lang:type=MemoryPool,name=PS Eden Space");
        Query query = queriesByResultName.get(objectName.toString());
        assertThat(query.getObjectName()).isEqualTo(objectName);
        assertThat(query.getResultAlias()).isNull();
        assertThat(query.getQueryAttributes()).hasSize(1);
        QueryAttribute queryAttribute = query.getQueryAttributes().iterator().next();
        assertThat(queryAttribute.getName()).isEqualTo("CollectionUsageThreshold");
    }

    @Test
    public void validateAliasedQuery() throws MalformedObjectNameException {
        ObjectName objectName = new ObjectName("java.lang:type=MemoryPool,name=PS Eden Space");
        Query query = queriesByResultName.get("test-aliased-query");
        assertThat(query.getObjectName()).isEqualTo(objectName);
        assertThat(query.getResultAlias()).isEqualTo("test-aliased-query");
        assertThat(query.getQueryAttributes()).hasSize(1);
        QueryAttribute queryAttribute = query.getQueryAttributes().iterator().next();
        assertThat(queryAttribute.getName()).isEqualTo("CollectionUsageThresholdCount");
    }

    @Test
    public void validateQueryWithAttributeAlias() throws MalformedObjectNameException {
        ObjectName objectName = new ObjectName("java.lang:type=MemoryPool,name=PS Eden Space");
        Query query = queriesByResultName.get("test-attribute-with-alias");
        assertThat(query.getObjectName()).isEqualTo(objectName);
        assertThat(query.getResultAlias()).isEqualTo("test-attribute-with-alias");
        assertThat(query.getQueryAttributes()).hasSize(1);
        QueryAttribute queryAttribute = query.getQueryAttributes().iterator().next();
        assertThat(queryAttribute.getName()).isEqualTo("CollectionUsageThresholdCount");
        assertThat(queryAttribute.getResultAlias()).isEqualTo("test-alias");
    }

    @Test
    public void validateQueryWithAttributes() throws MalformedObjectNameException {
        ObjectName objectName = new ObjectName("java.lang:type=MemoryPool,name=PS Eden Space");
        Query query = queriesByResultName.get("test-attributes");
        assertThat(query.getObjectName()).isEqualTo(objectName);
        assertThat(query.getResultAlias()).isEqualTo("test-attributes");

        assertThat(query.getQueryAttributes()).hasSize(2);

        Map<String, QueryAttribute> queryAttributes = TestUtils.indexQueryAttributesByAliasOrName(query.getQueryAttributes());

        {
            QueryAttribute queryAttribute = queryAttributes.get("CollectionUsageThresholdExceeded");
            assertThat(queryAttribute.getName()).isEqualTo("CollectionUsageThresholdExceeded");
            assertThat(queryAttribute.getResultAlias()).isNull();
        }
        {
            QueryAttribute queryAttribute = queryAttributes.get("CollectionUsageThresholdSupported");
            assertThat(queryAttribute.getName()).isEqualTo("CollectionUsageThresholdSupported");
            assertThat(queryAttribute.getResultAlias()).isNull();
        }

    }

    @Test
    public void validateQueryWithAliasedAttributes() throws MalformedObjectNameException {
        ObjectName objectName = new ObjectName("java.lang:type=MemoryPool,name=PS Eden Space");
        Query query = queriesByResultName.get("test-attributes-with-alias");
        assertThat(query.getObjectName()).isEqualTo(objectName);
        assertThat(query.getResultAlias()).isEqualTo("test-attributes-with-alias");

        assertThat(query.getQueryAttributes()).hasSize(3);

        Map<String, QueryAttribute> queryAttributes = TestUtils.indexQueryAttributesByAliasOrName(query.getQueryAttributes());

        {
            QueryAttribute queryAttribute = queryAttributes.get("collection-usage-threshold-supported");
            assertThat(queryAttribute.getName()).isEqualTo("CollectionUsageThresholdSupported");
            assertThat(queryAttribute.getResultAlias()).isEqualTo("collection-usage-threshold-supported");
        }
        {
            QueryAttribute queryAttribute = queryAttributes.get("CollectionUsageThresholdCount");
            assertThat(queryAttribute.getName()).isEqualTo("CollectionUsageThresholdCount");
            assertThat(queryAttribute.getResultAlias()).isNull();
        }
        {
            QueryAttribute queryAttribute = queryAttributes.get("collection-usage-threshold-exceeded");
            assertThat(queryAttribute.getName()).isEqualTo("CollectionUsageThresholdExceeded");
            assertThat(queryAttribute.getResultAlias()).isEqualTo("collection-usage-threshold-exceeded");
        }
    }

    @Test
    public void validateQueryWithOutputWriter() throws MalformedObjectNameException {
        ObjectName objectName = new ObjectName("java.lang:type=MemoryPool,name=PS Eden Space");
        Query query = queriesByResultName.get("test-with-outputwriter");
        assertThat(query.getObjectName()).isEqualTo(objectName);
        assertThat(query.getResultAlias()).isEqualTo("test-with-outputwriter");
        assertThat(query.getQueryAttributes()).hasSize(1);
        QueryAttribute queryAttribute = query.getQueryAttributes().iterator().next();
        assertThat(queryAttribute.getName()).isEqualTo("CollectionUsageThresholdCount");
    }

    @Test
    public void validateTuningParameters() {
        assertThat(embeddedJmxTrans.getNumQueryThreads()).isEqualTo(3);
        assertThat(embeddedJmxTrans.getQueryIntervalInSeconds()).isEqualTo(5);
        assertThat(embeddedJmxTrans.getExportIntervalInSeconds()).isEqualTo(10);
        assertThat(embeddedJmxTrans.getNumExportThreads()).isEqualTo(2);
    }
}
