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
package frentix.exam

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import frentix._

/**
 * Settings of the test course element:
 * - Show results after test has been submitted
 * - Show score on test homepage
 * - Overview results
 * - Disable option "LMS hidden"
 * 
 * Created by srosse on 25.06.2020
 */
class ExamSimulation extends Simulation {

  val numOfUsers = Integer.getInteger("users", 10)
  val numOfUsersToRendezVous = (numOfUsers.toDouble * 0.7d).toInt
  val ramp = Integer.getInteger("ramp", 10)
  val url = System.getProperty("url", "https://exam.uni-kiel.de")
  //val jump = System.getProperty("jump", "/url/RepositoryEntry/950272/CourseNode/101941541272702") // Gatling SR 1 - 2000
  //val jump = System.getProperty("jump", "/url/RepositoryEntry/1769472/CourseNode/101941664272850") // Gatling SR2 1- 500
  //val jump = System.getProperty("jump", "/url/RepositoryEntry/1769474/CourseNode/101941664272902") // Gtaling SR 3 500 -1000
  //val jump = System.getProperty("jump", "/url/RepositoryEntry/1769476/CourseNode/101941664272971") // Gtaling SR4 1000 - 2000
  val jump = System.getProperty("jump", "/url/RepositoryEntry/1769479/CourseNode/101941664550762") // Gatling SR5 Big 1 - 2000 
  
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
    .inferHtmlResources()
    .silentResources

  val qtiScn = scenario("Test QTI 2.1")
    .exec(LoginPage.loginScreen)
    .pause(1)
    .feed(UsersFeeder.feeder(1, 2000))
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
    .rendezVous(numOfUsersToRendezVous)
		.pause(1, thinksToRendezVous)
    .exec(QTI21TestPage.endTestPart).pause(1)
    .exec(QTI21TestPage.closeTestConfirm).pause(1)
    .exec(QTI21TestPage.confirmCloseTestAndCloseResults).pause(1)
    .exec(LoginPage.logout)


  setUp(qtiScn.inject(rampUsers(numOfUsers) during (ramp seconds))).protocols(httpProtocol)
}
