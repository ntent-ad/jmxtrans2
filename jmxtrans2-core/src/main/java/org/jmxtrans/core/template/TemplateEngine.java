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

import static java.lang.String.format;

@ThreadSafe
public class TemplateEngine {
    @Nonnull private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Nonnull private final StringEscape stringEscape;
    @Nonnull private final Map<Character, ExpressionEvaluator> evaluators;

    private TemplateEngine(
            @Nonnull StringEscape stringEscape,
            @Nonnull Map<Character, ExpressionEvaluator> evaluators) {
        this.stringEscape = stringEscape;
        this.evaluators = evaluators;
    }
    
    public String evaluate(String expression) {
        StringBuilder result = new StringBuilder();

        int position = 0;
        while (position < expression.length()) {
            char c = expression.charAt(position);
            if (evaluators.keySet().contains(c)) {
                char expressionSeparator = c;
                ExpressionEvaluator evaluator = evaluators.get(c);
                int beginningSeparatorPosition = position;
                int endingSeparatorPosition = expression.indexOf(expressionSeparator, beginningSeparatorPosition + 1);
                if (endingSeparatorPosition == -1) {
                    throw new IllegalStateException("Invalid expression '" + expression + "', no ending '" + expressionSeparator + "' after beginning '" + expressionSeparator + "' at position " + beginningSeparatorPosition);
                }
                String key = expression.substring(beginningSeparatorPosition + 1, endingSeparatorPosition);
                stringEscape.escape(evaluator.evaluate(key), result);
                position = endingSeparatorPosition + 1;
            } else {
                result.append(c);
                position++;
            }
        }
        logger.debug(format("evaluate expression [%s] -> [%s]", expression, result));
        return result.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    @NotThreadSafe
    public static final class Builder {
        private final Map<Character, ExpressionEvaluator> evaluators = new HashMap<>();
        private boolean escapeDots = true;
        
        public Builder addEvaluator(Character namespace, ExpressionEvaluator evaluator) {
            evaluators.put(namespace, evaluator);
            return this;
        }

        public Builder escapeDots() {
            escapeDots = true;
            return this;
        }

        public Builder doNotEscapeDots() {
            escapeDots = false;
            return this;
        }

        public TemplateEngine build() {
            if (escapeDots) return new TemplateEngine(new KeepAlphaNumeric(), evaluators);
            return new TemplateEngine(new KeepAlphaNumericAndDots(), evaluators);
        }
    }
}
