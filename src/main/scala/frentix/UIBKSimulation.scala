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
import io.gatling.core.session.Expression
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * This is the scenario used in the pre-launch phase of
 * OpenOLAT at UIBK in 2014 with 10'000 users within 8
 * minutes
 */
class UIBKSimulation extends Simulation {

	val httpProtocol = http
		.baseURL("https://kivik.frentix.com")
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("de-de")
		.connection("keep-alive")
		.userAgentHeader("Lynx")
		
		
	val wholetime = 480;
	val numberoframpsteps = 10;
	val singleramptime = wholetime / numberoframpsteps;

	val uibkScn = scenario("UIBK like")
		.exec(LoginPage.loginScreen)
		.pause(1)		
		.feed(csv("oo_user_credentials_small.csv"))
		.exec(LoginPage.login)

		.exec(CoursePage.selectCourseAndBack(0, 5))
		.exec(CoursePage.selectCourseAndBack(1, 5))
		.exec(CoursePage.selectCourseAndBack(2, 5))
		.exec(CoursePage.selectCourseAndBack(3, 5))
		.exec(CoursePage.selectCourseAndBack(4, 5))

  		.pause(5)
  		.exec(LoginPage.logout)


	setUp(uibkScn.inject(
		rampUsers(100) over (singleramptime seconds),
		rampUsers(300) over (singleramptime seconds),
		rampUsers(500) over (singleramptime seconds),
		rampUsers(700) over (singleramptime seconds),
		rampUsers(900) over (singleramptime seconds),
		rampUsers(1100) over (singleramptime seconds),
		rampUsers(1300) over (singleramptime seconds),
		rampUsers(1500) over (singleramptime seconds),
		rampUsers(1700) over (singleramptime seconds),
		rampUsers(1900) over (singleramptime seconds)
	)).protocols(httpProtocol)
		
	
	
}