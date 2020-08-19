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
 * Settings of the test course element:
 * - Show results after test has been submitted
 * - Show score on test homepage
 * - Overview results
 * - Disable option "LMS hidden"
 * 
 * Created by srosse on 09.12.14.
 */
class QTI21Simulation extends Simulation {

  val numOfUsers = Integer.getInteger("users", 100)
  val numOfUsersToRendezVous = (numOfUsers.toDouble * 0.7d).toInt
  val ramp = Integer.getInteger("ramp", 10)
  val url = System.getProperty("url", "http://localhost:8081")
  val jump = System.getProperty("jump", "/url/RepositoryEntry/1007091712/CourseNode/96184839926893")
  val thinks = Integer.getInteger("thinks", 5)
  val thinksToRendezVous = (thinks.toInt * 2)

  val httpProtocol = http
    .baseUrl(url)
    .acceptHeader("text/html,application/json,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("de-de")
    .connectionHeader("keep-alive")
    .userAgentHeader("Mozilla/5.0")
    .warmUp(url)
    //.inferHtmlResources()
    .silentResources

  val qtiScn = scenario("Test QTI 2.1")
    .exec(LoginPage.loginScreen)
    .pause(1)
    .feed(UsersFeeder.feeder(1, numOfUsers))
    .exec(LoginPage.restUrl(jump))
    .exec(QTI21TestPage.login)
    .rendezVous(numOfUsersToRendezVous)
		.pause(1, thinksToRendezVous)
    .exec(QTI21TestPage.startTest)
    .exec(QTI21TestPage.startWithItems).pause(1, thinks)
    .exec(QTI21TestPage.postItem)
    .repeat("${numOfItems}", "itemPos") {
      exec(QTI21TestPage.startItem, QTI21TestPage.postItem).pause(1, thinks)
    }
    .exec(QTI21TestPage.endTestPart).pause(1)
    .exec(QTI21TestPage.closeTestConfirm).pause(1)
    .exec(QTI21TestPage.confirmCloseTestAndCloseResults).pause(1)
    .exec(LoginPage.logout)


  setUp(qtiScn.inject(rampUsers(numOfUsers) during (ramp seconds))).protocols(httpProtocol)
}
