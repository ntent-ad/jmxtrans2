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
package org.jmxtrans.utils.appinfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.jmxtrans.utils.mockito.MockitoTestNGListener;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Listeners(MockitoTestNGListener.class)
public class AppInfoTest {

    @Mock private GitRepositoryState repositoryState;
    private AppInfo<AppInfo> appInfo;

    @BeforeMethod
    public void initializeAppInfo() throws IOException {
        appInfo = AppInfo.load(AppInfo.class);
    }

    @Test
    public void applicationNameIsLoaded() {
        assertThat(appInfo.getName()).isEqualTo("JMXTrans - utils");
    }

    @Test
    public void versionNumberIsDotSeparated() {
        assertThat(appInfo.getVersion()).contains(".");
    }

    @Test
    public void fullVersionContainsVersionAndGitHash() {
        assertThat(appInfo.getFullVersion())
                .startsWith(appInfo.getVersion())
                .contains("-");
    }

    @Test
    public void issueManagementUrlIsLoaded() {
        assertThat(appInfo.getIssueManagementUrl()).startsWith("http");
    }

    @Test
    public void appInfoPrintsRequiredFields() throws IOException {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(baos)) {
            appInfo.print(out);

            String result = baos.toString();

            assertThat(result)
                    .contains("JMXTrans")
                    .contains("version:")
                    .contains("last modified:")
                    .contains("build time: ")
                    .contains("license: MIT")
                    .contains("in case of problem");
        }
    }
    
    @Test
    public void userAgentContainsAppropriateInformation() {
        String userAgent = appInfo.getUserAgent();

        assertThat(userAgent)
                .contains("org.jmxtrans.jmxtrans2")
                .contains("jmxtrans")
                .contains(System.getProperty("java.vm.name"))
                .contains(System.getProperty("java.version"))
                .contains(System.getProperty("os.name"))
                .contains(System.getProperty("os.arch"))
                .contains(System.getProperty("os.version"));
        
    }

}
