/* Copyright 2016 Etnetera a.s.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cz.etnetera.reesmo.writer;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cz.etnetera.reesmo.writer.model.result.ResultLink;
import cz.etnetera.reesmo.writer.model.result.TestSeverity;
import cz.etnetera.reesmo.writer.storage.DummyStorage;
import cz.etnetera.reesmo.writer.storage.FileSystemStorage;
import cz.etnetera.reesmo.writer.storage.RestApiStorage;

public enum Property {

	ENABLED("enabled", Bool.FALSE),
	STORAGE("storage", FileSystemStorage.class),
	BASE_DIR("basedir", new File("reesmo-output")), 
	PROJECT_ID("projectid", null),
	PROJECT_KEY("projectkey", null),
	SUITE("suite", null),
	SUITE_ID("suiteid", null),
	JOB("job", null),
	JOB_ID("jobid", null),
	ENDPOINT("endpoint", null),
	USERNAME("username", null),
	PASSWORD("password", null),
	MILESTONE("milestone", null),
	NAME("name", null),
	DESCRIPTION("description", null),
	ENVIRONMENT("environment", null),
	AUTHOR("author", null),
	SEVERITY("severity", null),
	LABELS("labels", new ArrayList<String>()),
	NOTES("notes", new ArrayList<String>()),
	LINKS("links", new ArrayList<ResultLink>());

	private String key;

	private Object def;

	private Property(String key, Object def) {
		this.key = "reesmo." + key;
		this.def = def;
	}

	public Object get() {
		String value = System.getProperty(key);
		if (value != null) {
			switch (this) {
			case ENABLED:
				return Bool.valueOfString(value);
			case STORAGE:
				if (value.equals(DummyStorage.class.getName()) || value.equals(DummyStorage.class.getSimpleName())
						|| value.equals(DummyStorage.PROPERTY_NAME)) {
					return DummyStorage.class;
				}
				if (value.equals(FileSystemStorage.class.getName())
						|| value.equals(FileSystemStorage.class.getSimpleName())
						|| value.equals(FileSystemStorage.PROPERTY_NAME)) {
					return FileSystemStorage.class;
				}
				if (value.equals(RestApiStorage.class.getName()) || value.equals(RestApiStorage.class.getSimpleName())
						|| value.equals(RestApiStorage.PROPERTY_NAME)) {
					return RestApiStorage.class;
				}
				break;
			case BASE_DIR:
				return new File(value);
			case SEVERITY:
				return TestSeverity.valueOf(value);
			case LABELS:
				return new ArrayList<String>(Arrays.asList(value.split(";")));
			case NOTES:
				return new ArrayList<String>(Arrays.asList(value.split(";")));
			case LINKS:
				return convertStringsToResultLinks(value.split(";"));
			default:
				return value;
			}
		}
		return def;
	}
	
	public Object get(List<Object> configurations) {
		return get(configurations, null);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object get(List<Object> configurations, Object defaultValue) {
		if (configurations == null)
			return get();
		Object value = null;
		Object newValue = null;
		for (Object source : configurations) {
			newValue = get(source, false);
			if (value instanceof List && newValue instanceof List) {
				((List) value).addAll((List) newValue);
			} else if (newValue != null) {
				value = newValue;
			}
		}
		if (value == null) {
			value = get();
		}
		return value == null ? defaultValue : value;
	}

	public Object get(Object configuration) {
		return get(configuration, null);
	}
	
	@SuppressWarnings("unchecked")
	public Object get(Object configuration, Object defaultValue) {
		if (configuration instanceof List) {
			return get((List<Object>) configuration, defaultValue);
		}
		Object value = get(configuration, true);
		return value == null ? defaultValue : value;
	}
	
	public Object get(Object configuration, boolean useSystemProperty) {
		if (configuration == null)
			return useSystemProperty ? get() : null;
		Object value = null;
		ReesmoConfiguration conf = null;
		if (configuration instanceof ReesmoConfiguration) {
			conf = (ReesmoConfiguration) configuration;
		} else if (configuration instanceof Method) {
			conf = ((Method) configuration).getAnnotation(ReesmoConfiguration.class);
		} else if (configuration instanceof Class<?>) {
			conf = ((Class<?>) configuration).getAnnotation(ReesmoConfiguration.class);
		} else {
			conf = configuration.getClass().getAnnotation(ReesmoConfiguration.class);
		}
		if (conf != null) {
			switch (this) {
			case ENABLED:
				value = getFirstValue(conf.enabled());
				break;
			case STORAGE:
				value = getFirstValue(conf.storage());
				break;
			case BASE_DIR:
				value = getFirstValue(conf.baseDir());
				break;
			case PROJECT_ID:
				value = getFirstValue(conf.projectId());
				break;
			case PROJECT_KEY:
				value = getFirstValue(conf.projectKey());
				break;
			case SUITE:
				value = getFirstValue(conf.suite());
				break;
			case SUITE_ID:
				value = getFirstValue(conf.suiteId());
				break;
			case JOB:
				value = getFirstValue(conf.job());
				break;
			case JOB_ID:
				value = getFirstValue(conf.jobId());
				break;
			case ENDPOINT:
				value = getFirstValue(conf.endpoint());
				break;
			case USERNAME:
				value = getFirstValue(conf.username());
				break;
			case PASSWORD:
				value = getFirstValue(conf.password());
				break;
			case MILESTONE:
				value = getFirstValue(conf.milestone());
				break;
			case NAME:
				value = getFirstValue(conf.name());
				break;
			case DESCRIPTION:
				value = getFirstValue(conf.description());
				break;
			case ENVIRONMENT:
				value = getFirstValue(conf.environment());
				break;
			case AUTHOR:
				value = getFirstValue(conf.author());
				break;
			case SEVERITY:
				value = getFirstValue(conf.severity());
				break;
			case LABELS:
				value = new ArrayList<String>(Arrays.asList(conf.labels()));
				break;
			case NOTES:
				value = new ArrayList<String>(Arrays.asList(conf.notes()));
				break;
			case LINKS:
				value = convertStringsToResultLinks(conf.links());
				break;
			default:
				break;
			}
		}
		if (useSystemProperty) {
			return value == null ? get() : value;	
		}
		return value;
	}
	
	private Object getFirstValue(Object[] values) {
		if (values == null || values.length < 1) {
			return null;
		}
		return values[0];
	}
	
	private List<ResultLink> convertStringsToResultLinks(String[] arr) {
		return ((List<String>) Arrays.asList(arr)).stream().filter(s -> s != null).map(s -> convertStringToResultLink(s)).collect(Collectors.toList());
	}
	
	private ResultLink convertStringToResultLink(String s) {
		ResultLink link = new ResultLink();
		String[] parts = s.split("|");
		if (parts.length > 1) {
			link.setName(parts[0]);
			link.setUrl(parts[1]);
		} else {
			link.setUrl(parts[0]);
		}
		return link;
	}

}
