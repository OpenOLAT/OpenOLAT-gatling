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

object CoursePage extends HttpHeaders {
  
	def selectCourseAndBack(pos:Int, pause:Int) = {
		exec(selectCourse(pos)).pause(pause).exec(myCourses(pos)).pause(pause)
	}
  
	def selectCourse(pos:Int) = {
		doIf(session => session.contains("ffevent_my_courses_" + pos)) {
			exec(
				http("selectCourse:" + pos)
					.post(url => url("ffevent_my_courses_" + pos).as[FFEvent].url)
					.headers(headers_post)
					.formParam("""dispatchuri""", session => session("ffevent_my_courses_" + pos).as[FFEvent].elementId)
					.formParam("""dispatchevent""", session => session("ffevent_my_courses_" + pos).as[FFEvent].actionId)
					.check(status.is(200))
					.check(css("""div.o_repo_details div.o_lead h1,div.o_course_run"""))
				)
		}
	}
	
	def myCourses(pos:Int) = {
		val nextPos = pos + 1
		doIf(session => session.contains("ffevent_my_courses_" + pos)) {
			exec(
				http("myCourses:" + nextPos)
					.get("${href_mycourses}")
					.headers(headers_post)
					.check(status.is(200))
					.check(css("""div.o_coursetable"""))
					.check(css("""div.o_meta h4.o_title a""","href")
						.find(pos + 1)
						.transform(href => FFEvent(href))
						.optional
						.saveAs("ffevent_my_courses_" + nextPos)
				)
			)
		}
	}
}