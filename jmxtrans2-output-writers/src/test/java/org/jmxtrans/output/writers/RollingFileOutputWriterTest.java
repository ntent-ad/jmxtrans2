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
package org.jmxtrans.output.writers;

import org.jmxtrans.utils.DummyFiles;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class RollingFileOutputWriterTest {

    public static final String TEST_CONTENT = "Hello World";

    private final DummyFiles dummyFiles = new DummyFiles();

    @Test(expected = IOException.class)
    public void cannotCopySmallFilesToDirectory2() throws IOException {
        File destination = dummyFiles.testDirectory();
        try {
            RollingFileOutputWriter.doCopySmallFile(dummyFiles.testFile(TEST_CONTENT), destination);
        } catch (IOException ioe) {
            assertThat(ioe).hasMessage("Can not copy file, destination is a directory: " + destination.getAbsolutePath());
            throw ioe;
        }
    }

    @Test
    public void copyFileToNonExistingDestination2() throws IOException {
        File source = dummyFiles.testFile(TEST_CONTENT);
        File destination = dummyFiles.nonExistingFile();
        RollingFileOutputWriter.doCopySmallFile(source, destination);

        assertThat(destination).exists();
        assertThat(destination).hasContent(TEST_CONTENT);
        // source should still exist
        assertThat(source).exists();
        assertThat(source).hasContent(TEST_CONTENT);
    }

    @Test
    public void copyToEmptyFile2() throws IOException {
        File source = dummyFiles.testFile(TEST_CONTENT);
        File destination = dummyFiles.testFile("");
        RollingFileOutputWriter.doCopySmallFile(source, destination);

        assertThat(destination).exists();
        assertThat(destination).hasContent(TEST_CONTENT);
        // source should still exist
        assertThat(source).exists();
        assertThat(source).hasContent(TEST_CONTENT);
    }

    @Test
    public void copyToNonEmptyFile2() throws IOException {
        File source = dummyFiles.testFile(TEST_CONTENT);
        File destination = dummyFiles.testFile("1234");
        RollingFileOutputWriter.doCopySmallFile(source, destination);

        assertThat(destination).exists();
        assertThat(destination).hasContent(TEST_CONTENT);
        // source should still exist
        assertThat(source).exists();
        assertThat(source).hasContent(TEST_CONTENT);
    }

}
