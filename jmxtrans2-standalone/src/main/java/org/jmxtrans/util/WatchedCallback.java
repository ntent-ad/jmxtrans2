package org.jmxtrans.util;

import java.io.File;

public interface WatchedCallback {

	void fileModified(File file) throws Exception;

	void fileDeleted(File file) throws Exception;

	void fileAdded(File file) throws Exception;
}
