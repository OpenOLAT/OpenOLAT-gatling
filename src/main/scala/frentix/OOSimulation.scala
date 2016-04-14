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
package frentix

import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
 * You can start this simulation with the following
 * command line:<br/>
 * mvn gatling:execute -Dusers=100 -Dramp=50 -Durl=http://localhost:8080 -Dgatling.simulationClass=frentix.OOSimulation<br/>
 *
 */
class OOSimulation extends Simulation {

	val numOfUsers = Integer.getInteger("users", 1)
	val ramp = Integer.getInteger("ramp", 50)
	val url = System.getProperty("url", "http://localhost:8080")
	val thinks = Integer.getInteger("thinks", 5)

	val httpProtocol = http
		.baseURL(url)
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("de-de")
		.connection("keep-alive")
		.userAgentHeader("Mozilla/5.0")
		
	val uibkScn = scenario("UIBK like")
		.exec(LoginPage.loginScreen)
		.pause(1)		
		.feed(csv("oo_user_credential.csv"))
		.exec(LoginPage.loginToMyCourses)
		/*.repeat(5, "n") {
		  exec(GroupPage.myGroupsAndSelectCourse(thinks))
	  }*/
		.repeat(5, "n") {
			exec(CoursePage.selectCourseNavigateAndBack(thinks, thinks))
		}
  	.pause(5)
  	.exec(LoginPage.logout)

	setUp(uibkScn.inject(rampUsers(numOfUsers) over (ramp seconds))).protocols(httpProtocol)
	
}