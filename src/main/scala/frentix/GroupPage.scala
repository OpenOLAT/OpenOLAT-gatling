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

import frentix.event.{FFXHREvent, XHREvent}
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder

import scala.collection.immutable

object GroupPage extends HttpHeaders {
	
	def myGroupsAndSelectCourse(pause:Int) = {
		exec(myGroups()).pause(pause).exec(selectGroup()).pause(pause)
	}
  
	def myGroups(): ChainBuilder = {
		doIf(session => session.contains("href_mygroups")) {
  		exec(session => {
  			val myGroupsUrl = session("href_mygroups").as[XHREvent].url()
  			session.set("myGroupsUrl", myGroupsUrl)
  		})
  		.exec(
  		  http("mygroups:${n}")
  			  .post("""${myGroupsUrl}""")
  			  .headers(headers_json)
  			  .formParam("cid","t")
  			  .transformResponse(extractJsonResponse)
  			  .check(status.is(200))
  			  .check(css("""div.o_group_list"""))
  			  .check(css("""div.o_group_list td.o_dnd_label a""","href")
						.findAll
						.transform(_.map(href => FFXHREvent(href)))
						.optional
						.saveAs("currentGroups"))
					.check(css("""div#o_main_center_content_inner form""","action")
            .find
            .saveAs("myGroupsAction"))
					.check(css("""li.o_site_groups a""","onclick")
						.find(0)
						.transform(onclick => XHREvent(onclick))
						.saveAs("href_mygroups")))
		}
	}
	
	def selectGroup(): ChainBuilder = {
		exec(session => {
		  val groupList = session("currentGroups").as[immutable.Vector[String]]
			val nextPos = session("n").as[Int]
			if(nextPos < groupList.length) {
				session.set("currentGroup", groupList(nextPos))
			} else {
				session.remove("currentGroup")
			}
		})
		.doIf(session => session.contains("currentGroup")) {
			exec(session => {
				val currentGroupLink = session("currentGroup").as[FFXHREvent]
				val csrfToken = session("csrfToken").as[String]
				val parameters = currentGroupLink.formMap(csrfToken);
				session.set("formParameters", parameters)
			})
			.exec(
				http("selectGroup:${n}")
					.post("""${myGroupsAction}""")
          .formParamMap("""${formParameters}""")
					.headers(headers_json)
					.transformResponse(extractJsonResponse)
					.check(status.is(200))
					.check(css("""h2 i.o_icon.o_icon_group"""))
				)
		}
			
			
	}

}