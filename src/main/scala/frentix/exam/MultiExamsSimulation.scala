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
import io.gatling.core.structure._
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
class MultiExamsSimulation extends Simulation {

  private val numOfUsers = Integer.getInteger("users", 10)
  private val numOfUsersToRendezVous = (numOfUsers.doubleValue() * 0.7d).toInt
  private val ramp = Integer.getInteger("ramp", 10)
  private val url :String = System.getProperty("url", "http://localhost:8081")
  private val thinks = Integer.getInteger("thinks", 5)
  private val thinksToRendezVous = thinks.intValue() * 2

  private val httpProtocol = http
    .baseUrl(url)
    .acceptHeader("text/html,application/json,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("de-de")
    .connectionHeader("keep-alive")
    .userAgentHeader("Mozilla/5.0")
    .warmUp(url)
    .inferHtmlResources()
    .silentResources
    
  private val qti1Scn = exam("Test Gatling SR", "/url/RepositoryEntry/950272/CourseNode/101941541272702", 1500, 500)
  private val qti2Scn = exam("Test Gatling SR2", "/url/RepositoryEntry/1769472/CourseNode/101941664272850", 1, 500)
  private val qti3Scn = exam("Test Gatling SR3", "/url/RepositoryEntry/1769474/CourseNode/101941664272902", 500, 500)
  private val qti4Scn = exam("Test Gatling SR4", "/url/RepositoryEntry/1769476/CourseNode/101941664272971", 1000, 1000)
   
  setUp(
      qti1Scn.inject(rampUsers(numOfUsers.intValue()) during ramp.seconds),
      qti2Scn.inject(rampUsers(numOfUsers.intValue()) during ramp.seconds),
      qti3Scn.inject(rampUsers(numOfUsers.intValue()) during ramp.seconds),
      qti4Scn.inject(rampUsers(numOfUsers.intValue()) during ramp.seconds)
  ).protocols(httpProtocol)
  
  def exam(scenarioName:String, jumpTo:String, firstUser:Int, maxUsers:Int) : ScenarioBuilder = scenario(scenarioName)
    .exec(LoginPage.loginScreen)
    .pause(1)
    .feed(UsersFeeder.feeder(firstUser, maxUsers))
    .exec(LoginPage.restUrl(jumpTo))
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
    
    
}
