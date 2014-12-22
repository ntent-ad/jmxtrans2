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

import org.jmxtrans.config.OutputWriter;
import org.jmxtrans.config.QueryResult;
import org.jmxtrans.output.AbstractOutputWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class OutputWritersChain extends AbstractOutputWriter implements OutputWriter {

    // visible for testing
    final List<OutputWriter> outputWriters;

    public OutputWritersChain() {
        outputWriters = new ArrayList<OutputWriter>();
    }

    public OutputWritersChain(Collection<OutputWriter> outputWriters) {
        this.outputWriters = new ArrayList<OutputWriter>(outputWriters.size());
        this.outputWriters.addAll(outputWriters);
    }

    @Override
    public void write(QueryResult result) throws IOException {
        for (OutputWriter outputWriter : outputWriters) {
            outputWriter.write(result);
        }
    }

    @Override
    public void preDestroy() {
        for (OutputWriter outputWriter : outputWriters) {
            outputWriter.preDestroy();
        }
    }

    @Override
    public void postCollect() throws IOException {
        for (OutputWriter outputWriter : outputWriters) {
            outputWriter.postCollect();
        }
    }

    @Override
    public void preCollect() throws IOException {
        for (OutputWriter outputWriter : outputWriters) {
            outputWriter.preCollect();
        }
    }
}
