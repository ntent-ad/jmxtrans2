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
package org.jmxtrans.standalone.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jmxtrans.utils.io.FileResource;
import org.jmxtrans.utils.io.Resource;

import com.beust.jcommander.Parameter;
import lombok.Getter;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.walkFileTree;

public class JmxTransParameters {

    @Nullable
    @Parameter(
            names = { "--configFiles", "-c" },
            description = "List of configuration files.",
            validateValueWith = ExistingFileCollectionValidator.class)
    private List<File> configFiles;

    @Nullable
    @Parameter(
            names = { "--configDirectories", "-d" },
            description = "List of configuration directories, all files in those directories will be loaded.",
            validateValueWith = ExistingDirectoryCollectionValidator.class)
    private List<File> configDirectories;

    @Getter
    @Parameter(
            names = { "--ignoreParsingErrors", "-i" },
            description = "Start JmxTrans even if errors are found in configuration files.")
    private boolean ignoringParsingErrors = false;

    @Getter
    @Parameter(
            names = { "--help", "-h" },
            description = "Display this help message",
            help = true)
    private boolean help;
    
    @Nonnull
    public Iterable<Resource> getConfigResources() throws IOException {
        List<Resource> configurations = new ArrayList<>();
        if (configFiles != null) {
            for (File configFile : configFiles) {
                configurations.add(new FileResource(configFile));
            }
        }
        if (configDirectories != null) {
            for (File configDir : configDirectories) {
                recursivelyFindFiles(configDir, configurations);
            }
        }
        return configurations;
    }

    private void recursivelyFindFiles(@Nonnull File directory, @Nonnull final Collection<Resource> accumulator) throws IOException {
        walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                accumulator.add(new FileResource(file.toFile()));
                return CONTINUE;
            }
        });
    }
}
