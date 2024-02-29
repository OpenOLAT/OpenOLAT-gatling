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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.gatling.http.response.Response;
import io.gatling.http.response.ResponseBody;
import io.gatling.http.response.StringResponseBody;
import io.gatling.javaapi.core.Session;

/**
 * Abstract class for pages.
 * 
 * 
 * Initial date: 29 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class HttpHeaders {

	private static final Logger log  = LoggerFactory.getLogger(HttpHeaders.class);
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public static final Map<String,String> headers = Map.of("Accept", "*/*");

	public static final Map<String,String> headersJson = Map.of("Accept", "application/json,text/html");

	public static final Map<String,String> headersPost = Map.of("Content-Type", "application/x-www-form-urlencoded", "Origin" , "http://localhost");
	
	public static final BiFunction<Response, Session, Response> extractJsonResponse = (response, session) -> {
		String redirect = null;
		StringBuilder extractedResponse = new StringBuilder(32000);
		extractedResponse.append("<!DOCTYPE html><html><head><title>Fragment</title></head><body>");
		try(InputStream bodyStream = response.body().stream()) {
			JsonNode jsonResponse = objectMapper.readTree(bodyStream);
			JsonNode cmds = jsonResponse.get("cmds");
			if(cmds instanceof ArrayNode cmdsArr) {
				int numOfCmds = cmdsArr.size();
				for(int i=0; i<numOfCmds; i++) {
					JsonNode cmd = cmds.get(i);
					int cmdCode = cmd.get("cmd").asInt();
					if(cmdCode == 2) {
						JsonNode cps = cmd.get("cda").get("cps");
						if(cps instanceof ArrayNode cpsArr) {
							for(int j=0; j<cpsArr.size(); j++) {
								JsonNode cp = cps.get(j);
								if (cp != null && cp.has("hfrag")) {
									String htmlFragment = cp.get("hfrag").asText();
									if (!htmlFragment.equals("<!-- empty -->")) {
										extractedResponse.append(htmlFragment);
									}
								}
							}
						}
					} else if(cmdCode == 3) {
						redirect = cmd.get("cda").get("rurl").asText();
					}
				}
			}
		} catch (IOException e) {
			log.error("", e);
		}
		
		extractedResponse.append("</body></html>");
		//System.out.println("***********************************************************")
		//System.out.println("Cmds: " + cmds.size())
		//System.out.println(extractedResponse) new StringResponseBody(extractedResponse.toString(), UTF_8)
		ResponseBody htmlBody = new StringResponseBody(extractedResponse.toString(), StandardCharsets.UTF_8);
		Response htmlResponse = response.copy(response.request(),
				response.startTimestamp(), response.endTimestamp(), response.status(),
				response.headers(), htmlBody, response.checksums(), false);
		if(redirect != null) {
			log.warn("Redirect: {}", redirect);
		}
		return htmlResponse;
	};
	
	public static final String csrfToken(Session session) {
		String token = session.getString ("csrfToken");
		return token == null ? "" : token;
	}
}
