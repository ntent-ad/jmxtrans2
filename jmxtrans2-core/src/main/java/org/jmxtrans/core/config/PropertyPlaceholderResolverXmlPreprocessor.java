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
package org.jmxtrans.core.config;

import org.jmxtrans.utils.PropertyPlaceholderResolver;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import static org.w3c.dom.Node.ATTRIBUTE_NODE;
import static org.w3c.dom.Node.TEXT_NODE;

public class PropertyPlaceholderResolverXmlPreprocessor {

    private final PropertyPlaceholderResolver propertyPlaceholderResolver;


    public PropertyPlaceholderResolverXmlPreprocessor(PropertyPlaceholderResolver propertyPlaceholderResolver) {
        this.propertyPlaceholderResolver = propertyPlaceholderResolver;
    }

    public Document preprocess(Document document) {
        preprocessNode(document);
        return document;
    }

    private void preprocessNode(Node node) {
        switch (node.getNodeType()) {
            case ATTRIBUTE_NODE:
                Attr attributeNode = (Attr) node;
                attributeNode.setValue(propertyPlaceholderResolver.resolveString(attributeNode.getValue()));
                break;
            case TEXT_NODE:
                Text textNode = (Text) node;
                textNode.setData(propertyPlaceholderResolver.resolveString(textNode.getData()));
                break;
            default:
                NamedNodeMap attributes = node.getAttributes();
                if (attributes != null) {
                    for (int i = 0; i < attributes.getLength(); i++) {
                        preprocessNode(attributes.item(i));
                    }
                }
                NodeList children = node.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    preprocessNode(children.item(i));
                }
                break;
        }
    }
}
