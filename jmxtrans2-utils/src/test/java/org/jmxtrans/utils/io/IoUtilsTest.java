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

import java.io.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.jmxtrans.utils.io.Charsets.UTF_8;

import static org.assertj.core.api.Assertions.assertThat;

public class IoUtilsTest {

    public static final String TEST_CONTENT = "Hello World";
    private byte[] testContentBytes;
    private final DummyFiles dummyFiles = new DummyFiles();

    @Before
    public void setUp() throws Exception {
        testContentBytes = TEST_CONTENT.getBytes(UTF_8);
    }

    @Test(expected = IOException.class)
    public void cannotCopySmallFilesToDirectory() throws IOException {
        File destination = dummyFiles.testDirectory();
        try {
            IoUtils.doCopySmallFile(dummyFiles.testFile(TEST_CONTENT), destination, false);
        } catch (IOException ioe) {
            assertThat(ioe).hasMessage("Can not copy file, destination is a directory: " + destination.getAbsolutePath());
            throw ioe;
        }
    }

    @Test
    @Ignore("Current implementation does not copy files, but move them when destination does not exist")
    public void copyFileToNonExistingDestination() throws IOException {
        File source = dummyFiles.testFile(TEST_CONTENT);
        File destination = dummyFiles.nonExistingFile();
        IoUtils.doCopySmallFile(source, destination, false);

        assertThat(destination).exists();
        assertThat(destination).hasContent(TEST_CONTENT);
        // source should still exist
        assertThat(source).exists();
        assertThat(source).hasContent(TEST_CONTENT);
    }

    @Test
    public void copyToEmptyFile() throws IOException {
        File source = dummyFiles.testFile(TEST_CONTENT);
        File destination = dummyFiles.testFile("");
        IoUtils.doCopySmallFile(source, destination, false);

        assertThat(destination).exists();
        assertThat(destination).hasContent(TEST_CONTENT);
        // source should still exist
        assertThat(source).exists();
        assertThat(source).hasContent(TEST_CONTENT);
    }

    @Test
    public void copyToNonEmptyFile() throws IOException {
        File source = dummyFiles.testFile(TEST_CONTENT);
        File destination = dummyFiles.testFile("1234");
        IoUtils.doCopySmallFile(source, destination, false);

        assertThat(destination).exists();
        assertThat(destination).hasContent(TEST_CONTENT);
        // source should still exist
        assertThat(source).exists();
        assertThat(source).hasContent(TEST_CONTENT);
    }

    @Test
    public void appendToExistingFile() throws IOException {
        File source = dummyFiles.testFile(TEST_CONTENT);
        File destination = dummyFiles.testFile(TEST_CONTENT);
        IoUtils.doCopySmallFile(source, destination, true);

        assertThat(destination).exists();
        assertThat(destination).hasContent(TEST_CONTENT + "\n" + TEST_CONTENT);
        // source should still exist
        assertThat(source).exists();
        assertThat(source).hasContent(TEST_CONTENT);
    }

    @Test
    public void canCopyStreamToEmptyDestination() throws IOException {
        testContentBytes = TEST_CONTENT.getBytes(UTF_8);
        InputStream in = new ByteArrayInputStream(testContentBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IoUtils.copy(in, out);

        assertThat(out.toByteArray()).isEqualTo(testContentBytes);
    }

    @After
    public void cleanUpDummyFiles() {
        dummyFiles.cleanUpTestFiles();
    }

}
