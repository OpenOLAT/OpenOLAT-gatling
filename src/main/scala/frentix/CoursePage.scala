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
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

import scala.collection.immutable
import io.gatling.http.check.HttpCheck

object CoursePage extends HttpHeaders {
	
  /**
	 * List of checks: find the next button and optionally save it as
	 * "nextElement" in session.
	 */
	val nextCourseElementCheckList: Seq[HttpCheck] = Seq(css("""a[onclick*='nextelement']""","onclick")
							.find
							.transform(onclick => XHREvent(onclick))
							.optional
						  .saveAs("nextElement"))
  
	def selectCourseAndBack(pause:Int) = {
		exec(selectCourse()).pause(pause).exec(myCourses()).pause(pause)
	}
	
	def selectCourseNavigateAndBack(pause:Int, pauseElement:Int) = {
		exec(selectCourse()).pause(pause)
		  .exec(asLongAs(session => session.contains("nextElement")) {
			   exec(nextCourseElement()).pause(pauseElement)
		  })
		  .exec(myCourses()).pause(pause)
	}
  
	def selectCourse() = {
		doIf(session => session.contains("currentCourse")) {
			exec(
				http("selectCourse:${n}")
					.post(session => session("currentCourse").as[FFEvent].url())
					.headers(headers_json)
					.formParam("""dispatchuri""", session => session("currentCourse").as[FFEvent].elementId)
					.formParam("""dispatchevent""", session => session("currentCourse").as[FFEvent].actionId)
					.formParam("""_csrf""", session => session("csrfToken").asOption[String].getOrElse(""))
					.transformResponse(extractJsonResponse)
					.check(status.is(200))
					// details page, course, share folders
					.check(css("""div.o_repo_details div.o_lead h1,div.o_course_run,div.o_briefcase"""))
					.check(nextCourseElementCheckList: _*)
				)
		}
	}
	
	def nextCourseElement() = {
		doIf(session => session.contains("nextElement")) {
			exec(session => {
				val nextElementUrl = session("nextElement").as[XHREvent].url()
				session.remove("nextElement").set("nextElementUrl", nextElementUrl)
			})
			.exec(
				http("nextCourseElement:selectCourse:${n}")
				  .post("""${nextElementUrl}""")
				  .formParam("cid","nextelement")
				  .formParam("""_csrf""", session => session("csrfToken").asOption[String].getOrElse(""))
					.headers(headers_json)
					.check(status.is(200))
					.transformResponse(extractJsonResponse)
					.check(nextCourseElementCheckList: _*)
			)
		}
	}

	/**
	 *
	 * @return
	 */
	def myCourses(): ChainBuilder = {
		doIf(session => session.contains("currentCourse")) {
			exec(session => {
				val myCoursesUrl = session("href_mycourses").as[XHREvent].url()
				session.set("myCoursesUrl", myCoursesUrl)
			})
			.exec(
				http("myCourses:${n}")
					.post("""${myCoursesUrl}""")
					.formParam("cid","t")
				  .formParam("""_csrf""", session => session("csrfToken").asOption[String].getOrElse(""))
					.headers(headers_json)
					.check(status.is(200))
					.transformResponse(extractJsonResponse)
					.check(css("""div.o_coursetable"""))
					.check(css("""div.o_meta h4.o_title a""","onclick")
						.findAll
						.transform(_.map(onclick => FFEvent(onclick)))
						.optional
						.saveAs("currentCourses"))
					.check(css("""li.o_site_repository a""","onclick")
						.find(0)
						.transform(onclick => XHREvent(onclick))
						.saveAs("href_mycourses")
				)
			)
			.exec(session => {
				val courseList = session("currentCourses").as[immutable.Vector[String]]
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