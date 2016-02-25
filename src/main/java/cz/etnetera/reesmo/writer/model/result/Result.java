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
package cz.etnetera.reesmo.writer.model.result;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import cz.etnetera.reesmo.writer.model.AuditedModel;

public class Result extends AuditedModel {
	
	private String id;
	
	private String projectId;
	
	/**
	 * Suite name.
	 */
	private String suite;
	
	/**
	 * Unique suite run identifier. It should be unique
	 * only for given suite. Best way is to use timestamp.
	 */
	private String suiteId;
	
	/**
	 * Name for job, i.e. group of tests. It will usually be set 
	 * in CI or build management tool and it indicates
	 * job name which is running all the tests including
	 * suites. For example in Teamcity it will be the name 
	 * of Build, in Jenkins the name of Job.
	 */
	private String job;
	
	/**
	 * Unique job run identifier. It should be unique
	 * only for given job. Best way is to use timestamp.
	 */
	private String jobId;
	
	private String milestone;
	
	private String name;
	
	private String description;
	
	private String environment;
	
	private String author;
	
	private Date startedAt;
	
	private Date endedAt;
	
	private Long length;
	
	private TestStatus status;
	
	private TestSeverity severity;
	
	private boolean automated;
	
	private List<String> labels = new ArrayList<>();
	
	private List<String> notes = new ArrayList<>();
	
	private List<String> errors = new ArrayList<>();
	
	private List<TestCategory> categories = new ArrayList<>();
	
	private List<TestType> types = new ArrayList<>();
	
	private List<ResultAttachment> attachments = new ArrayList<>();
	
	private List<ResultLink> links = new ArrayList<>();

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getSuite() {
		return suite;
	}

	public void setSuite(String suite) {
		this.suite = suite;
	}
	
	public String getSuiteId() {
		return suiteId;
	}

	public void setSuiteId(String suiteId) {
		this.suiteId = suiteId;
	}

	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getMilestone() {
		return milestone;
	}

	public void setMilestone(String milestone) {
		this.milestone = milestone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}

	public Date getEndedAt() {
		return endedAt;
	}

	public void setEndedAt(Date endedAt) {
		this.endedAt = endedAt;
	}

	public Long getLength() {
		return length;
	}

	public void setLength(Long length) {
		this.length = length;
	}

	public TestStatus getStatus() {
		return status;
	}

	public void setStatus(TestStatus status) {
		this.status = status;
	}

	public TestSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(TestSeverity severity) {
		this.severity = severity;
	}

	public boolean isAutomated() {
		return automated;
	}

	public void setAutomated(boolean automated) {
		this.automated = automated;
	}

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public List<String> getNotes() {
		return notes;
	}

	public void setNotes(List<String> notes) {
		this.notes = notes;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}

	public List<TestCategory> getCategories() {
		return categories;
	}

	public void setCategories(List<TestCategory> categories) {
		this.categories = categories;
	}

	public List<TestType> getTypes() {
		return types;
	}

	public void setTypes(List<TestType> types) {
		this.types = types;
	}

	public List<ResultAttachment> getAttachments() {
		return attachments;
	}

	public List<ResultLink> getLinks() {
		return links;
	}

	public void setLinks(List<ResultLink> links) {
		this.links = links;
	}
	
	public Result addLabel(String label) {
		labels.add(label);
		return this;
	}
	
	public Result addNote(String note) {
		notes.add(note);
		return this;
	}
	
	public Result addError(String error) {
		errors.add(error);
		return this;
	}
	
	public Result addError(Throwable error) {
		errors.add(createErrorFromThrowable(error));
		return this;
	}
	
	public void setThrowables(List<Throwable> errors) {
		this.errors = errors.stream().map(e -> createErrorFromThrowable(e)).collect(Collectors.toList());
	}
	
	public Result addCategory(TestCategory category) {
		categories.add(category);
		return this;
	}
	
	public Result addType(TestType type) {
		types.add(type);
		return this;
	}
	
	public Result addLink(ResultLink link) {
		links.add(link);
		return this;
	}
	
	private String createErrorFromThrowable(Throwable error) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%s: %s", error.getClass().getSimpleName(), error.getMessage()));
		sb.append("\n");

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		error.printStackTrace(pw);
		sb.append(sw.getBuffer());
		
		return sb.toString();
	}
	
}
