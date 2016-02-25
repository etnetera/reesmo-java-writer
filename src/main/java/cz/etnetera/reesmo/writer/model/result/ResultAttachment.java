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

import cz.etnetera.reesmo.writer.model.AuditedModel;

public class ResultAttachment extends AuditedModel {

    private String id;
	
	private String name;
	
	private String path;
	
	private String contentType;
	
	private long size;

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public String getContentType() {
		return contentType;
	}

	public long getSize() {
		return size;
	}
	
}
