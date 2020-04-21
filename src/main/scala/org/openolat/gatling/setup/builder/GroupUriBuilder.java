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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;

import org.openolat.gatling.setup.RestConnection;
import org.openolat.gatling.setup.voes.GroupConfigurationVO;
import org.openolat.gatling.setup.voes.GroupVO;
import org.openolat.gatling.setup.voes.UserVO;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;


public class GroupUriBuilder {
	
	private final RestConnection connection;
	
	public GroupUriBuilder(RestConnection connection) {
		this.connection = connection;
	}
	
	public List<GroupVO> getGroups() throws IOException, URISyntaxException {
		URI groupsUri = getGroupsUri().build();
		HttpGet method = connection.createGet(groupsUri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200) {
			InputStream body = response.getEntity().getContent();
			return connection.parseGroupArray(body);
		}
		EntityUtils.consume(response.getEntity());
		System.out.println("Cannot get groups: " + response.getStatusLine().getStatusCode());
		return Collections.emptyList();
	}
	
	public GroupVO createGroup(String name, String description) throws IOException, URISyntaxException {
		GroupVO vo = new GroupVO();
		vo.setName(name);
		vo.setDescription(description);
		
		URI uri = getGroupsUri().build();

		HttpPut method = connection.createPut(uri, MediaType.APPLICATION_JSON);
		connection.addJsonEntity(method, vo);
		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
			InputStream body = response.getEntity().getContent();
			return connection.parse(body, GroupVO.class);
		}
		EntityUtils.consume(response.getEntity());
		System.out.println("Cannot get group: " + response.getStatusLine().getStatusCode());
		return null;
	}
	
	public void setConfiguration(Long groupKey, boolean owners, boolean participants)
	throws IOException, URISyntaxException {
		GroupConfigurationVO config = new GroupConfigurationVO();
		config.setOwnersPublic(false);
		config.setOwnersVisible(owners);
		config.setParticipantsPublic(false);
		config.setParticipantsVisible(participants);
		config.setWaitingListPublic(false);
		config.setWaitingListVisible(false);
	
		URI uri = getConfigurationUri(groupKey).build();
		HttpPost method = connection.createPost(uri, MediaType.APPLICATION_JSON);
		connection.addJsonEntity(method, config);
		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
			//System.out.println("Config OK");
		} else {
			System.out.println("Config NOK");
		}
		EntityUtils.consume(response.getEntity());
	}
	
	public List<UserVO> getParticipants(GroupVO group) throws IOException, URISyntaxException {
		URI participantsUri = getParticipantsUri(group.getKey()).build();
		HttpGet method = connection.createGet(participantsUri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200) {
			return connection.parseUserArray(response);
		}
		EntityUtils.consume(response.getEntity());
		return Collections.emptyList();
	}
	
	public List<UserVO> getOwners(GroupVO group) throws IOException, URISyntaxException {
		URI groupsUri = getOwnersUri(group.getKey()).build();
		HttpGet method = connection.createGet(groupsUri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200) {
			return connection.parseUserArray(response);
		}
		EntityUtils.consume(response.getEntity());
		return Collections.emptyList();
	}
	
	public boolean addParticipant(GroupVO group, UserVO user) throws IOException, URISyntaxException {
		UriBuilder participantUriBuilder = getParticipantsUri(group.getKey());
		URI participantUri = participantUriBuilder.path(user.getKey().toString()).build();
		HttpPut method = connection.createPut(participantUri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);

		int code = response.getStatusLine().getStatusCode();
		EntityUtils.consume(response.getEntity());
		return (code == 200 || code == 201);
	}
	
	public boolean addOwner(GroupVO group, UserVO user) throws IOException, URISyntaxException {
		UriBuilder ownerUriBuilder = getOwnersUri(group.getKey());
		URI ownerUri = ownerUriBuilder.path(user.getKey().toString()).build();
		HttpPut method = connection.createPut(ownerUri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);

		int code = response.getStatusLine().getStatusCode();
		EntityUtils.consume(response.getEntity());
		return (code == 200 || code == 201);
	}
	
	public boolean removeOwner(GroupVO group, UserVO user) throws IOException, URISyntaxException {
		UriBuilder ownerUriBuilder = getOwnersUri(group.getKey());
		URI ownerUri = ownerUriBuilder.path(user.getKey().toString()).build();
		HttpDelete method = connection.createDelete(ownerUri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);

		int code = response.getStatusLine().getStatusCode();
		EntityUtils.consume(response.getEntity());
		return (code == 200 || code == 201);
	}
	
	public UriBuilder getGroupsUri() throws URISyntaxException {
		return connection.getContextURI().path("groups");
	}

	public UriBuilder getParticipantsUri(Long groupKey) throws URISyntaxException {
		return connection.getContextURI().path("groups").path(groupKey.toString()).path("participants");
	}
	
	public UriBuilder getOwnersUri(Long groupKey) throws URISyntaxException {
		return connection.getContextURI().path("groups").path(groupKey.toString()).path("owners");
	}
	
	public UriBuilder getConfigurationUri(Long groupKey) throws URISyntaxException {
		return connection.getContextURI().path("groups").path(groupKey.toString()).path("configuration");
	}
}
