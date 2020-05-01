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

import frentix.event.{XHREvent, FFEvent}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

/**
 * Some functions to be exec
 */
object LoginPage extends HttpHeaders {

	/**
	 *
	 * @return The request builder
	 */
  def loginScreen: HttpRequestBuilder = http("Login Screen")
		.get("""/dmz/""")
		.headers(headers)
		.check(status.is(200))
		.check(regex("""o_fiooolat_login_button"""))

	/**
	 * Do the login and optionally save the first course
	 * in the "My courses" list.
	 *
	 * @return The request builder
	 */
	def loginToMyCourses: HttpRequestBuilder = http("Login to my courses")
		.post("""/dmz/1:1:oolat_login:1:0:ofo_:fid/""")
		.headers(headers_post)
		.formParam("""dispatchuri""", """o_fiooolat_login_button""")
		.formParam("""dispatchevent""", """2""")
		.formParam("""o_fiooolat_login_name""", "${username}")
		.formParam("""o_fiooolat_login_pass""", "${password}")
		.check(status.is(200))
		.check(css(".o_logout", "href")
		  .saveAs("logoutlink"))
		//check my courses is loaded
		.check(css("""div.o_coursetable"""))
		.check(css("""form input[name=_csrf]""","value")
			.find(0)
			.optional
			.saveAs("csrfToken"))
		.check(css("""li.o_site_repository a""","onclick")
			.find(0)
			.transform(onclick => XHREvent(onclick))
			.saveAs("href_mycourses"))
		.check(css("""li.o_site_groups a""","onclick")
			.find(0)
			.transform(onclick => XHREvent(onclick))
			.saveAs("href_mygroups"))
		.check(css("""div.o_meta h4.o_title a""","onclick")
			.find(0)
			.transform(onclick => FFEvent(onclick))
			.optional
			.saveAs("currentCourse"))
		.check(css("""div.o_meta h4.o_title a span""")
			.find(0)
			.optional
			.saveAs("currentCourseTitle"))

	/**
	 * Jump to in OpenOLAT with a REST url ( a business path).
	 * There isn't any check to the landing point.
	 *
	 * @param url The URL where to jump
	 * @return The request builder
	 */
	def restUrl(url:String): HttpRequestBuilder = http("Jump with REST url")
		.get(url)
		.headers(headers)
		.check(status.is(200))

	/**
	 * Log out
	 * @return
	 */
	def logout: HttpRequestBuilder = http("Logout")
		.get("${logoutlink}")
		.headers(headers)
		.check(status.is(200))
		.check(regex("""o_fiooolat_login_button"""))

}