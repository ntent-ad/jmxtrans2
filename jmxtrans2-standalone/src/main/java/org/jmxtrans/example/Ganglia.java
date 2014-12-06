package org.jmxtrans.example;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.jmxtrans.JmxTransformer;
import org.jmxtrans.cli.JmxTransConfiguration;
import org.jmxtrans.guice.JmxTransModule;
import org.jmxtrans.model.JmxProcess;
import org.jmxtrans.model.Query;
import org.jmxtrans.model.Server;
import org.jmxtrans.model.output.GangliaWriter;
import org.jmxtrans.util.JsonPrinter;

/**
 * This class hits a Graphite server running on port 2003 and sends the memory
 * usage data to it for graphing.
 * 
 * @author jon
 */
public class Ganglia {

	private static final JsonPrinter printer = new JsonPrinter(System.out);

	public static void main(String[] args) throws Exception {
		printer.prettyPrint(new JmxProcess(Server.builder()
				.setHost("w2")
				.setPort("1099")
				.setAlias("fooalias")
				.addQuery(Query.builder()
						.setObj("java.lang:type=GarbageCollector,name=ConcurrentMarkSweep")
						.addOutputWriter(GangliaWriter.builder()
							.setHost("10.0.3.16")
							.setPort(8649)
							.setDebugEnabled(true)
							.setGroupName("memory")
							.build())
					.build())
				.build()));

		Injector injector = Guice.createInjector(new JmxTransModule(new JmxTransConfiguration()));
		JmxTransformer transformer = injector.getInstance(JmxTransformer.class);
		transformer.executeStandalone(new JmxProcess(Server.builder()
				.setHost("w2")
				.setPort("1099")
				.setAlias("fooalias")
				.addQuery(Query.builder()
						.setObj("java.lang:type=GarbageCollector,name=ConcurrentMarkSweep")
						.addOutputWriter(GangliaWriter.builder()
							.setHost("10.0.3.16")
							.setPort(8649)
							.setDebugEnabled(true)
							.setGroupName("memory")
							.build())
					.build())
				.build()));
	}

}
