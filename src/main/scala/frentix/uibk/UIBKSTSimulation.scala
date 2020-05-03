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
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder

/**
 * This is the scenario used to reproduce the uibk lms
 * fallout on 15.1.2016 with 400 users logging in
 * in a timeframe of 70 seconds and 200 users
 * starting a test within 10 seconds.
 */
class UIBKSTSimulation extends Simulation {

	val httpProtocol = http
		.baseUrl("http://localhost:8080")
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("de-de")
		.connectionHeader("keep-alive")
		.userAgentHeader("Lynx")
		
	val headers = Map("""Accept""" -> """*/*""")
	val headers_post = Map(
		"""Content-Type""" -> """application/x-www-form-urlencoded""",
		"""Origin""" -> """http://localhost:8080""")
		
	val uibkScn = scenario("UIBK ST")
		.exec(
			http("Login Screen")
				.get("""/dmz/""")
				.headers(headers)
				.check(status.is(200))
				.check(regex("""o_fiooolat_login_button""")))
		.pause(1, 2)		
		.feed(csv("oo_user_credentials.csv"))
		.exec(
			http("Login to my courses")
				.post("""/dmz/1:1:oolat_login:1:0:ofo_:fid/""")
				.headers(headers_post)
				.formParam("""dispatchuri""", """undefined""")
				.formParam("""dispatchevent""", """undefined""")
				.formParam("""o_fiooolat_login_name""", "${username}")
				.formParam("""o_fiooolat_login_pass""", "${password}")
				.check(status.is(200))
				.check(css(".o_logout", "href").saveAs("logoutlink")))
		.rendezVous(700)
		.pause(1, 10)
		.exec(
			http("openRepositoryEntry")
				.get("/url/RepositoryEntry/349437952")
				.headers(headers)
				.check(regex("""Gatling UIBK"""))
				.check(css("""span.o_tree_link a[title=Onlineklausur]""","href")
						.find(0)
						.saveAs("href_node_klausur"))
				.check(status.is(200)))
                .pause(1, 2)
                .exec(  
                        http("openNodeKlausur")
                                .get("${href_node_klausur}")
                                .headers(headers)
                                .check(css("""div.o_button_group a.btn""","href")
                                		.find(0)
                                		.saveAs("href_test_start"))
                                .check(status.is(200)))
                .pause(1, 2)
                .exec(  
                        http("startTest")
                                .get("${href_test_start}")
                                .headers(headers)
                                .check(css("""#o_qti_run_title a.btn""","href")
                                		.find(0)
                                		.saveAs("href_test_quit"))
                                .check(status.is(200)))
                .pause(2, 8)
                .exec(  
                        http("quitTest")
                                .get("${href_test_quit}")
                                .headers(headers)
                                .check(css("""#o_qti_run_title a.btn""","href")
                                		.find(0)
                                		.saveAs("href_test_close"))
                                .check(status.is(200)))
                .pause(2, 8)
                .exec(  
                        http("closeTest")
                                .get("${href_test_close}")
                                .headers(headers)
                                .check(status.is(200)))
		.pause(5)
		.exec(
			http("Logout")
				.get("${logoutlink}")
				.headers(headers)
				.check(status.is(200))
				.check(regex("""o_fiooolat_login_button""")))

	setUp(uibkScn.inject(
		rampUsers(1000) during (70)
	)).protocols(httpProtocol)

}
