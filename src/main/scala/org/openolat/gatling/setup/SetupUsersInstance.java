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
package org.openolat.gatling.setup;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.openolat.gatling.setup.voes.UserVO;

/**
 * Setup an instance with some users
 *
 * Created by srosse on 20.06.2020
 */
public class SetupUsersInstance {

	private static final String url = "https://exam.uni-kiel.de";

	public static final File avatars = new File("/Users/srosse/Pictures/Avatars");

	public static void main(String[] args) {
		try {
			//build the connection pool (for MySQL setup, use only 1 connection)
			List<RestConnection> connections = new ArrayList<>(4);
			connections.add(new RestConnection(new URL(url), "administrator", ""));
			for(RestConnection connection:connections) {
				connection.login();
			}

			RestConnectionPool pool = new RestConnectionPool(connections);
			//start
			new SetupUsersInstance().setup(pool);
			System.out.println("The End");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int setup(RestConnectionPool pool)
			throws IOException, URISyntaxException, InterruptedException {

		// create or load users
		ConcurrentMap<String,UserVO> users = new SetupUsers(pool)
				.createUsers("zac", 10000);
				//.loadExistingUsers();

		return users.size();
	}
}