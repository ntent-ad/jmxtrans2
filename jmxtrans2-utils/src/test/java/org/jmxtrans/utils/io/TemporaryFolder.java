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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.delete;

public class TemporaryFolder {
    
    // NOTE: This class should be implemented with a field listener (to match the JUnit @Rule, which is much more
    // elegant). Field listeners don't seem to exist in testng. So we should probably implement one ...
    
    private final Path tempFolder;
    
    public TemporaryFolder() throws IOException {
        tempFolder = createTempDirectory("unit-test");
    }
    
    public File newFile() throws IOException {
        return createTempFile(tempFolder, "unit-test", "file").toFile();
    }

    public File newFolder() throws IOException {
        return newFolder("unit-test");
    }

    public File newFolder(String prefix) throws IOException {
        return createTempDirectory(tempFolder, prefix).toFile();
    }

    public void destroy() throws IOException {
        Files.walkFileTree(tempFolder, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (attrs.isRegularFile()) delete(file);
                return CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException ioe) throws IOException {
                delete(directory);
                return CONTINUE;
            }
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException ioe) throws IOException {
                return CONTINUE;
            }
        });
    }
}
