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
package org.jmxtrans.core.query;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.management.ObjectName;

import org.jmxtrans.core.log.Logger;
import org.jmxtrans.core.log.LoggerFactory;
import org.jmxtrans.core.template.ExpressionEvaluator;
import org.jmxtrans.core.template.KeepAlphaNumericAndDots;
import org.jmxtrans.core.template.StringEscape;
import org.jmxtrans.core.template.TemplateEngine;
import org.jmxtrans.utils.StringUtils2;

import static java.lang.String.format;
import static java.util.Collections.list;
import static java.util.Collections.sort;

public class ResultNameStrategy {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Nonnull private final ExpressionEvaluator expressionEvaluator;
    @Nonnull private final StringEscape stringEscape;

    public ResultNameStrategy() {
        ExpressionEvaluator.Builder evaluatorsBuilder = ExpressionEvaluator.builder();
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String hostName = localHost.getHostName();
            String reversedHostName = StringUtils2.reverseTokens(hostName, ".");
            String canonicalHostName = localHost.getCanonicalHostName();
            String reversedCanonicalHostName = StringUtils2.reverseTokens(canonicalHostName, ".");
            String hostAddress = localHost.getHostAddress();

            evaluatorsBuilder
                    .addExpression("hostname", hostName)
                    .addExpression("reversed_hostname", reversedHostName)
                    .addExpression("escaped_hostname", hostName.replaceAll("\\.", "_"))
                    .addExpression("canonical_hostname", canonicalHostName)
                    .addExpression("reversed_canonical_hostname", reversedCanonicalHostName)
                    .addExpression("escaped_canonical_hostname", canonicalHostName.replaceAll("\\.", "_"))
                    .addExpression("hostaddress", hostAddress)
                    .addExpression("escaped_hostaddress", hostAddress.replaceAll("\\.", "_"));
        } catch (Exception e) {
            logger.error("Exception resolving localhost, expressions like #hostname#, #canonical_hostname# or #hostaddress# will not be available", e);
        }
        expressionEvaluator = evaluatorsBuilder.build();
        stringEscape = new KeepAlphaNumericAndDots();
    }

    @Nonnull
    public String getResultName(@Nonnull Query query, @Nonnull ObjectName objectName, @Nonnull QueryAttribute queryAttribute) {

        StringBuilder result = _getResultName(query, objectName, queryAttribute);

        return result.toString();
    }

    @Nonnull
    public String getResultName(@Nonnull Query query, @Nonnull ObjectName objectName, @Nonnull QueryAttribute queryAttribute, @Nonnull String key) {
        StringBuilder result = _getResultName(query, objectName, queryAttribute);
        result.append(".");
        result.append(key);
        return result.toString();
    }

    @Nonnull
    private StringBuilder _getResultName(@Nonnull Query query, @Nonnull ObjectName objectName, @Nonnull QueryAttribute queryAttribute) {
        StringBuilder result = new StringBuilder();

        String queryName;
        if (query.getResultAlias() == null) {
            queryName = escapeObjectName(objectName);
        } else {
            queryName = resolveExpression(query.getResultAlias(), objectName);
        }

        if (queryName != null && !queryName.isEmpty()) {
            result.append(queryName).append(".");
        }

        String attributeName;
        if (queryAttribute.getResultAlias() == null) {
            attributeName = queryAttribute.getName();
        } else {
            attributeName = queryAttribute.getResultAlias();
        }
        result.append(attributeName);
        return result;
    }

    @Nonnull
    public String resolveExpression(@Nonnull String expression, @Nonnull ObjectName exactObjectName) {
        return TemplateEngine.builder()
                .addEvaluator('%', ExpressionEvaluator.builder()
                        .addExpressions(exactObjectName.getKeyPropertyList())
                        .build())
                .addEvaluator('#', expressionEvaluator)
                .doNotEscapeDots()
                .build()
                .evaluate(expression);
    }

    /**
     * Transforms an {@linkplain javax.management.ObjectName} into a plain {@linkplain String} only composed of (a->Z, A-Z, '_').
     * <p/>
     * '_' is the escape char for not compliant chars.
     */
    @Nonnull
    private String escapeObjectName(@Nonnull ObjectName objectName) {
        StringBuilder result = new StringBuilder();
        stringEscape.escape(objectName.getDomain(), result);

        List<String> keys = list(objectName.getKeyPropertyList().keys());
        sort(keys);

        // Treat "type" and "name" attributes in special way:
        // do not write key, only values. This will result in metric like "java.lang.Memory" instead of "java.lang.type__Memory"
        String type = objectName.getKeyProperty("type");
        String name = objectName.getKeyProperty("name");
        if(type != null && type.length() > 0) {
            result.append('.');
            result.append(type);
        }
        if(name != null && name.length()> 0) {
            result.append('.');
            result.append(name);
        }

        for (Iterator<String> it = keys.iterator(); it.hasNext(); ) {
            String key = it.next();
            if(key.equalsIgnoreCase("type") || key.equalsIgnoreCase("name"))
                continue;

            result.append('.');
            stringEscape.escape(key, result);
            result.append("__");
            stringEscape.escape(objectName.getKeyProperty(key), result);
        }
        logger.debug(format("escapeObjectName(%s): %s", objectName, result));
        return result.toString();
    }
}
