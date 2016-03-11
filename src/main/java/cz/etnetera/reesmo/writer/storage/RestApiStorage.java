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
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.MultipartBody;

import cz.etnetera.reesmo.writer.model.result.Result;
import cz.etnetera.reesmo.writer.model.result.ResultAttachment;

public class RestApiStorage extends Storage {

	public static final String PROPERTY_NAME = "restapi";

	private static final String METHOD_RESULT_CREATE = "/api/results/create";

	private static final String METHOD_RESULT_CREATE_PROJECT_KEY = "/api/results/create/{projectKey}";

	private static final String METHOD_RESULT_DELETE = "/api/results/delete/{resultId}";

	private static final String METHOD_RESULT_ATTACHMENT_CREATE = "/api/results/attachment/create/{resultId}";

	private static final String VIEW_RESULT_DETAIL = "/result/detail/{resultId}";

	private String endpoint;

	private String username;

	private String password;

	public RestApiStorage(String endpoint, String username, String password) throws StorageException {
		if (endpoint == null || endpoint.isEmpty())
			throw new StorageException("Endpoint is null or empty");
		if (username == null || username.isEmpty())
			throw new StorageException("Username is null or empty");
		if (password == null || password.isEmpty())
			throw new StorageException("Password is null or empty");

		this.endpoint = endpoint.replaceAll("/+$", "");
		this.username = username;
		this.password = password;
	}

	@Override
	protected Result createResult(String projectKey, Result result, List<Object> attachments) throws StorageException {
		String url;
		if (projectKey == null)
			url = getUrl(METHOD_RESULT_CREATE);
		else
			url = getUrl(METHOD_RESULT_CREATE_PROJECT_KEY).replace("{projectKey}", projectKey);

		result = requestEntity(result, url);
		getLogger().info("Result created " + result.getId() + " "
				+ getUrl(VIEW_RESULT_DETAIL).replace("{resultId}", result.getId()));

		if (attachments != null) {
			for (Object attachment : attachments) {
				try {
					addResultAttachment(result, attachment);
				} catch (StorageException e) {
					throw new StorageException("Unable to store result attachment", e);
				}
			}
		}

		return result;
	}

	@Override
	protected void deleteResult(Result result) throws StorageException {
		request(getUrl(METHOD_RESULT_DELETE).replace("{resultId}", result.getId()));
	}

	protected void addResultAttachment(final Result result, Object attachment) throws StorageException {
		File file = null;
		String path = null;
		if (attachment instanceof File) {
			file = (File) attachment;
		} else if (attachment instanceof FileWithPath) {
			FileWithPath fileWithPath = (FileWithPath) attachment;
			file = fileWithPath.getFile();
			path = fileWithPath.getPath();
		} else {
			throw new StorageException("Unsupported attachment type " + attachment.getClass());
		}

		if (path != null) {
			path = path.replaceAll("^/+", "").replaceAll("/+$", "");
		}

		if (file.isDirectory()) {
			Path root = file.toPath();
			final String rootPath = path == null ? file.getName() : path;
			try {
				Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						FileVisitResult res = super.visitFile(file, attrs);
						String relativePath = rootPath + "/" + root.relativize(file).normalize().toString();
						try {
							addResultAttachment(result, new FileWithPath(file.toFile(), relativePath));
						} catch (StorageException e) {
							throw new IOException(e);
						}
						return res;
					}
				});
			} catch (IOException e) {
				throw new StorageException(e);
			}
			// directory is not stored, just paths
			return;
		}

		String contentType = URLConnection.guessContentTypeFromName(file.getName());
		if (contentType == null) {
			contentType = "application/octet-stream";
		}
		
		MultipartBody body = Unirest.post(getUrl(METHOD_RESULT_ATTACHMENT_CREATE).replace("{resultId}", result.getId()))
				.basicAuth(username, password).header("Accept", "application/json").field("file", file, contentType);
		if (path != null) {
			body.field("path", path);
		}

		HttpResponse<String> response;
		try {
			response = body.asString();
		} catch (UnirestException e) {
			throw new StorageException("Unable to store result attachment", e);
		}

		if (response.getStatus() != 200) {
			throw new StorageException("Wrong status code when storing result attachment " + response.getStatus());
		}

		ResultAttachment resultAttachment = null;
		try {
			resultAttachment = new ObjectMapper().readValue(response.getBody(), ResultAttachment.class);
		} catch (UnsupportedOperationException | IOException e) {
			throw new StorageException("Unable to parse result attachment from response", e);
		}

		getLogger().info("Result attachment stored " + resultAttachment.getId());
	}

	protected String getUrl(String uri) {
		return endpoint + uri;
	}

	@SuppressWarnings("unchecked")
	protected <T> T requestEntity(T entity, String url) throws StorageException {
		HttpResponse<String> response;
		try {
			response = Unirest.post(url).basicAuth(username, password).header("Accept", "application/json")
					.header("Content-Type", "application/json; charset=UTF-8")
					.body(new ObjectMapper().writeValueAsString(entity)).asString();
		} catch (UnirestException | JsonProcessingException e) {
			throw new StorageException("Unable to execute entity request on url " + url, e);
		}

		if (response.getStatus() != 200) {
			throw new StorageException(
					"Wrong status code " + response.getStatus() + " when requesting entity url " + url);
		}

		try {
			entity = (T) new ObjectMapper().readValue(response.getBody(), entity.getClass());
		} catch (UnsupportedOperationException | IOException e) {
			throw new StorageException("Unable to parse result from response while requesting url " + url, e);
		}

		return entity;
	}

	protected void request(String url) throws StorageException {
		HttpResponse<String> response;
		try {
			response = Unirest.get(url).basicAuth(username, password).asString();
		} catch (UnirestException e) {
			throw new StorageException("Unable to execute request on url " + url, e);
		}

		if (response.getStatus() != 200) {
			throw new StorageException("Wrong status code " + response.getStatus() + " when requesting url " + url);
		}
	}

}
