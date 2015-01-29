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

import com.beust.jcommander.ParameterException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ExistingFileValidatorTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void validationPassIfFileExists() throws IOException {
        File existingFile = testFolder.newFile();
        new ExistingFileValidator().validate("", existingFile);
    }

    @Test(expected = ParameterException.class)
    public void validationFailsIfFileDoesNotExist() throws IOException {
        File nonExistingFile = testFolder.newFile();
        nonExistingFile.delete();
        new ExistingFileValidator().validate("", nonExistingFile);
    }

    @Test(expected = ParameterException.class)
    public void validationFailsIfFileIsADirectory() throws IOException {
        File nonExistingFile = testFolder.newFolder();
        nonExistingFile.delete();
        new ExistingFileValidator().validate("", nonExistingFile);
    }

}
