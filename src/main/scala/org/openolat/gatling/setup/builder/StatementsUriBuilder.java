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
package org.openolat.gatling.setup.builder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.openolat.gatling.setup.RestConnection;
import org.openolat.gatling.setup.voes.EfficiencyStatementVO;

/**
 * 
 * Initial date: 01.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StatementsUriBuilder {
	
	private final RestConnection connection;
	
	public StatementsUriBuilder(RestConnection connection) {
		this.connection = connection;
	}

	public boolean hasEfficiencyStatement(Long resourceKey, Long identityKey) throws IOException, URISyntaxException {
		URI uri = getEfficiencyStatementUri(resourceKey).path(identityKey.toString()).build();

		HttpGet method = connection.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		int statusCode = response.getStatusLine().getStatusCode();
		boolean found = false;
		if(statusCode == 200) {
			found = true;
		} else if(statusCode == 404) {
			found = false;
		} else {
			System.out.println("Cannot get efficiency statement: " + response.getStatusLine().getStatusCode());
		}
		EntityUtils.consume(response.getEntity());
		return found;

	}
	
	public boolean create(String courseTitle, Long resourceKey, Long identityKey, Boolean passed, Float score)
	throws IOException, URISyntaxException {
		EfficiencyStatementVO vo = new EfficiencyStatementVO();
		vo.setCourseTitle(courseTitle);
		vo.setCreationDate(new Date());
		vo.setPassed(passed);
		vo.setScore(score);
		
		URI uri = getEfficiencyStatementUri(resourceKey).path(identityKey.toString()).build();

		HttpPut method = connection.createPut(uri, MediaType.APPLICATION_JSON);
		connection.addJsonEntity(method, vo);
		HttpResponse response = connection.execute(method);
		int statusCode = response.getStatusLine().getStatusCode();
		boolean ok = false;
		if(statusCode == 200 || statusCode == 201) {
			ok = true;
		} else if(statusCode == 409) {
			System.out.println("Efficiency statement already created"); 
		} else {
			System.out.println("Cannot create efficiency statement: " + response.getStatusLine().getStatusCode());
		}
		EntityUtils.consume(response.getEntity());
		return ok;
	}
	
	/**
	 * repo/courses/{resourceKey}/statements
	 * @param resourceKey
	 * @return
	 */
	public UriBuilder getEfficiencyStatementUri(Long resourceKey) throws URISyntaxException {
		return connection.getContextURI().path("repo").path("courses").path(resourceKey.toString()).path("statements");
	}
}
