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
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.openolat.gatling.setup.RestConnection;
import org.openolat.gatling.setup.voes.OrganisationVO;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OrganisationUriBuilder {
	
	private final RestConnection connection;
	
	public OrganisationUriBuilder(RestConnection connection) {
		this.connection = connection;
	}
	
	public List<OrganisationVO> getOrganisations() throws IOException, URISyntaxException {
		URI organisationsUri = getOrganisationsUri().build();
		HttpGet method = connection.createGet(organisationsUri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200) {
			return connection.parseOrganisationArray(response.getEntity());
		}
		EntityUtils.consume(response.getEntity());
		System.out.println("Cannot get organisations: " + response.getStatusLine().getStatusCode());
		return Collections.emptyList();
	}
	
	public UriBuilder getOrganisationsUri() throws URISyntaxException {
		return connection.getContextURI().path("organisations");
	}
}
