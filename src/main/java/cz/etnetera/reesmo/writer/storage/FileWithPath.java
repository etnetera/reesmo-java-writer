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

public class FileWithPath {

	protected File file;
	
	protected String path;

	public FileWithPath(File file, String path) {
		this.file = file;
		this.path = path;
	}

	public File getFile() {
		return file;
	}

	public String getPath() {
		return path;
	}

	@Override
	public String toString() {
		return "File: " + file + ", Path: " + path;
	}

}