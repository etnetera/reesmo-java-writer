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

public enum TestStatus {

	/**
	 * When a test is run and the actual outcome does not match the expected
	 * out-come.
	 */
	FAILED,

	/**
	 * When a test is run, an error that keeps the test from running to
	 * completion. The error may be explicitly raised or thrown by the system
	 * under test (SUT) or by the test itself, or it may be thrown by the
	 * runtime system (e.g., operating system, virtual machine). In general, it
	 * is much easier to debug a test error than a test failure because the
	 * cause of the problem tends to be much more local to where the test error
	 * occurs. Compare with test failure and test success.
	 */
	BROKEN, 
	
	/**
	 * Test that did not run for some reason, e.g. test dependencies.
	 */
	SKIPPED,

	/**
	 * A situation in which a test is run and all actual outcomes match the
	 * expected outcomes. Compare with test failure and test error.
	 */
	PASSED;

}
