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

import java.nio.charset.StandardCharsets._

import com.fasterxml.jackson.databind.ObjectMapper
import io.gatling.core.session._
import io.gatling.http._
import io.gatling.http.response._
import io.gatling.commons.validation._

import scala.collection.mutable

trait HttpHeaders {

	val headers: Map[String, String] = Map("""Accept""" -> """*/*""")

	val headers_json: Map[String, String] = Map("""Accept""" -> """application/json,text/html""")

	val headers_post: Map[String, String] = Map(
		"""Content-Type""" -> """application/x-www-form-urlencoded""",
		"""Origin""" -> """http://localhost""")
		
	def extractJsonResponse: ResponseTransformer = { (response: Response, session: Session) => {

	  val extractedResponse = new mutable.StringBuilder(32000)
		extractedResponse.append("<!DOCTYPE html><html><head><title>Fragment</title></head><body>")
		val bodyStream = response.body.stream
		val jsonResponse = new ObjectMapper().readTree(bodyStream)
		bodyStream.close()
		val cmds = jsonResponse.get("cmds")
		var redirect:String = null
		0 until cmds.size() foreach { i => {
			val cmd = cmds.get(i)
			val cmdCode = cmd.get("cmd").asInt()
			if(cmdCode == 2) {
				val cps = cmd.get("cda").get("cps")
				0 until cps.size() foreach { j => {
					val cp = cps.get(j)
					if (cp != null && cp.has("hfrag")) {
						val htmlFragment = cp.get("hfrag").asText()
						if (!htmlFragment.equals("<!-- empty -->")) {
							extractedResponse.append(htmlFragment)
						}
					}
				}}
			} else if (cmdCode == 3) {
				redirect = cmd.get("cda").get("rurl").asText()
			}
		}}
		extractedResponse.append("</body></html>")
		//println("***********************************************************")
		//println("Cmds: " + cmds.size())
		//println(extractedResponse)
		val htmlResponse = response.copy(body = new StringResponseBody(extractedResponse.toString(), UTF_8))
		Success(htmlResponse)
	}}

}