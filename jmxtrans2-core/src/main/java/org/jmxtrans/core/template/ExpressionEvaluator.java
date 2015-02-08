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
package org.jmxtrans.core.template;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import org.jmxtrans.core.log.Logger;
import org.jmxtrans.core.log.LoggerFactory;

@ThreadSafe
public class ExpressionEvaluator {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    /** Function based evaluators for expressions like '#hostname#' or '#hostname_canonical#'. */
    @Nonnull private final Map<String, Expression> expressions;

    private ExpressionEvaluator(@Nonnull Map<String, Expression> expressions) {
        this.expressions = expressions;
    }

    @Nonnull
    public String evaluate(@Nonnull String key) {
        Expression expressionProcessor = expressions.get(key);
        if (expressionProcessor == null) {
            logger.info("Unsupported expression '" + key + "'");
            return "#unsupported_expression#";
        } else {
            try {
                return expressionProcessor.evaluate(key);
            } catch (Exception e) {
                logger.warn("Error evaluating expression '" + key + "'", e);
                return  "#expression_error#";
            }
        }
    }
    
    @Nonnull
    public static Builder builder() {
        return new Builder();
    }
    
    @NotThreadSafe
    public static final class Builder {
        @Nonnull private final Map<String, Expression> expressions = new HashMap<>();

        private Builder() {}
        
        @Nonnull
        public Builder addExpression(@Nonnull String key, @Nonnull Expression expression) {
            expressions.put(key, expression);
            return this;
        }

        @Nonnull
        public Builder addExpression(@Nonnull String key, @Nonnull String value) {
            return addExpression(key, new StaticExpression(value));
        }

        public Builder addExpressions(Map<String, String> expressions) {
            for (Map.Entry<String, String> expression : expressions.entrySet()) {
                addExpression(expression.getKey(), expression.getValue());
            }
            return this;
        }

        @Nonnull
        public ExpressionEvaluator build() {
            return new ExpressionEvaluator(expressions);
        }
    }

}
