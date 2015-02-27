/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.openolat.gatling.setup.builder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.openolat.gatling.setup.voes.UserVO;
import org.openolat.gatling.setup.RestConnection;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * <p>
 * Initial Date:  9 feb. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class UserUriBuilder {
	
	private final RestConnection connection;
	
	public UserUriBuilder(RestConnection connection) {
		this.connection = connection;
	}
	
	public List<UserVO> getUsers()
	throws URISyntaxException, IOException {
		URI uri = getUsersUri().build();
		HttpGet method = connection.createGet(uri, MediaType.APPLICATION_JSON);

		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200) {
			InputStream body = response.getEntity().getContent();
			List<UserVO> vos = connection.parseUserArray(body);
			return vos;
		} else {
			System.out.println("getUserByLogin: HTTP Error code: " + response.getStatusLine().getStatusCode());
			EntityUtils.consume(response.getEntity());
		}
		return null;
	}

	public UserVO getUserByLogin(String login)
	throws URISyntaxException, IOException {
		URI uri = getUsersUri().queryParam("login", login).build();
		HttpGet method = connection.createGet(uri, MediaType.APPLICATION_JSON);

		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200) {
			InputStream body = response.getEntity().getContent();
			List<UserVO> vos = connection.parseUserArray(body);
			if(vos.isEmpty()) {
				return null;
			} else if(vos.size() == 1) {
				return vos.get(0);
			}
			
			for(UserVO user:vos) {
				if(login.equals(user.getLogin())) {
					return user;
				}
			}
		} else {
			System.out.println("getUserByLogin: HTTP Error code: " + response.getStatusLine().getStatusCode());
			EntityUtils.consume(response.getEntity());
		}
		return null;
	}
	
	public UserVO createTestUser(String username)
	throws IOException, URISyntaxException{
		UserVO vo = new UserVO();
		vo.setLogin(username);
		vo.setFirstName("John_" + username);
		vo.setLastName("Smith_" + username);
		vo.setEmail(username + "@frentix.com");
		vo.putProperty("telOffice", "39847592");
		vo.putProperty("telPrivate", "39847592");
		vo.putProperty("telMobile", "39847592");
		vo.putProperty("gender", "Female");//male or female
		vo.putProperty("birthDay", "12/12/2009");

		URI usersUri = getUsersUri().build();
		HttpPut method = connection.createPut(usersUri, MediaType.APPLICATION_JSON);
		connection.addJsonEntity(method, vo);
		method.addHeader("Accept-Language", "en");
		
		HttpResponse response = connection.execute(method);
		int code = response.getStatusLine().getStatusCode();
		if(code == 200 || code == 201) {
			InputStream body = response.getEntity().getContent();
			UserVO savedVo = connection.parse(body, UserVO.class);
			return savedVo;
		}
		EntityUtils.consume(response.getEntity());
		return null;
	}
	
	public UserVO createUser(String username, String email, String firstName, String lastName, String password)
	throws IOException, URISyntaxException {
		UserVO vo = new UserVO();
		vo.setLogin(username);
		vo.setFirstName(firstName);
		vo.setLastName(lastName);
		vo.setPassword(password);
		vo.setEmail(email);
		vo.putProperty("telOffice", "39847592");
		vo.putProperty("telPrivate", "39847592");
		vo.putProperty("telMobile", "39847592");
		vo.putProperty("gender", "Female");//male or female
		vo.putProperty("birthDay", "12/12/2009");

		URI usersUri = getUsersUri().build();
		HttpPut method = connection.createPut(usersUri, MediaType.APPLICATION_JSON);
		connection.addJsonEntity(method, vo);
		
		HttpResponse response = connection.execute(method);
		int code = response.getStatusLine().getStatusCode();
		if(code == 200 || code == 201) {
			InputStream body = response.getEntity().getContent();
			return connection.parse(body, UserVO.class);
		}
		System.out.println("createUser return error code: " + code);
		EntityUtils.consume(response.getEntity());
		return null;
	}
	
	public boolean uploadPortrait(UserVO user, File portrait)
	throws FileNotFoundException, IOException, URISyntaxException {
		//upload portrait
		URI portraitUri = getPortraitUri(user).build();

		HttpPost method = connection.createPost(portraitUri, MediaType.APPLICATION_JSON);
		connection.addMultipart(method, "portrait.jpg", portrait);

		HttpResponse response = connection.execute(method);
		EntityUtils.consume(response.getEntity());
		int code = response.getStatusLine().getStatusCode();
		return code == 200 || code == 201;
	}
	
	/**
	 * @return The URI to courses of http://localhost:8080/olat/restapi/users
	 */
	public UriBuilder getUsersUri() throws URISyntaxException {
		return connection.getContextURI().path("users");
	}
	
	/**
	 * @return The URI to courses of http://localhost:8080/olat/restapi/users/{userKey}
	 */
	public UriBuilder getUserUri(UserVO user) throws URISyntaxException {
		return connection.getContextURI().path("users").path(user.getKey().toString());
	}
	
	/**
	 * @return The URI to courses of http://localhost:8080/olat/restapi/users/{userKey}/portrait
	 */
	public UriBuilder getPortraitUri(UserVO user) throws URISyntaxException {
		return connection.getContextURI().path("users").path(user.getKey().toString()).path("portrait");
	}
}