/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.gatling.page;


import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import org.olat.gatling.event.FFEvent;
import org.olat.gatling.event.XHREvent;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

/**
 * 
 * Initial date: 29 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LoginPage extends HttpHeaders {
	
	public static ChainBuilder loginScreen = http("Login Screen")
				.get("/dmz/")
				.headers(headers)
				.check(status().is(200))
				.check(regex("o_fiooolat_login_button"))
				.toChainBuilder();
	
	public static HttpRequestActionBuilder restUrl(String url) {
		return http("Jump with REST url")
				.get(url)
				.headers(headers)
				.check(status().is(200));
	}

	public static HttpRequestActionBuilder loginUsername = http("Login username")
			.post("/dmz/1:1:oolat_login:1:0:ofo_:fid/")
			.headers(headersPost)
			.formParam("dispatchuri", "o_fiooolat_login_button")
			.formParam("dispatchevent", "2")
			.formParam("o_fiooolat_login_name", "#{username}")
			.check(status().is(200));
	
	public static HttpRequestActionBuilder loginPasswordOptional = http("Login password")
			.post("/dmz/1:1:oolat_login:1:0:ofo_:fid/")
			.headers(headersPost)
			.formParam("dispatchuri", "o_fiooolat_login_button")
			.formParam("dispatchevent", "2")
			.formParam("o_fiooolat_login_pass", "#{password}")
			.formParam("_csrf", HttpHeaders::csrfToken)
			.check(css(".o_logout", "href")
				.saveAs("logoutlink"))
			//check my courses is loaded
			
			.check(css("form input[name=_csrf]","value")
				.find(0)
				.optional()
				.saveAs("csrfToken"))
			.check(css("li.o_site_repository a","onclick")
				.find(0)
				.transform(onclick -> XHREvent.valueOf(onclick, "site repository")) //
				.saveAs("href_mycourses"))
			.check(css("li.o_site_groups a","onclick")
				.find(0)
				.transform(onclick -> XHREvent.valueOf(onclick, "site groups"))
				.saveAs("href_mygroups"))
			.check(css("div.o_meta h4.o_title a","onclick")
				.find(0)
				.transform(FFEvent::valueOf)
				.optional()
				.saveAs("currentCourse"))
			.check(css("div.o_meta h4.o_title a span")
				.find(0)
				.optional()
				.saveAs("currentCourseTitle"));

	public static HttpRequestActionBuilder loginPasswordToMyCourses = loginPasswordOptional
			.check(css("div.o_coursetable"));
	
	public static final HttpRequestActionBuilder logout = http("Logout")
			.get("#{logoutlink}")
			.headers(headers)
			.check(status().is(200))
			.check(regex("o_fiooolat_login_button"));
}
