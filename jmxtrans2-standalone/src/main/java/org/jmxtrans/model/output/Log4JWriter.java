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
package org.jmxtrans.model.output;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jmxtrans.model.Query;
import org.jmxtrans.model.Result;
import org.jmxtrans.model.Server;
import org.jmxtrans.model.ValidationException;
import org.jmxtrans.model.naming.KeyUtils;
import org.jmxtrans.model.naming.StringUtils;


/**
 * A writer for Log4J. It may be a nice way to send JMX metrics to Logstash for example. <br /> <br />
 * <p/>
 * Here is an example of MDC variables that are set by this class. <br> <br> server: localhost_9003 <br /> metric:
 * sun_management_MemoryImpl.HeapMemoryUsage_committed <br /> value: 1251999744 <br /> resultAlias: myHeapMemoryUsage
 * <br /> attributeName: HeapMemoryUsage <br /> key: committed <br /> epoch: 1388343325728 <br />
 *
 * @author Yannick Robin
 */
public class Log4JWriter extends BaseOutputWriter {
	private final Logger log;
	private final String logger;

	@JsonCreator
	public Log4JWriter(
			@JsonProperty("typeNames") ImmutableList<String> typeNames,
			@JsonProperty("booleanAsNumber") boolean booleanAsNumber,
			@JsonProperty("debug") Boolean debugEnabled,
			@JsonProperty("logger") String logger,
			@JsonProperty("settings") Map<String, Object> settings) {
		super(typeNames, booleanAsNumber, debugEnabled, settings);
		this.logger = firstNonNull(logger, (String) getSettings().get("logger"), "Log4JWriter");
		this.log = Logger.getLogger("Log4JWriter." + this.logger);
	}

	public void validateSetup(Server server, final Query query) throws ValidationException {}

	/**
	 * Set the log context and log
	 */
	public void internalWrite(Server server, final Query query, ImmutableList<Result> results) throws Exception {
		final List<String> typeNames = getTypeNames();

		for (final Result result : results) {
			final Map<String, Object> resultValues = result.getValues();
			if (resultValues != null) {
				for (final Entry<String, Object> values : resultValues.entrySet()) {
					if (NumberUtils.isNumeric(values.getValue())) {
						String alias;
						if (server.getAlias() != null) {
							alias = server.getAlias();
						} else {
							alias = server.getHost() + "_" + server.getPort();
							alias = StringUtils.cleanupStr(alias);
						}

						MDC.put("server", alias);
						MDC.put("metric", KeyUtils.getKeyString(server, query, result, values, typeNames, null));
						MDC.put("value", values.getValue());
						if (result.getClassNameAlias() != null) {
							MDC.put("resultAlias", result.getClassNameAlias());
						}
						MDC.put("attributeName", result.getAttributeName());
						MDC.put("key", values.getKey());
						MDC.put("Epoch", String.valueOf(result.getEpoch()));
						log.info("");
					}
				}
			}
		}
	}

	public String getLogger() {
		return logger;
	}
}
