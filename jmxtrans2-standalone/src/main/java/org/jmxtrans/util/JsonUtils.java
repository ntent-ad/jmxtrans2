package org.jmxtrans.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

import java.io.File;
import java.io.IOException;

import org.jmxtrans.model.JmxProcess;

public final class JsonUtils {

	private JsonUtils() {}

	/**
	 * Uses jackson to load json configuration from a File into a full object
	 * tree representation of that json.
	 */
	public static JmxProcess getJmxProcess(File file) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new GuavaModule());
		JmxProcess jmx = mapper.readValue(file, JmxProcess.class);
		jmx.setName(file.getName());
		return jmx;
	}
}
