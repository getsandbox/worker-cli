/*
 * Copyright (c) 2011-2014 GoPivotal, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.pivotal.cf.mobile.ats.json;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.StdConverter;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * @author Will Tran <wtran@pivotallabs.com>
 */
public class ScriptObjectMirrorSerializer extends StdSerializer<ScriptObjectMirror> {
	public ScriptObjectMirrorSerializer() {
		super(ScriptObjectMirror.class);
	}

	private StdDelegatingSerializer arraySerializer = null;

	private StdDelegatingSerializer objectSerializer = null;

	@Override
	public void serialize(ScriptObjectMirror value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonGenerationException {
		if (value.isArray()) {
			if (arraySerializer == null) {
				arraySerializer = (StdDelegatingSerializer) new StdDelegatingSerializer(ScriptObjectMirror.class,
						new StdConverter<ScriptObjectMirror, Collection<Object>>() {

							@Override
							public Collection<Object> convert(ScriptObjectMirror value) {
								return value.values();
							}

						}).createContextual(provider, null);

			}
			arraySerializer.serialize(value, jgen, provider);
		} else {
			if (objectSerializer == null) {
				objectSerializer = (StdDelegatingSerializer) new StdDelegatingSerializer(ScriptObjectMirror.class,
						new StdConverter<ScriptObjectMirror, Map<String, Object>>() {

							@Override
							public Map<String, Object> convert(ScriptObjectMirror value) {
								return value;
							}

						}).createContextual(provider, null);

			}
			objectSerializer.serialize(value, jgen, provider);
		}
	}

}
