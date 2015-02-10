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

import java.io.IOException;

import org.jmxtrans.utils.io.TemporaryFolder;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JmxTransParameterTest {

    private TemporaryFolder temporaryFolder;

    @BeforeMethod
    public void createTemporaryFolder() throws IOException {
        temporaryFolder = new TemporaryFolder();
    }

    @Test(expectedExceptions = ParameterException.class)
    public void failOnNonExistingConfigFile() {
        String[] arguments = new String[] {
                "-configFile", "non-existing-file"
        };
        new JCommander(new JmxTransParameters(), arguments);
    }
    
    @AfterMethod
    public void destroyTempFolder() throws IOException {
        temporaryFolder.destroy();
    }
    
}
