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

	private val numOfUsers = Integer.getInteger("users", 100)
	private val ramp = Integer.getInteger("ramp", 60)
	private val url = System.getProperty("url", "http://localhost:8080")
	private val thinks = Integer.getInteger("thinks", 5)
	private val rate = numOfUsers.toDouble / ramp

	private val httpProtocol = http
		.baseUrl(url)
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("de-de")
		.connectionHeader("keep-alive")
		.userAgentHeader("Mozilla/5.0")
		//.inferHtmlResources()
		
	private val uibkScn = scenario("UIBK like")
		.exec(LoginPage.loginScreen)
		.pause(thinks)
		.feed(UsersFeeder.feeder(1, numOfUsers))
		.exec(LoginPage.loginToMyCourses)
		//.repeat(5, "n") { exec(GroupPage.myGroupsAndSelectCourse(thinks)) }
		.repeat(5, "n") {
			//exec(CoursePage.selectCourseNavigateAndBack(thinks, thinks))
			exec(CoursePage.selectCourseAndBack(thinks))
		}
  	  .pause(5)
  	  .exec(LoginPage.logout)
  	  

	setUp(uibkScn.inject(
	    constantUsersPerSec(rate.toDouble) during ramp.seconds
	    
	)).protocols(httpProtocol)
	
}