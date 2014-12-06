package org.jmxtrans.jobs;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.management.remote.JMXConnector;

import org.jmxtrans.connections.JMXConnectionParams;
import org.jmxtrans.jmx.JmxUtils;
import org.jmxtrans.model.Server;

/**
 * This is a quartz job that is responsible for executing a Server object on a
 * cron schedule that is defined within the Server object.
 *
 * @author jon
 */
public class ServerJob implements Job {
	private static final Logger log = LoggerFactory.getLogger(ServerJob.class);

	private final GenericKeyedObjectPool<JMXConnectionParams, JMXConnector> jmxPool;

	private final JmxUtils jmxUtils;

	@Inject
	public ServerJob(GenericKeyedObjectPool<JMXConnectionParams, JMXConnector> jmxPool, JmxUtils jmxUtils) {
		this.jmxPool = jmxPool;
		this.jmxUtils = jmxUtils;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap map = context.getMergedJobDataMap();
		Server server = (Server) map.get(Server.class.getName());

		log.debug("+++++ Started server job: {}", server);

		JMXConnector conn = null;
		JMXConnectionParams connectionParams = null;
		try {
			connectionParams = new JMXConnectionParams(server.getJmxServiceURL(), server.getEnvironment());
			if (!server.isLocal()) {
				conn = jmxPool.borrowObject(connectionParams);
			}
			jmxUtils.processServer(server, conn);
		} catch (Exception e) {
			log.error("Error in job for server: " + server, e);
			throw new JobExecutionException(e);
		} finally {
			try {
				jmxPool.returnObject(connectionParams, conn);
			} catch (Exception ex) {
				log.error("Error returning object to pool for server: " + server);
			}
		}

		log.debug("+++++ Finished server job: {}", server);
	}
}
