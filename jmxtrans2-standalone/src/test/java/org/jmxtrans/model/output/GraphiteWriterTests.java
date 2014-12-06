package org.jmxtrans.model.output;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.jmxtrans.model.Query;
import org.jmxtrans.model.Result;
import org.jmxtrans.model.Server;
import org.jmxtrans.model.ValidationException;

import static com.google.common.collect.ImmutableList.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GraphiteWriterTests {

	@Test(expected = NullPointerException.class)
	public void hostIsRequired() throws ValidationException {
		try {
			GraphiteWriter.builder()
					.setPort(123)
					.build();
		} catch (NullPointerException npe) {
			assertThat(npe).hasMessage("Host cannot be null.");
			throw npe;
		}
	}

	@Test(expected = NullPointerException.class)
	public void portIsRequired() throws ValidationException {
		try {
			GraphiteWriter.builder()
					.setHost("localhost")
					.build();
		} catch (NullPointerException npe) {
			assertThat(npe).hasMessage("Port cannot be null.");
			throw npe;
		}
	}

	@Test
	public void writeSingleResult() throws Exception {
		// a lot of setup for not much of a test ...
		Server server = Server.builder().setHost("host").setPort("123").build();
		Query query = Query.builder().build();
		Result result = new Result(System.currentTimeMillis(), "attributeName", "className", "classNameAlias", "typeName", ImmutableMap.of("key", (Object)1));

		GenericKeyedObjectPool<InetSocketAddress, Socket> pool = mock(GenericKeyedObjectPool.class);
		Socket socket = mock(Socket.class);
		when(pool.borrowObject(any(InetSocketAddress.class))).thenReturn(socket);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		when(socket.getOutputStream()).thenReturn(out);

		GraphiteWriter writer = GraphiteWriter.builder()
				.setHost("localhost")
				.setPort(2003)
				.build();
		writer.setPool(pool);

		writer.doWrite(server, query, of(result));

		// check that Graphite format is respected
		assertThat(out.toString()).startsWith("servers.host_123.classNameAlias.attributeName_key 1 ");
	}

}
