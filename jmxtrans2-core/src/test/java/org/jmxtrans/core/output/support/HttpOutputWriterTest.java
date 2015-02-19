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
package org.jmxtrans.core.output.support;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.jmxtrans.core.results.QueryResult;
import org.jmxtrans.utils.appinfo.AppInfo;
import org.jmxtrans.utils.mockito.MockitoTestNGListener;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.mockito.Mock;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.resetToDefault;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

@Listeners(MockitoTestNGListener.class)
public class HttpOutputWriterTest {

    private WireMockServer wireMockServer;

    private AppInfo<AppInfo> appInfo;
    @Mock private OutputStreamBasedOutputWriter target;
    @Mock private QueryResult result;

    @BeforeMethod
    public void initializeAppInfo() throws IOException {
        appInfo = AppInfo.load(AppInfo.class);
    }

    @BeforeClass
    public void startHttpServer() {
        wireMockServer = new WireMockServer();
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
    }
    
    @BeforeMethod
    public void resetServer() {
        resetToDefault();
    }
    
    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "URL needs to be HTTP or HTTPS \\[ftp://ftp.test.net\\]")
    public void ftpIsNotSupported() throws MalformedURLException {
        HttpOutputWriter.builder(new URL("ftp://ftp.test.net"), appInfo, target)
                .build();
    }

    @Test
    public void canWriteSimplePostMessage() throws IOException {
        stubFor(post(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(200)));

        HttpOutputWriter<DummyStreamWriter> outputWriter = HttpOutputWriter
                .builder(new URL("http://localhost:" + wireMockServer.port()), appInfo, new DummyStreamWriter("hello world"))
                .build();

        int count = outputWriter.write(result);
        assertThat(count).isEqualTo(1);

        verify(postRequestedFor(urlEqualTo("/"))
                .withRequestBody(equalTo("hello world")));
    }

    @Test
    public void canSetContentType() throws IOException {
        stubFor(post(urlEqualTo("/"))
                .withHeader("Content-Type", matching("text/plain"))
                .willReturn(aResponse()
                        .withStatus(200)));

        HttpOutputWriter<DummyStreamWriter> outputWriter = HttpOutputWriter
                .builder(new URL("http://localhost:" + wireMockServer.port()), appInfo, new DummyStreamWriter("hello world"))
                .withContentType("text/plain")
                .build();

        int count = outputWriter.write(result);
        assertThat(count).isEqualTo(1);

        verify(postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", matching("text/plain"))
                .withRequestBody(equalTo("hello world")));
    }

    @Test
    public void canAuthenticate() throws IOException {
        stubFor(post(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(200)));

        HttpOutputWriter<DummyStreamWriter> outputWriter = HttpOutputWriter
                .builder(new URL("http://localhost:" + wireMockServer.port()), appInfo, new DummyStreamWriter("hello world"))
                .withAuthentication("username", "password")
                .build();

        int count = outputWriter.write(result);
        assertThat(count).isEqualTo(1);

        verify(postRequestedFor(urlEqualTo("/"))
                .withHeader("Authorization", matching("Basic dXNlcm5hbWU6cGFzc3dvcmQ="))
                .withRequestBody(equalTo("hello world")));
    }

    @Test
    public void noAuthenticationSentIfAuthenticationNotSet() throws IOException {
        stubFor(post(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(200)));

        HttpOutputWriter<DummyStreamWriter> outputWriter = HttpOutputWriter
                .builder(new URL("http://localhost:" + wireMockServer.port()), appInfo, new DummyStreamWriter("hello world"))
                .build();

        int count = outputWriter.write(result);
        assertThat(count).isEqualTo(1);

        verify(postRequestedFor(urlEqualTo("/"))
                .withoutHeader("Authorization")
                .withRequestBody(equalTo("hello world")));
    }

    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = ".*not OK.*404.*")
    public void exceptionThrownIfResponseIsNotOk() throws IOException {
        stubFor(post(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withBody("world hello")
                        .withStatus(404)));

        HttpOutputWriter<DummyStreamWriter> outputWriter = HttpOutputWriter
                .builder(new URL("http://localhost:" + wireMockServer.port()), appInfo, new DummyStreamWriter("hello world"))
                .build();

        outputWriter.write(result);
    }

    @Test(expectedExceptions = SocketTimeoutException.class)
    public void timeoutIsRespected() throws IOException {
        stubFor(post(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withFixedDelay(100)
                        .withStatus(200)));

        HttpOutputWriter<DummyStreamWriter> outputWriter = HttpOutputWriter
                .builder(new URL("http://localhost:" + wireMockServer.port()), appInfo, new DummyStreamWriter("hello world"))
                .withTimeout(10, MILLISECONDS)
                .build();

        outputWriter.write(result);
    }
    
    @AfterClass
    public void stopHttpServer() {
        wireMockServer.stop();
    }
    
}
