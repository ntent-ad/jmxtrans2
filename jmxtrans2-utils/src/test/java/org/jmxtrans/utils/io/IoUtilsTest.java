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

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class IoUtilsTest {

    public static final String TEST_CONTENT = "Hello World";
    private Collection<File> testFiles = new ArrayList<File>();

    @Test(expected = IOException.class)
    public void cannotCopySmallFilesToDirectory() throws IOException {
        File destination = testDirectory();
        try {
            IoUtils.doCopySmallFile(testFile(TEST_CONTENT), destination, false);
        } catch (IOException ioe) {
            assertThat(ioe).hasMessage("Can not copy file, destination is a directory: " + destination.getAbsolutePath());
            throw ioe;
        }
    }

    @Test
    @Ignore("Current implementation does not copy files, but move them when destination does not exist")
    public void copyFileToNonExistingDestination() throws IOException {
        File source = testFile(TEST_CONTENT);
        File destination = nonExistingFile();
        IoUtils.doCopySmallFile(source, destination, false);

        assertThat(destination).exists();
        assertThat(destination).hasContent(TEST_CONTENT);
        // source should still exist
        assertThat(source).exists();
        assertThat(source).hasContent(TEST_CONTENT);
    }

    @Test
    public void copyToEmptyFile() throws IOException {
        File source = testFile(TEST_CONTENT);
        File destination = testFile("");
        IoUtils.doCopySmallFile(source, destination, false);

        assertThat(destination).exists();
        assertThat(destination).hasContent(TEST_CONTENT);
        // source should still exist
        assertThat(source).exists();
        assertThat(source).hasContent(TEST_CONTENT);
    }

    @Test
    public void copyToNonEmptyFile() throws IOException {
        File source = testFile(TEST_CONTENT);
        File destination = testFile("1234");
        IoUtils.doCopySmallFile(source, destination, false);

        assertThat(destination).exists();
        assertThat(destination).hasContent(TEST_CONTENT);
        // source should still exist
        assertThat(source).exists();
        assertThat(source).hasContent(TEST_CONTENT);
    }

    @Test
    public void appendToExistingFile() throws IOException {
        File source = testFile(TEST_CONTENT);
        File destination = testFile(TEST_CONTENT);
        IoUtils.doCopySmallFile(source, destination, true);

        assertThat(destination).exists();
        assertThat(destination).hasContent(TEST_CONTENT + "\n" + TEST_CONTENT);
        // source should still exist
        assertThat(source).exists();
        assertThat(source).hasContent(TEST_CONTENT);
    }

    @Nonnull
    private File nonExistingFile() throws IOException {
        File file = register(File.createTempFile(getClass().getSimpleName(), "file"));
        if (!file.delete()) throw new IOException("Cannot create test directory");
        return file;
    }

    @Nonnull
    private File testDirectory() throws IOException {
        File directory = register(File.createTempFile(getClass().getSimpleName(), "dir"));
        if (!directory.delete()) throw new IOException("Cannot create test directory");
        if (!directory.mkdir()) throw new IOException("Cannot create test directory");
        return directory;
    }

    @Nonnull
    private File testFile(@Nullable String content) throws IOException {
        File file = register(File.createTempFile(getClass().getSimpleName(), "file"));
        writeToFile(file, content);
        return file;
    }

    private void writeToFile(@Nonnull File file, @Nullable String content) throws IOException {
        if (content == null) {
            return;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(content.getBytes("UTF-8"));
        } finally {
            IoUtils.closeQuietly(fos);
        }
    }

    @Nonnull
    private File register(@Nonnull File file) {
        testFiles.add(file);
        return file;
    }

    @After
    public void cleanUpTestFiles() {
        for (File file : testFiles) {
            if (file.exists()) {
                file.delete();
            }
        }
    }
}