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
package org.olat.gatling.setup.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.List;

import org.olat.gatling.setup.model.ErrorVO;
import org.olat.gatling.setup.model.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class UsersWSClient {
	
	// status = 1..99    User with this status are visible (e.g. user search)
	//          100..199 User with this status are invisible (e.g. user search)
	/** Identity has a permanent olat user account and will be never listen in user-deletion process. */
	public static final Integer STATUS_PERMANENT = 1;
	/** Identity has access to olat-system. */ 
	public static final Integer STATUS_ACTIV = 2;
	/** Limit for visible identities, all identities with status < LIMIT will be listed in search etc. */
	public static final Integer STATUS_VISIBLE_LIMIT = 100;
	/** Identity can not login and will not be listed (only on login-denied list). */
	public static final Integer STATUS_LOGIN_DENIED = 101;
	/** Identity can not login and will not be listed (only on pending list). */
	public static final Integer STATUS_PENDING = 102;
	/** Identity can not BE SEARCHED. */
	public static final Integer STATUS_INACTIVE = 103;
	/** Identity is deleted and has no access to olat-system and is not visible (except administrators). */
	public static final Integer STATUS_DELETED   = 199;
	
	private final Logger log = LoggerFactory.getLogger(UsersWSClient.class);
	
	private final RestConnection conn;
	
	public UsersWSClient(RestConnection conn) {
		this.conn = conn;
	}
	
	public UserVO createUser(UserVO userVo, List<ErrorVO> errors)
	throws IOException, URISyntaxException, InterruptedException {
		HttpResponse<InputStream> response = conn.put("users", userVo);
		int statusCode = response.statusCode();
		if(statusCode >= 200 && statusCode < 400) {
			return conn.parse(response, UserVO.class);
		} else if(statusCode == 406) {
			errors.addAll(conn.parseList(response, ErrorVO.class));
			return null;
		}
		String errorMsg = ConnectionUtilities.toString(response);
		log.error("createUser doesn't return correctly: {}", response.statusCode());
		log.error("Message: {}", errorMsg);
		return null;
	}
	
	public List<UserVO> searchAllUsers()
	throws IOException, URISyntaxException, InterruptedException {
		NameValuePair searchAttr = new NameValuePair("statusVisibleLimit", "all");
		HttpResponse<InputStream> response = conn.getJson("users", searchAttr);
		if(response.statusCode() == 200) {
			return conn.parseList(response, UserVO.class);
		}
		log.error("searchAllUsers doesn't return correctly: {}", response.statusCode());
		ConnectionUtilities.consume(response);
		return null;
	}
}