/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
import io.gatling.app.Gatling;
import io.gatling.core.config.GatlingPropertiesBuilder;

/**
 * 
 * Initial date: 29 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Engine {

	public static void main(String[] args) {
		GatlingPropertiesBuilder props = new GatlingPropertiesBuilder()
			.resourcesDirectory(IDEPathHelper.mavenResourcesDirectory.toString())
			.resultsDirectory(IDEPathHelper.resultsDirectory.toString())
			.binariesDirectory(IDEPathHelper.mavenBinariesDirectory.toString())
			.simulationClass("org.olat.gatling.QTI21Simulation")
			//.simulationClass("org.olat.gatling.ExamSimulation")
			//.simulationClass("org.olat.gatling.OOSimulation")
		;

		Gatling.fromMap(props.build());
	}
}
