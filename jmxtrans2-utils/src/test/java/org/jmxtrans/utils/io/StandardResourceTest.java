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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.jmxtrans.utils.io.Charsets.UTF_8;

import static org.assertj.core.api.Assertions.assertThat;

public class StandardResourceTest {

    public static final String HELLO_WORLD_PATH = "org/jmxtrans/utils/io/hello.world";
    public static final String HELLO_WORLD_CONTENT = "hello world";

    private TemporaryFolder temporaryFolder;

    @BeforeMethod
    public void createTemporaryFolder() throws IOException {
        temporaryFolder = new TemporaryFolder();
    }

    @Test
    public void canLoadClasspathResource() throws IOException {
        Resource resource = new StandardResource("classpath:" + HELLO_WORLD_PATH);
        try (InputStream in = resource.getInputStream()) {
            assertThat(in).isNotNull();
            assertThat(in).hasContentEqualTo(getTestStream(HELLO_WORLD_CONTENT));
        }
    }

    @Test(expectedExceptions = IOException.class)
    public void nonExistingClasspathResourceThrowsException() throws IOException {
        Resource resource = new StandardResource("classpath:non/existing/resource");
        try (InputStream in = resource.getInputStream()){
            assertThat(in).isNotNull();
        } catch (IOException ioe) {
            assertThat(ioe).hasMessageContaining("classpath:non/existing/resource");
            throw ioe;
        }
    }

    @Test
    public void canLoadFileUrlResource() throws IOException {
        Resource resource = new StandardResource("file://" + getTestFilePath(HELLO_WORLD_PATH));
        try (InputStream in = resource.getInputStream()) {
            assertThat(in).isNotNull();
            assertThat(in).hasContentEqualTo(getTestStream("hello world"));
        }
    }

    @Test
    public void canLoadFileResource() throws IOException {
        Resource resource = new StandardResource(getTestFilePath(HELLO_WORLD_PATH));
        try (InputStream in = resource.getInputStream()) {
            assertThat(in).isNotNull();
            assertThat(in).hasContentEqualTo(getTestStream("hello world"));
        }
    }

    @Test(expectedExceptions = FileNotFoundException.class)
    public void nonExistingFileThrowException() throws IOException {
        Resource resource = new StandardResource("/non/existing/file");
        try (InputStream in = resource.getInputStream()) {
            assertThat(in).isNotNull();
        }
    }

    @Test(expectedExceptions = FileNotFoundException.class)
    public void directoryThrowsException() throws IOException {
        Resource resource = new StandardResource(temporaryFolder.newFolder("test").getPath());
        try (InputStream in = resource.getInputStream()) {
            assertThat(in).isNotNull();
        }
    }

    private String getTestFilePath(String helloTxtPath) {
        return StandardResourceTest.class.getClassLoader().getResource(helloTxtPath).getFile();
    }

    private ByteArrayInputStream getTestStream(String content) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(content.getBytes(UTF_8));
    }

    @AfterMethod
    public void destroyTempFolder() throws IOException {
        temporaryFolder.destroy();
    }

}
