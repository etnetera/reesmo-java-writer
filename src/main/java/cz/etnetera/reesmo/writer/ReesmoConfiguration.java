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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cz.etnetera.reesmo.writer.model.result.TestSeverity;
import cz.etnetera.reesmo.writer.storage.Storage;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ReesmoConfiguration {

	Bool[] enabled() default {};
	
	Class<? extends Storage>[] storage() default {};
	
	String[] baseDir() default {};
	
	String[] projectId() default {};
	
	String[] projectKey() default {};
	
	String[] suite() default {};
	
	String[] suiteId() default {};
	
	String[] job() default {};
	
	String[] jobId() default {};
	
	String[] endpoint() default {};
	
	String[] username() default {};
	
	String[] password() default {};
	
	String[] milestone() default {};
	
	String[] name() default {};
	
	String[] description() default {};
	
	String[] environment() default {};
	
	String[] author() default {};
	
	TestSeverity[] severity() default {};
	
	String[] labels() default {};
	
	String[] notes() default {};
	
	String[] links() default {};
	
}
