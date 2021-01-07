/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */

import java.nio.file.Path
import java.nio.file.Paths


object IDEPathHelper {

	val gatlingConfUrl: Path = Paths.get(getClass.getClassLoader.getResource("gatling.conf").toURI)
	val projectRootDir = gatlingConfUrl.getParent.getParent.getParent

	val mavenSourcesDirectory = projectRootDir.resolve("src").resolve("test").resolve("scala")
	val mavenResourcesDirectory = projectRootDir.resolve("src").resolve("test").resolve("resources")
	val mavenTargetDirectory = projectRootDir.resolve("target")
	val mavenBinariesDirectory = mavenTargetDirectory.resolve("test-classes")

	val dataDirectory = mavenResourcesDirectory.resolve("data")
	val bodiesDirectory = mavenResourcesDirectory.resolve("bodies")

	val recorderOutputDirectory = mavenSourcesDirectory
	val resultsDirectory = mavenTargetDirectory.resolve("results")

	val recorderConfigFile = mavenResourcesDirectory.resolve("recorder.conf")
}