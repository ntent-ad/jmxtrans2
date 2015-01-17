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
package org.jmxtrans.utils.io;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@ThreadSafe
public class StandardResource implements Resource {

    @Nonnull
    private final String path;

    public StandardResource(@Nonnull String path) {
        this.path = path;
    }

    @Override
    @Nonnull
    public String getPath() {
        return path;
    }

    @Override
    @Nonnull
    public InputStream getInputStream() throws IOException {
        if (path.toLowerCase().startsWith("classpath:")) {
            return getClasspathInputStream();
        } else if (path.toLowerCase().startsWith("file://") ||
                path.toLowerCase().startsWith("http://") ||
                path.toLowerCase().startsWith("https://")
                ) {
            return getURLInputStream();
        } else {
            return getFileInputStream();
        }
    }

    @Nonnull
    private InputStream getClasspathInputStream() throws IOException {
        String classpathResourcePath = path.substring("classpath:".length());
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathResourcePath);
        if (inputStream == null) {
            throw new IOException(String.format("Resource [%s] does not exist.", path));
        }
        return inputStream;
    }

    @Nonnull
    private FileInputStream getFileInputStream() throws FileNotFoundException {
        return new FileInputStream(new File(path));
    }

    @Nonnull
    private InputStream getURLInputStream() throws IOException {
        URL url = new URL(path);
        return url.openStream();
    }

}
