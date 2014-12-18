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
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jmxtrans.exceptions.LifecycleException;
import org.jmxtrans.model.OutputWriter;
import org.jmxtrans.model.Query;
import org.jmxtrans.model.Result;
import org.jmxtrans.model.Server;
import org.jmxtrans.model.naming.KeyUtils;
import org.jmxtrans.model.results.BooleanAsNumberValueTransformer;
import org.jmxtrans.model.results.IdentityValueTransformer;
import org.jmxtrans.model.results.ValueTransformer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.copyOf;
import static org.jmxtrans.model.PropertyResolver.resolveMap;
import static org.jmxtrans.model.output.Settings.getBooleanSetting;

/**
 * Implements the common code for output filters.
 *
 * Note that the use of a non threadsafe @link{Map} makes this class non threadsafe.
 *
 * @author jon
 */
@NotThreadSafe
public abstract class BaseOutputWriter implements OutputWriter {

	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String OUTPUT_FILE = "outputFile";
	public static final String TEMPLATE_FILE = "templateFile";
	public static final String BINARY_PATH = "binaryPath";
	public static final String DEBUG = "debug";
	public static final String TYPE_NAMES = "typeNames";

	private ImmutableList<String> typeNames;
	private boolean debugEnabled;
	private Map<String, Object> settings;
	private final ValueTransformer valueTransformer;

	@JsonCreator
	public BaseOutputWriter(
			@JsonProperty("typeNames") ImmutableList<String> typeNames,
			@JsonProperty("booleanAsNumber") boolean booleanAsNumber,
			@JsonProperty("debug") Boolean debugEnabled,
			@JsonProperty("settings") Map<String, Object> settings) {
		// resolve and initialize settings first, so we cean refer to them to initialize other fields
		this.settings = resolveMap(MoreObjects.firstNonNull(
				settings,
				Collections.<String, Object>emptyMap()));

		this.typeNames = copyOf(firstNonNull(
				typeNames,
				(List<String>) this.settings.get(TYPE_NAMES),
				Collections.<String>emptyList()));
		this.debugEnabled = firstNonNull(
				debugEnabled,
				getBooleanSetting(this.settings, DEBUG),
				false);

		if (booleanAsNumber) {
			this.valueTransformer = new BooleanAsNumberValueTransformer(0, 1);
		} else {
			this.valueTransformer = new IdentityValueTransformer();
		}
	}

	protected <T> T firstNonNull(@Nullable T first, @Nullable T second, @Nullable T third) {
		return first != null ? first : (second != null ? second : checkNotNull(third));
	}

	/**
	 * @deprecated Don't use the settings Map, please extract necessary bits at construction time.
	 */
	@Deprecated
	public Map<String, Object> getSettings() {
		return settings;
	}

	/**
	 * @deprecated Initialize settings in constructor only please.
	 */
	@Deprecated
	public void setSettings(Map<String, Object> settings) {
		this.settings = resolveMap(settings);
		if (settings.containsKey(DEBUG)) {
			this.debugEnabled = getBooleanSetting(settings, DEBUG);
		}
		if (settings.containsKey(TYPE_NAMES)) {
			this.typeNames = copyOf((List<String>) settings.get(TYPE_NAMES));
		}
	}

	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	public List<String> getTypeNames() {
		return typeNames;
	}

	/**
	 * Given a typeName string, get the first match from the typeNames setting.
	 * In other words, suppose you have:
	 * <p/>
	 * typeName=name=PS Eden Space,type=MemoryPool
	 * <p/>
	 * If you addTypeName("name"), then it'll retrieve 'PS Eden Space' from the
	 * string
	 */
	protected String getConcatedTypeNameValues(String typeNameStr) {
		return KeyUtils.getConcatedTypeNameValues(this.getTypeNames(), typeNameStr);
	}

	/**
	 * A do nothing method.
	 */
	@Override
	public void start() throws LifecycleException {
		// Do nothing.
	}

	/**
	 * A do nothing method.
	 */
	@Override
	public void stop() throws LifecycleException {
		// Do nothing.
	}

	@Override
	public final void doWrite(Server server, Query query, ImmutableList<Result> results) throws Exception {
		internalWrite(server, query, from(results).transform(new ResultValuesTransformer(valueTransformer)).toList());
	}

	protected abstract void internalWrite(Server server, Query query, ImmutableList<Result> results) throws Exception;

	private static final class ResultValuesTransformer implements Function<Result, Result> {

		private final ValueTransformer valueTransformer;

		private ResultValuesTransformer(ValueTransformer valueTransformer) {
			this.valueTransformer = valueTransformer;
		}

		@Nullable
		@Override
		public Result apply(@Nullable Result input) {
			if (input == null) {
				return null;
			}
			return new Result(
					input.getEpoch(),
					input.getAttributeName(),
					input.getClassName(),
					input.getClassNameAlias(),
					input.getTypeName(),
					Maps.transformValues(input.getValues(), valueTransformer)
			);
		}
	}
}
