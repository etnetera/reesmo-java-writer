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

import java.util.List;

import cz.etnetera.reesmo.writer.model.result.Result;

/**
 * Dummy suite
 */
public class DummyStorage extends Storage {
	
	public static final String PROPERTY_NAME = "dummy";

	@Override
	public Result addResult(String projectKey, Result result, List<Object> attachments)
			throws StorageException {
		return result;
	}

	@Override
	protected Result createResult(String projectKey, Result result, List<Object> attachments) throws StorageException {
		return result;
	}

	@Override
	protected void deleteResult(Result result) throws StorageException {}

}
