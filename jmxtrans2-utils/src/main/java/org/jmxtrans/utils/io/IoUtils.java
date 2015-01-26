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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class IoUtils {

    private static final int COPY_BUFFER_SIZE = 512;

    private IoUtils() {
    }

    /**
     * Simple implementation without chunking if the source file is big.
     *
     * @param source
     * @param destination
     * @throws java.io.IOException
     */
    public static void doCopySmallFile(@Nonnull File source, @Nonnull File destination, boolean append) throws IOException {
        if (destination.exists() && destination.isDirectory()) {
            throw new IOException("Can not copy file, destination is a directory: " + destination.getAbsolutePath());
        } else if (!destination.exists()) {
            boolean renamed = source.renameTo(destination);
            if (renamed) return;
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(destination, append);
            if (append) {
                fos.write("\n".getBytes("UTF-8"));
            }
            fos.write(Files.readAllBytes(Paths.get(source.getAbsolutePath())));
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    public static void copy(@Nonnull InputStream in, @Nonnull OutputStream out) throws IOException {
        byte[] buffer = new byte[COPY_BUFFER_SIZE];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }
}
