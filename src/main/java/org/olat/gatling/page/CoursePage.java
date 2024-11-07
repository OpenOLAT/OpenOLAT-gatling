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

import java.util.List;

import org.olat.gatling.event.FFEvent;
import org.olat.gatling.event.XHREvent;

import io.gatling.javaapi.core.*;

/**
 * 
 * Initial date: 29 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoursePage extends HttpHeaders {
	
	public static final CheckBuilder nextCourseElementCheckList = css("a[onclick*='nextelement']","onclick")
			.find()
			.transform(onclick -> XHREvent.valueOf(onclick, "Next element"))
			.optional()
		  .saveAs("nextElement");
	
	public static final ChainBuilder selectCourseAndBack(int pause) {
		return exec(selectCourse())
			.pause(pause)
			.exec(myCourses())
			.pause(pause);
	}
	
	public static final ChainBuilder selectCourseNavigateAndBack(int pause, int pauseElement) {
		return exec(selectCourse())
			.pause(pause)
			.exec(asLongAs(session -> session.contains("nextElement"))
				.on(
						exec(nextCourseElement())
						.pause(pauseElement)
				)
			)
			.exec(myCourses())
			.pause(pause);
	}
	
	public static final ChainBuilder selectCourse() {
		return doIf(session -> session.contains("currentCourse")).then(
				exec(
					http("selectCourse:#{n}")
						.post(session -> ((FFEvent)session.get("currentCourse")).url())
						.headers(headersJson)
						.formParam("dispatchuri", session -> ((FFEvent)session.get("currentCourse")).element())
						.formParam("dispatchevent", session -> ((FFEvent)session.get("currentCourse")).action())
						.formParam("_csrf", HttpHeaders::csrfToken)
						.transformResponse(extractJsonResponse)
						.check(status().is(200))
						// details page, course, share folders
						.check(css("div.o_repo_details div.o_lead h2,div.o_course_run,div.o_folder"))
						.check(nextCourseElementCheckList)
					)
				);
	}
	
	public static final ChainBuilder nextCourseElement() {
		return doIf(session -> session.contains("nextElement")).then(
				exec(session -> {
					String nextElementUrl = ((XHREvent)session.get("nextElement")).url();
					return session.remove("nextElement").set("nextElementUrl", nextElementUrl);
				})
				.exec(
					http("nextCourseElement:selectCourse:#{n}")
					  .post("#{nextElementUrl}")
					  .formParam("cid","nextelement")
					  .formParam("_csrf", HttpHeaders::csrfToken)
						.headers(headersJson)
						.check(status().is(200))
						.transformResponse(extractJsonResponse)
						.check(nextCourseElementCheckList)
				)
			);
		}

	public static final ChainBuilder myCourses() {
		return doIf(session -> session.contains("currentCourse")).then(
				exec(session -> {
					String myCoursesUrl = ((XHREvent)session.get("href_mycourses")).url();
					return session.set("myCoursesUrl", myCoursesUrl);
				})
				.exec(
					http("myCourses:#{n}")
						.post("#{myCoursesUrl}")
						.formParam("cid","t")
					  .formParam("_csrf", HttpHeaders::csrfToken)
						.headers(headersJson)
						.check(status().is(200))
						.transformResponse(extractJsonResponse)
						.check(css("div.o_coursetable"))
						.check(css("div.o_meta h4.o_title a","onclick")
							.findAll()
							.transform(list -> {
								return list.stream().map(FFEvent::valueOf).toList();
							})
							.optional()
							.saveAs("currentCourses"))
						.check(css("li.o_site_repository a","onclick")
							.find(0)
							.transform(onclick -> XHREvent.valueOf(onclick, "Site repository in course"))
							.saveAs("href_mycourses")
					)
				)
				.exec(session -> {
					List<String> courseList = session.get("currentCourses");
					int nextPos = session.getInt("n");
					if(nextPos < courseList.size()) {
						return session.set("currentCourse", courseList.get(nextPos));
					}
					return session.remove("currentCourse");
				})
			);
		}
}
