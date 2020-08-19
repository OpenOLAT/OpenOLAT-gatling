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

import org.openolat.gatling.setup.builder.UserUriBuilder;
import org.openolat.gatling.setup.voes.UserVO;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * Created by srosse on 20.02.15.
 */
public class SetupUsers {

	private final RestConnectionPool pool;

	public SetupUsers(RestConnectionPool pool) {
		this.pool = pool;
	}

	public ConcurrentMap<String,UserVO> createUsers(String prefix, int numOfUsers)
			throws IOException, URISyntaxException, InterruptedException {

		ConcurrentMap<String,UserVO> existingUsers = loadExistingUsers();
		CreateUser createUser = new CreateUser(pool, existingUsers);

		//my users
		//myDefaultUsers().parallelStream().forEach(createUser);
		//some random users
		createRandomUsers(prefix, numOfUsers).parallelStream().forEach(createUser);

		return existingUsers;
	}

	public ConcurrentMap<String,UserVO> loadExistingUsers()
			throws IOException, URISyntaxException, InterruptedException {
		ConcurrentMap<String,UserVO> existingUsers = new ConcurrentHashMap<>();
		
		RestConnection connection = pool.borrow();
		try {
			UserUriBuilder userBuilder = new UserUriBuilder(connection);
			List<UserVO> currentUsers = userBuilder.getUsers();
			for(UserVO currentUser:currentUsers) {
				if(currentUser.getExternalId() != null) {
					existingUsers.put(currentUser.getExternalId(), currentUser);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.giveBack(connection);
		}
		return existingUsers;
	}

	public List<UserDef> myDefaultUsers() {
		List<UserDef> defaultUsers = new ArrayList<>();
		defaultUsers.add(new UserDef("aoi", "aoi@cyberiacafe.ch", "Aoi", "Volks", "aoi001"));
		defaultUsers.add(new UserDef("yukino", "yukino@cyberiacafe.ch", "Yukino", "Volks", "yukino1"));
		defaultUsers.add(new UserDef("kanu", "kanu@cyberiacafe.ch", "Kanu", "Unchou", "kanu01"));
		defaultUsers.add(new UserDef("ryomou", "ryomou@cyberiacafe.ch", "Ryomou", "Shimei", "ryomou1"));
		defaultUsers.add(new UserDef("rei", "rei@cyberiacafe.ch", "Rei", "Ayanami", "rei001"));
		defaultUsers.add(new UserDef("asuka", "asuka@cyberiacafe.ch", "Asuka", "Langley Soryu", "asuka1"));
		defaultUsers.add(new UserDef("deunan", "deunan@cyberiacafe.ch", "Deunan", "Knute", "deunan1"));
		defaultUsers.add(new UserDef("briareos", "briareos@cyberiacafe.ch", "Briareos", "Hecatonecles", "briareos1"));
		return defaultUsers;
	}

	public List<UserDef> createRandomUsers(String prefix, int numOfUsers) {
		List<String> firstNames = new ArrayList<>(5000);
		List<String> lastNames = new ArrayList<>(5000);
		try (InputStream in = SetupInstance.class.getResourceAsStream("/data/list_of_names.txt");
			 Reader reader = new InputStreamReader(in);
			 BufferedReader bReader = new BufferedReader(reader)) {

			String line;
			while ((line = bReader.readLine()) != null) {
				int lastIndex = line.lastIndexOf(' ');
				if(lastIndex > 0) {
					String firstName = line.substring(0, lastIndex);
					String lastName = line.substring(lastIndex);
					firstNames.add(firstName.trim());
					lastNames.add(lastName.trim());
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		Random rnd = new Random();
		List<UserDef> users = new ArrayList<>(numOfUsers);
		for(int i=1; i<=numOfUsers; i++) {
			String firstName = getRandomName(firstNames, rnd);
			String lastName = getRandomName(lastNames, rnd);
			users.add(new UserDef(prefix + "_" + i, prefix + "_" + i + "@mt2014.com", firstName, lastName, "openolat"));
		}
		return users;
	}

	public String getRandomName(List<String> names, Random rnd) {
		double randomPosition = rnd.nextDouble() * names.size();
		long first = Math.min(Math.round(randomPosition), names.size() - 1);
		return names.get((int)first);
	}

	public static class CreateUser implements Consumer<UserDef> {

		private final RestConnectionPool pool;
		private final ConcurrentMap<String,UserVO> existingUserNames;

		public CreateUser(RestConnectionPool pool, ConcurrentMap<String,UserVO> existingUserNames) {
			this.pool = pool;
			this.existingUserNames = existingUserNames;
		}

		@Override
		public void accept(UserDef def) {
			RestConnection connection = null;
			try {
				connection = pool.borrow();
				UserUriBuilder userBuilder = new UserUriBuilder(connection);

				UserVO user;
				if(existingUserNames.containsKey(def.getExternalId())) {
					user = existingUserNames.get(def.getExternalId());
				} else {
					user = userBuilder.getUserByExternalId(def.getExternalId());
					if(user != null) {
						existingUserNames.put(user.getLogin(), user);
					}
				}
				if(user != null) {
					System.out.println("User already exists: " + def.getExternalId());
				} else {
					user = userBuilder.createUser(def.getName(), def.getExternalId(), def.getEmail(), def.getFirstName(), def.getLastName(), def.getPassword());
					if(user == null) {
						System.out.println("Return null user " + def.getExternalId());
					} else {
						existingUserNames.put(user.getExternalId(), user);
						File avatar = new File(SetupInstance.avatars, def.getName() + ".jpg");
						if (avatar.exists()) {
							userBuilder.uploadPortrait(user, avatar);
						}
					}
				}
			} catch (URISyntaxException | IOException e) {
				e.printStackTrace();
			} finally {
				pool.giveBack(connection);
			}
		}
	}

	public static class UserDef {

		private final String name;
		private final String email;
		private final String firstName;
		private final String lastName;
		private final String password;

		public UserDef(String name, String email, String firstName, String lastName, String password) {
			this.name = name;
			this.email = email;
			this.firstName = firstName;
			this.lastName = lastName;
			this.password = password;
		}

		public String getName() {
			return name;
		}
		
		public String getExternalId() {
			return name;
		}

		public String getFirstName() {
			return firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public String getEmail() {
			return email;
		}

		public String getPassword() {
			return password;
		}
	}
}
