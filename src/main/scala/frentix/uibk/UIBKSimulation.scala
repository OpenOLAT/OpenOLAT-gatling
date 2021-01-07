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
package frentix.uibk

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import frentix.CoursePage
import frentix.LoginPage
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder

/**
 * This is the scenario used in the pre-launch phase of
 * OpenOLAT at UIBK in 2014 with 10'000 users within 8
 * minutes
 */
class UIBKSimulation extends Simulation {

	val httpProtocol = http
		.baseUrl("https://kivik.frentix.com")
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("de-de")
		.connectionHeader("keep-alive")
		.userAgentHeader("Mozilla/5.0")
		
		
	val wholetime = 480
	val numberoframpsteps = 10
	val singleramptime = wholetime / numberoframpsteps

	val uibkScn = scenario("UIBK like")
		.exec(LoginPage.loginScreen)
		.pause(1)		
		.feed(csv("oo_user_credentials.csv"))
		.exec(LoginPage.loginToMyCourses)
		.pause(100.milliseconds)
		.repeat(5, "n") {
			exec(CoursePage.selectCourseAndBack(5))
		}
  	.pause(5)
  	.exec(LoginPage.logout)


	setUp(uibkScn.inject(
		rampUsers(100) during (singleramptime.seconds),
		rampUsers(300) during (singleramptime.seconds),
		rampUsers(500) during (singleramptime.seconds),
		rampUsers(700) during (singleramptime.seconds),
		rampUsers(900) during (singleramptime.seconds),
		rampUsers(1100) during (singleramptime.seconds),
		rampUsers(1300) during (singleramptime.seconds),
		rampUsers(1500) during (singleramptime.seconds),
		rampUsers(1700) during (singleramptime.seconds),
		rampUsers(1900) during (singleramptime.seconds)
	)).protocols(httpProtocol)
		
	
	
}