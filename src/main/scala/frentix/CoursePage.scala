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
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

import scala.collection.mutable

object CoursePage extends HttpHeaders {
  
	def selectCourseAndBack(pause:Int) = {
		exec(selectCourse()).pause(pause).exec(myCourses()).pause(pause)
	}
  
	def selectCourse() = {
		doIf(session => session.contains("currentCourse")) {
			exec(
				http("selectCourse:${n}")
					.post(session => session("currentCourse").as[FFEvent].url())
					.headers(headers_post)
					.formParam("""dispatchuri""", session => session("currentCourse").as[FFEvent].elementId)
					.formParam("""dispatchevent""", session => session("currentCourse").as[FFEvent].actionId)
					.check(status.is(200))
					.check(css("""div.o_repo_details div.o_lead h1,div.o_course_run"""))
				)
		}
	}

	/**
	 *
	 * @return
	 */
	def myCourses(): ChainBuilder = {
		doIf(session => session.contains("currentCourse")) {
			exec(
				http("myCourses:${n}")
					.get("${href_mycourses}")
					.headers(headers_post)
					.check(status.is(200))
					.check(css("""div.o_coursetable"""))
					.check(css("""div.o_meta h4.o_title a""","href")
						.findAll
						.transform(_.map(href => FFEvent(href)))
						.optional
						.saveAs("currentCourses")
				)
			)
			.exec(session => {
				val courseList = session("currentCourses").as[mutable.Buffer[String]]
				val nextPos = session("n").as[Int]
				if(nextPos < courseList.length) {
					session.set("currentCourse", courseList(nextPos))
				} else {
					session.remove("currentCourse")
				}
			})
		}
	}
}