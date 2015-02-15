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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.jmxtrans.utils.io.Charsets.UTF_8;

import static org.assertj.core.api.Assertions.assertThat;

public class FileResourceTest {

    private TemporaryFolder testFolder;

    @BeforeMethod
    public void createTemporaryFolder() throws IOException {
        testFolder = new TemporaryFolder();
    }
    
    @Test
    public void fileResourceCanBeRead() throws IOException {
        File file = createFileWithContent("hello world");
        Resource resource = new FileResource(file);
        assertThat(resource.getPath()).isEqualTo(file.getAbsolutePath());
        try (InputStream in = resource.getInputStream()) {
            assertThat(in).hasContentEqualTo(inputStreamWithContent("hello world"));
        }
    }

    @Test
    public void resourcesForSameFileAreEqual() {
        Resource resource1 = new FileResource(new File("/tmp"));
        Resource resource2 = new FileResource(new File("/tmp"));
        assertThat(resource1).isEqualTo(resource2);
    }

    @Test
    public void resourcesForSameFileHaveSameHashCode() {
        Resource resource1 = new FileResource(new File("/tmp"));
        Resource resource2 = new FileResource(new File("/tmp"));
        assertThat(resource1.hashCode()).isEqualTo(resource2.hashCode());
    }

    @Test
    public void resourcesForDifferentFilesAreNotEqual() {
        Resource resource1 = new FileResource(new File("/tmp"));
        Resource resource2 = new FileResource(new File("/toto"));
        assertThat(resource1).isNotEqualTo(resource2);
    }

    @Test
    public void resourcesForDifferentFilesHaveDifferentHashcodes() {
        // Ok, we could have collisions ... but let's not take that into account for this test
        Resource resource1 = new FileResource(new File("/tmp"));
        Resource resource2 = new FileResource(new File("/toto"));
        assertThat(resource1.hashCode()).isNotEqualTo(resource2.hashCode());
    }

    private InputStream inputStreamWithContent(String content) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(content.getBytes(UTF_8));
    }

    private File createFileWithContent(String content) throws IOException {
        File file = testFolder.newFile();
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(content.getBytes(UTF_8));
        }
        return file;
    }

    @AfterMethod
    public void destroyTempFolder() throws IOException {
        testFolder.destroy();
    }
    
}
