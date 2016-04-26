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
package cz.etnetera.reesmo.writer.storage;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.etnetera.reesmo.writer.Bool;
import cz.etnetera.reesmo.writer.Property;
import cz.etnetera.reesmo.writer.model.result.Result;
import cz.etnetera.reesmo.writer.model.result.ResultLink;
import cz.etnetera.reesmo.writer.model.result.TestSeverity;
import cz.etnetera.reesmo.writer.model.result.TestStatus;

abstract public class Storage {

	@SuppressWarnings("unchecked")
	public static Storage newInstance(Object configuration) throws StorageException {
		try {
			Class<? extends Storage> clazz = null;
			if (Bool.FALSE.equals(Property.ENABLED.get(configuration))) {
				clazz = DummyStorage.class;
			} else {
				clazz = (Class<? extends Storage>) Property.STORAGE.get(configuration);
			}
			if (clazz.isAssignableFrom(DummyStorage.class)) {
				return new DummyStorage();
			}
			if (clazz.isAssignableFrom(FileSystemStorage.class)) {
				return new FileSystemStorage((File) Property.BASE_DIR.get(configuration));
			}
			if (clazz.isAssignableFrom(RestApiStorage.class)) {
				return new RestApiStorage((String) Property.ENDPOINT.get(configuration),
						(String) Property.USERNAME.get(configuration),
						(String) Property.PASSWORD.get(configuration));
			}
			throw new StorageException("Unsupported storage type: " + clazz);
		} catch (Exception e) {
			throw new StorageException("Failed to create storage instance", e);
		}
	}
	
	/**
	 * Store result.
	 * 
	 * @param result
	 * @return
	 */
	public Result addResult(Result result) throws StorageException {
		return addResult((String) null, result, null);
	}
	
	/**
	 * Store result with attachments.
	 * 
	 * @param result
	 * @param attachments
	 * @return
	 */
	public Result addResult(Result result, List<Object> attachments) throws StorageException {
		return addResult((String) null, result, attachments);
	}

	/**
	 * Store result under given project key.
	 * 
	 * @param projectKey
	 * @param result
	 * @return
	 */
	public Result addResult(String projectKey, Result result) throws StorageException {
		return addResult(projectKey, result, null);
	}
	
	/**
	 * Store result gathering information from configuration.
	 * 
	 * @param configuration
	 * @param result
	 * @return
	 */
	public Result addResult(Object configuration, Result result) throws StorageException {
		return addResult((String) Property.PROJECT_KEY.get(configuration), updateResultFromConfigurations(Arrays.asList(new Object[]{configuration}), result), null);
	}
	
	/**
	 * Store result with attachments gathering information from configuration.
	 * 
	 * @param configuration
	 * @param result
	 * @param attachments
	 * @return
	 */
	public Result addResult(Object configuration, Result result, List<Object> attachments) throws StorageException {
		return addResult((String) Property.PROJECT_KEY.get(configuration), updateResultFromConfigurations(Arrays.asList(new Object[]{configuration}), result), attachments);
	}
	
	/**
	 * Store result gathering information from configurations.
	 * 
	 * @param configurations
	 * @param result
	 * @return
	 */
	public Result addResult(List<Object> configurations, Result result) throws StorageException {
		return addResult((String) Property.PROJECT_KEY.get(configurations), updateResultFromConfigurations(configurations, result), null);
	}
	
	/**
	 * Store result with attachments gathering information from configurations.
	 * 
	 * @param configurations
	 * @param result
	 * @param attachments
	 * @return
	 */
	public Result addResult(List<Object> configurations, Result result, List<Object> attachments) throws StorageException {
		return addResult((String) Property.PROJECT_KEY.get(configurations), updateResultFromConfigurations(configurations, result), attachments);
	}
	
	/**
	 * Store result with attachments under given project key.
	 * 
	 * @param projectKey
	 * @param result
	 * @param attachments
	 * @return
	 */
	public Result addResult(String projectKey, Result result, List<Object> attachments) throws StorageException {
		try {
			prepareResultBeforeCreate(result);
			validateResult(result);
			if (result.getProjectId() == null || result.getProjectId().trim().isEmpty()) {
				if (projectKey == null)
					projectKey = (String) Property.PROJECT_KEY.get();
				if (projectKey == null || projectKey.trim().isEmpty())
					throw new StorageException("Both result project key and id are empty");
			}
			result = createResult(projectKey, result, attachments);
			getLogger().info("Result added " + result.getName() + " " + result.getId());
		} catch (Exception e) {
			try {
				if (result != null && result.getId() != null) {
					getLogger().info("Deleting result " + result.getId());
					deleteResult(result);
					getLogger().info("Result deleted " + result.getId());
				}
			} catch (Exception e2) {
				getLogger().error("Failed to delete result after failing to add result", e2);
			}
			throw new StorageException("Failed to add result", e);
		}
		return result;
	}
	
	abstract protected Result createResult(String projectKey, Result result, List<Object> attachments) throws StorageException;
	
	abstract protected void deleteResult(Result result) throws StorageException;
	
	protected void prepareResultBeforeCreate(Result result) {
		if (result == null)
			return;
		if (result.getProjectId() == null)
			result.setProjectId((String) Property.PROJECT_ID.get());
		if (result.getEndedAt() == null)
			result.setEndedAt(new Date());
		if (result.getStatus() == null)
			result.setStatus(TestStatus.PASSED);
		if (result.getSeverity() == null)
			result.setSeverity(TestSeverity.NORMAL);
	}
	
	protected void validateResult(Result result) throws StorageException {
		if (result == null)
			throw new StorageException("Result is required");
		if (result.getId() != null)
			throw new StorageException("Result id is already defined [" + result.getId() + "]");
		if (result.getName() == null)
			throw new StorageException("Result name is required");
		if (result.getSuite() != null && result.getSuiteId() == null)
			throw new StorageException("Both suite and suite id are required not only suite [" + result.getSuite() + "]");
		if (result.getSuiteId() != null && result.getSuite() == null)
			throw new StorageException("Both suite and suite id are required not only suite id [" + result.getSuiteId() + "]");
		if (result.getJob() != null && result.getJobId() == null)
			throw new StorageException("Both job and job id are required not only job [" + result.getJob() + "]");
		if (result.getJobId() != null && result.getJob() == null)
			throw new StorageException("Both job and job id are required not only job id [" + result.getJobId() + "]");
		if (result.getStartedAt() == null)
			throw new StorageException("Result started at is required");
		if (result.getEndedAt() == null)
			throw new StorageException("Result ended at is required");
		if (result.getStatus() == null)
			throw new StorageException("Result status is required");
		if (result.getSeverity() == null)
			throw new StorageException("Result severity is required");
		
		if (result.getStartedAt().after(result.getEndedAt()))
			throw new StorageException("Result ended before it started [" + result.getStartedAt() + ", " + result.getEndedAt() + "]");
		
		if (result.getLinks() != null) {
			for (int i = 0; i < result.getLinks().size(); i++) {
				ResultLink link = result.getLinks().get(i);
				if (link == null)
					throw new StorageException("Result link " + i + " is null");
				if (link.getUrl() == null || "".equals(link.getUrl()))
					throw new StorageException("Result link " + i + " url is required");
				try {
					new URL(link.getUrl());
				} catch (MalformedURLException e) {
					throw new StorageException("Result link " + i + " url is invalid [" + link.getUrl() + "]");
				}					
			}
			
		}
	}
	
	protected Logger getLogger() {
		return LoggerFactory.getLogger(getClass());
	}
	
	@SuppressWarnings("unchecked")
	protected Result updateResultFromConfigurations(List<Object> configurations, Result result) {
		result.setSuite((String) Property.SUITE.get(configurations, result.getSuite()));
		result.setSuiteId((String) Property.SUITE_ID.get(configurations, result.getSuiteId()));
		result.setJob((String) Property.JOB.get(configurations, result.getJob()));
		result.setJobId((String) Property.JOB_ID.get(configurations, result.getJobId()));
		result.setMilestone((String) Property.MILESTONE.get(configurations, result.getMilestone()));
		result.setName((String) Property.NAME.get(configurations, result.getName()));
		result.setDescription((String) Property.DESCRIPTION.get(configurations, result.getDescription()));
		result.setEnvironment((String) Property.ENVIRONMENT.get(configurations, result.getEnvironment()));
		result.setAuthor((String) Property.AUTHOR.get(configurations, result.getAuthor()));
		result.setSeverity((TestSeverity) Property.SEVERITY.get(configurations, result.getSeverity()));
		
		List<String> labels = (List<String>) Property.LABELS.get(configurations);
		if (result.getLabels() != null) labels.addAll(result.getLabels());
		result.setLabels(labels);
		
		List<String> notes = (List<String>) Property.NOTES.get(configurations);
		if (result.getNotes() != null) notes.addAll(result.getNotes());
		result.setNotes(notes);
		
		List<ResultLink> links = (List<ResultLink>) Property.LINKS.get(configurations);
		if (result.getLinks() != null) links.addAll(result.getLinks());
		result.setLinks(links);
		
		return result;
	}

}
