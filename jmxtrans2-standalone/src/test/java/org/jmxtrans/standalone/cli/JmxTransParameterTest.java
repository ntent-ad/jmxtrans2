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

import org.jmxtrans.utils.io.FileResource;
import org.jmxtrans.utils.io.TemporaryFolder;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JmxTransParameterTest {

    private TemporaryFolder temporaryFolder;

    @BeforeMethod
    public void createTemporaryFolder() throws IOException {
        temporaryFolder = new TemporaryFolder();
    }

    @Test(
            expectedExceptions = ParameterException.class,
            expectedExceptionsMessageRegExp = "Parameter \\[--configFiles\\] is a non existing file \\[.*non-existing-file\\]")
    public void failOnNonExistingConfigFile() {
        String[] arguments = new String[] {
                "--configFiles", "non-existing-file"
        };
        new JCommander(new JmxTransParameters(), arguments);
    }
    
    @Test(
            expectedExceptions = ParameterException.class,
            expectedExceptionsMessageRegExp = "Parameter \\[--configDirectories\\] is a non existing directory \\[.*non-existing-directory\\]")
    public void failOnNonExistingConfigDirectory() {
        String[] arguments = new String[] {
                "--configDirectories", "non-existing-directory"
        };
        new JCommander(new JmxTransParameters(), arguments);
    }
    
    @Test
    public void configFilesAndDirectoriesAreAggregated() throws IOException {
        File configFile = temporaryFolder.newFile();
        File configDir = temporaryFolder.newFolder();
        File configFileInConfigDir = new File(configDir, "configFile");
        configFileInConfigDir.createNewFile();

        String[] arguments = new String[] {
                "--configFiles", configFile.getAbsolutePath(),
                "--configDirectories", configDir.getAbsolutePath()
        };
        JmxTransParameters parameters = new JmxTransParameters();
        new JCommander(parameters, arguments);
        assertThat(parameters.getConfigResources())
                .contains(new FileResource(configFile))
                .contains(new FileResource(configFileInConfigDir));
    }

    @AfterMethod
    public void destroyTempFolder() throws IOException {
        temporaryFolder.destroy();
    }
    
}
