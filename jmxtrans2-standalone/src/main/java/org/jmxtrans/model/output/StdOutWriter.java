package org.jmxtrans.model.output;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.Map;

import org.jmxtrans.model.Query;
import org.jmxtrans.model.Result;
import org.jmxtrans.model.Server;
import org.jmxtrans.model.ValidationException;

/**
 * Basic filter good for testing that just outputs the Result objects using
 * System.out.
 * 
 * @author jon
 */
public class StdOutWriter extends BaseOutputWriter {

	@JsonCreator
	public StdOutWriter(
			@JsonProperty("typeNames") ImmutableList<String> typeNames,
			@JsonProperty("booleanAsNumber") boolean booleanAsNumber,
			@JsonProperty("debug") Boolean debugEnabled,
			@JsonProperty("settings") Map<String, Object> settings) {
		super(typeNames, booleanAsNumber, debugEnabled, settings);
	}

	/**
	 * nothing to validate
	 */
	public void validateSetup(Server server, Query query) throws ValidationException {
	}

	public void internalWrite(Server server, Query query, ImmutableList<Result> results) throws Exception {
		for (Result r : results) {
			System.out.println(r);
		}
	}
}
