package org.jmxtrans.example;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.jmxtrans.JmxTransformer;
import org.jmxtrans.cli.JmxTransConfiguration;
import org.jmxtrans.guice.JmxTransModule;
import org.jmxtrans.model.JmxProcess;
import org.jmxtrans.model.Query;
import org.jmxtrans.model.Server;
import org.jmxtrans.model.output.GraphiteWriter;
import org.jmxtrans.util.JsonPrinter;

/**
 * This example shows how to query ehcache for its statistics information.
 * 
 * @author jon
 */
public class Ehcache {

	private static final String GW_HOST = "192.168.192.133";
	private static final JsonPrinter printer = new JsonPrinter(System.out);

	/** */
	public static void main(String[] args) throws Exception {

		JmxProcess process = new JmxProcess(Server.builder()
				.setHost("w2")
				.setPort("1099")
				.setAlias("w2_ehcache_1099")
				.addQuery(Query.builder()
						.setObj("net.sf.ehcache:CacheManager=net.sf.ehcache.CacheManager@*,name=*,type=CacheStatistics")
						.addAttr("CacheHits")
						.addAttr("InMemoryHits")
						.addAttr("OnDiskHits")
						.addAttr("CacheMisses")
						.addAttr("ObjectCount")
						.addAttr("MemoryStoreObjectCount")
						.addAttr("DiskStoreObjectCount")
						.addOutputWriter(GraphiteWriter.builder()
								.addTypeName("name")
								.setDebugEnabled(true)
								.setHost(GW_HOST)
								.setPort(2003)
								.build())
						.build())
				.build());

		printer.prettyPrint(process);

		Injector injector = Guice.createInjector(new JmxTransModule(new JmxTransConfiguration()));
		JmxTransformer transformer = injector.getInstance(JmxTransformer.class);

		transformer.executeStandalone(process);
	}
}
