package org.jmxtrans.util;

import com.google.common.io.Closer;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jmxtrans.model.JmxProcess;
import org.jmxtrans.model.Query;
import org.jmxtrans.model.Server;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonPrinterTest {

	@Test
	public void standardJsonPrinting() throws Exception {
		Closer closer = Closer.create();
		try {
			ByteArrayOutputStream baos = closer.register(new ByteArrayOutputStream());
			PrintStream out = closer.register(new PrintStream(baos));

			new JsonPrinter(out).print(standardProcess());
			String result = new String(baos.toByteArray());

			assertThat(result).contains("\"url\":\"service:jmx:rmi:///jndi/rmi://example.org:123/jmxrmi\"");
		} catch (Throwable t) {
			throw closer.rethrow(t);
		} finally {
			closer.close();
		}
	}

	@Test
	public void prettyJsonPrinting() throws Exception {
		Closer closer = Closer.create();
		try {
			ByteArrayOutputStream baos = closer.register(new ByteArrayOutputStream());
			PrintStream out = closer.register(new PrintStream(baos));

			new JsonPrinter(out).prettyPrint(standardProcess());
			String result = new String(baos.toByteArray());

			assertThat(result).contains("\"url\" : \"service:jmx:rmi:///jndi/rmi://example.org:123/jmxrmi\"");
		} catch (Throwable t) {
			throw closer.rethrow(t);
		} finally {
			closer.close();
		}
	}

	private JmxProcess standardProcess() {
		Server server = Server.builder()
				.setAlias("alias")
				.setHost("example.org")
				.setPort("123")
				.addQuery(Query.builder()
					.setObj("obj")
					.build())
				.build();
		return new JmxProcess(server);
	}

}
