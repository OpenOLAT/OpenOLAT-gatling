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


object OOLoginLogout  {

	val headers = Map("""Accept""" -> """*/*""")

	val headers_post = Map(
		"""Content-Type""" -> """application/x-www-form-urlencoded""",
		"""Origin""" -> """http://localhost""")

	val scn = scenario("OpenOLAT Login")
		.exec(
			http("Login Screen")
				.get("""/dmz/""")
				.headers(headers)
				.check(status.is(200))
				.check(regex("""o_fiooolat_login_button"""))
			)
		.pause(1)		
		.feed(csv("oo_user_credential.csv"))
		.exec(
			http("Login")
				.post("""/dmz/1:1:oolat_login:1:0:ofo_:fid/""")
				.headers(headers_post)
				.formParam("""dispatchuri""", """o_fiooolat_login_button""")
				.formParam("""dispatchevent""", """2""")
				.formParam("""o_fiooolat_login_name""", "${username}")
				.formParam("""o_fiooolat_login_pass""", "${password}")
				.check(status.is(200))
				.check(css(".o_logout", "href").saveAs("logoutlink"))
				//check my courses is loaded
				.check(css("""div.o_coursetable"""))
				.check(css("""li.o_site_repository a""","href").find(0).saveAs("href_mycourses"))
				.check(css("""li.o_site_groups a""","href").find(0).saveAs("href_mygroups"))
				.check(css("""div.o_meta h4.o_title a""","href").find(0).transform(href => FFEvent(href)).dontValidate.saveAs("ffevent_my_courses_0"))
			)
		.pause(5)
		.doIf(session => session.contains("ffevent_my_courses_0")) {
			exec(
				http("select:course0")
					.post(url => url("ffevent_my_courses_0").as[FFEvent].url)
					.headers(headers_post)
					.formParam("""dispatchuri""", session => session("ffevent_my_courses_0").as[FFEvent].elementId)
					.formParam("""dispatchevent""", session => session("ffevent_my_courses_0").as[FFEvent].actionId)
					.check(status.is(200))
					.check(css("""div.o_repo_details div.o_lead h1"""))
				)
		}
	
		.pause(5)
		.exec(
		    http("mycourses:1")
		    	.get("${href_mycourses}")
				.headers(headers_post)
				.check(status.is(200))
				.check(css("""div.o_coursetable"""))
				.check(css("""div.o_meta h4.o_title a""","href")
				    .find(1)
				    .transform(href => FFEvent(href))
				    .dontValidate.saveAs("ffevent_my_courses_1")
				)
			) 
		.doIf(session => session.contains("ffevent_my_courses_1")) {
			exec(
				http("select:course1")
					.post(url => url("ffevent_my_courses_1").as[FFEvent].url)
					.headers(headers_post)
					.formParam("""dispatchuri""", session => session("ffevent_my_courses_1").as[FFEvent].elementId)
					.formParam("""dispatchevent""", session => session("ffevent_my_courses_1").as[FFEvent].actionId)
					.check(status.is(200))
					.check(css("""div.o_repo_details div.o_lead h1"""))
				)
		}
		
		.pause(5)
		.exec(
		    http("mycourses:2")
		    	.get("${href_mycourses}")
				.headers(headers_post)
				.check(status.is(200))
				.check(css("""div.o_coursetable"""))
				.check(css("""div.o_meta h4.o_title a""","href")
				    .find(2)
				    .transform(href => FFEvent(href))
				    .dontValidate.saveAs("ffevent_my_courses_2")
				)
			) 
		.doIf(session => session.contains("ffevent_my_courses_2")) {
			exec(
				http("select:course2")
					.post(url => url("ffevent_my_courses_2").as[FFEvent].url)
					.headers(headers_post)
					.formParam("""dispatchuri""", session => session("ffevent_my_courses_2").as[FFEvent].elementId)
					.formParam("""dispatchevent""", session => session("ffevent_my_courses_2").as[FFEvent].actionId)
					.check(status.is(200))
					.check(css("""div.o_repo_details div.o_lead h1"""))
				)
		}
		
		.pause(5)
		.exec(
		    http("mygroups:0")
		    	.get("${href_mygroups}")
				.headers(headers_post)
				.check(status.is(200))
				.check(css("""div.o_rendertype_classic"""))
			) 

		.exec(
			http("Logout")
				.get("${logoutlink}")
				.headers(headers)
				.check(status.is(200))
				.check(regex("""o_fiooolat_login_button"""))
			)
	
}