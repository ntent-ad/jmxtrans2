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
import org.jmxtrans.output.OutputWriter;
import org.jmxtrans.query.embedded.Query;

import java.util.ArrayList;
import java.util.Collection;

public final class Config {
    private final Collection<OutputWriter> outputWriters = new ArrayList<>();
    private final Collection<Query> queries = new ArrayList<>();

    private Integer queryIntervalInSeconds;
    private Integer exportedBatchSize;
    private Integer numQueryThreads;
    private Integer exportIntervalInSeconds;
    private Integer numExportThreads;

    public void addOutputWriters(Collection<OutputWriter> outputWriters) {
        this.outputWriters.addAll(outputWriters);
    }

    public void addQuery(Query query) {
        this.queries.add(query);
    }

    public void setQueryIntervalInSeconds(Integer queryIntervalInSeconds) {
        this.queryIntervalInSeconds = queryIntervalInSeconds;
    }

    public void setExportedBatchSize(Integer exportedBatchSize) {
        this.exportedBatchSize = exportedBatchSize;
    }

    public void setNumQueryThreads(Integer numQueryThreads) {
        this.numQueryThreads = numQueryThreads;
    }

    public void setExportIntervalInSeconds(Integer exportIntervalInSeconds) {
        this.exportIntervalInSeconds = exportIntervalInSeconds;
    }

    public void setNumExportThreads(Integer numExportThreads) {
        this.numExportThreads = numExportThreads;
    }

    public void configure(EmbeddedJmxTrans embeddedJmxTrans) {
        for (Query query : queries) {
            embeddedJmxTrans.addQuery(query);
        }

        embeddedJmxTrans.getOutputWriters().addAll(outputWriters);

        if (queryIntervalInSeconds != null) {
            embeddedJmxTrans.setQueryIntervalInSeconds(queryIntervalInSeconds);
        }

        if (exportedBatchSize != null) {
            embeddedJmxTrans.setExportBatchSize(exportedBatchSize);
        }

        if (numQueryThreads != null) {
            embeddedJmxTrans.setNumQueryThreads(numQueryThreads);
        }

        if (exportIntervalInSeconds != null) {
            embeddedJmxTrans.setExportIntervalInSeconds(exportIntervalInSeconds);
        }

        if (numExportThreads != null) {
            embeddedJmxTrans.setNumExportThreads(numExportThreads);
        }
    }
}
