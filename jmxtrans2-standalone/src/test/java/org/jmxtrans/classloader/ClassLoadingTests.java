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
package org.jmxtrans.classloader;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

public class ClassLoadingTests {

	@Test(expected = ClassNotFoundException.class)
	public void classCannotBeAccessedIfJarIsNotLoaded() throws ClassNotFoundException {
		Class.forName("dummy.Dummy");
	}

	/**
	 * This test modify the class loader. This makes it hard to isolate, so let's just run it manually.
	 */
	@Test
	@Ignore("Manual test")
	public void loadedJarCanBeAccessed() throws ClassNotFoundException, MalformedURLException, FileNotFoundException {
		new ClassLoaderEnricher().add(new File("test/dummy.jar"));
		Class.forName("dummy.Dummy");
	}

	@Test(expected = FileNotFoundException.class)
	public void loadingNonExistingFileThrowsException() throws MalformedURLException, FileNotFoundException {
		new ClassLoaderEnricher().add(new File("/nonexising"));
	}

}