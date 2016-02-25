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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.etnetera.reesmo.writer.model.Model;
import cz.etnetera.reesmo.writer.model.result.Result;

/**
 * Stores results in given directory. It should be used if you do not want to
 * slow down your testing. Results can be propagated to server later using
 * {@link RestApiStorage}.
 */
public class FileSystemStorage extends Storage {

	public static final String PROPERTY_NAME = "filesystem";
	
	private File baseDir;

	public FileSystemStorage(File baseDir) throws StorageException {
		if (baseDir == null) 
			throw new StorageException("Base directory is null");
		if (!baseDir.exists())
			if (baseDir.mkdirs()) {
				getLogger().info("Base directory was created: " + baseDir);
			} else {
				throw new StorageException("Base directory does not exists and can not be created: " + baseDir);
			}
		if (!baseDir.canWrite())
			throw new StorageException("Base directory is not writeable: " + baseDir);
		this.baseDir = baseDir;
	}

	@Override
	protected Result createResult(String projectKey, Result result, List<Object> attachments) throws StorageException {
		File baseDir = this.baseDir;
		File resultDir = createModelDir(baseDir, result);
		
		createModelFile(resultDir, result);
		if (projectKey != null)
			createModelProjectKeyFile(resultDir, result, projectKey);
		
		if (attachments != null && !attachments.isEmpty()) {
			File resultAttachmentDir = createResultAttachmentDir(resultDir);
			for (Object attachment : attachments) {
				if (attachment instanceof File) {
					try {
						Files.copy(((File) attachment).toPath(), resultAttachmentDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						throw new StorageException("Unable to copy result attachment file: " + attachment, e);
					}
				} else if (attachment instanceof FileWithPath) {
					FileWithPath file = (FileWithPath) attachment;
					File targetFile = new File(resultAttachmentDir, file.getPath());
					targetFile.mkdirs();
					try {
						Files.copy(file.getFile().toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						throw new StorageException("Unable to copy result attachment file: " + attachment, e);
					}
				} else {
					throw new StorageException("Unsupported attachment type: " + attachment.getClass());
				}
			}
		}
		
		createModelReadyFile(resultDir, result);
		result.setId(createModelId(resultDir));
		
		return result;
	}
	
	@Override
	protected void deleteResult(Result result) throws StorageException {
		File resultDir;
		try {
			resultDir = convertModelIdToDir(result.getId());
		} catch (StorageException e) {
			throw new StorageException("Result not found, it must be added before deleting");
		}
		deleteModelDir(resultDir, result);
	}
	
	protected File createResultAttachmentDir(File resultDir) {
		File attachmentDir = new File(resultDir, "attachments");
		attachmentDir.mkdir();
		getLogger().info("Result attachment directory created: " + attachmentDir);
		return attachmentDir;
	}

	protected File createModelDir(File baseDir, Model model) {
		String dirName = String.valueOf(new Date().getTime());
		int increment = 0;
		File dir = null;
		do {
			if (increment == 0) {
				dir = new File(baseDir, dirName);
			} else {
				dir = new File(baseDir, dirName + "-" + increment);
			}
			increment++;
		} while (dir.exists());

		dir.mkdir();
		getLogger().info(getModelName(model) + " directory created: " + dir);
		return dir;
	}

	protected File createModelFile(File modelDir, Model model) throws StorageException {
		try {
			File jsonFile = getModelJsonFile(modelDir, model);
			new ObjectMapper().writeValue(jsonFile, model);
			getLogger().info(getModelName(model) + " json file created: " + jsonFile);
			return jsonFile;
		} catch (IOException e) {
			throw new StorageException("Unable to create " + getModelName(model) + " json file", e);
		}
	}
	
	protected File createModelReadyFile(File modelDir, Model model) throws StorageException {
		try {
			File readyFile = getModelReadyFile(modelDir);
			readyFile.createNewFile();
			getLogger().info(getModelName(model) + " ready file created: " + readyFile);
			return readyFile;
		} catch (IOException e) {
			throw new StorageException("Unable to create " + getModelName(model) + " ready file", e);
		}
	}
	
	protected File createModelProjectKeyFile(File modelDir, Model model, String projectKey) throws StorageException {
		try {
			File projectFile = getModelProjectKeyFile(modelDir);
			Files.write(projectFile.toPath(), projectKey.getBytes());
			getLogger().info(getModelName(model) + " project key file created: " + projectFile);
			return projectFile;
		} catch (IOException e) {
			throw new StorageException("Unable to create " + getModelName(model) + " project key file", e);
		}
	}

	protected File getModelJsonFile(File modelDir, Model model) {
		return new File(modelDir, getModelName(model) + ".json");
	}
	
	protected File getModelReadyFile(File modelDir) {
		return new File(modelDir, "ready");
	}
	
	protected File getModelProjectKeyFile(File modelDir) {
		return new File(modelDir, "projectkey");
	}
	
	protected boolean deleteModelDir(File modelDir, Model model) {
		getLogger().info("Deleting " + getModelName(model) + " directory: " + modelDir.getName());
		return deleteDir(modelDir);
	}
	
	protected boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(children[i]);
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
	
	protected File convertModelIdToDir(String modelId) throws StorageException {
		if (modelId == null) {
			throw new StorageException("Model directory is uknown because of empty model id");
		}
		File file = getModelIdDir(modelId);
		if (!file.exists())
			throw new StorageException("Model directory does not exists: " + file);
		if (!file.isDirectory())
			throw new StorageException("Model directory is not directory: " + file);
		if (!file.canWrite())
			throw new StorageException("Model directory is not writable: " + file);
		return file;
	}
	
	protected File getModelIdDir(String modelId) {
		return new File(baseDir, modelId);
	}
	
	protected String createModelId(File modelDir) {
		return baseDir.toPath().relativize(modelDir.toPath()).normalize().toString().replaceAll(File.separator, "/");
	}
	
	protected String getModelName(Model model) {
		return model.getClass().getSimpleName();
	}

}
