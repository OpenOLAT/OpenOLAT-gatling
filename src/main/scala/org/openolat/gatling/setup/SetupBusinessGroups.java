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

import org.openolat.gatling.setup.builder.GroupUriBuilder;
import org.openolat.gatling.setup.voes.GroupVO;
import org.openolat.gatling.setup.voes.UserVO;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * Build a lot of groups.
 *
 * Created by srosse on 20.02.15.
 */
public class SetupBusinessGroups {

	private final RestConnectionPool pool;
	private final List<UserVO> users;

	public SetupBusinessGroups(RestConnectionPool pool, Collection<UserVO> users) {
		this.pool = pool;
		this.users = new ArrayList<>(users);
	}

	public ConcurrentMap<String, GroupVO> createBusinessGroups(String prefix, int numOfGroups,
									 int averageOwners, int averageParticipants)
			throws IOException, URISyntaxException {

		ConcurrentMap<String, GroupVO> existingGroups = getGroupNamesOnInstance();
		List<GroupDef> groupsToCreate = new ArrayList<>();
		for(int i=0; i<numOfGroups; i++) {
			String name = prefix + "_" + i;
			if(existingGroups.containsKey(name)) {
				System.out.println("Business group already exists: " + name);
			} else {
				String description = "Group created by the test REST API (" + i + ").";
				groupsToCreate.add(new GroupDef(name, description));
			}
		}

		CreateGroup createGroup = new CreateGroup(pool,
				averageOwners, averageParticipants,
				users, existingGroups);
		groupsToCreate.parallelStream().forEach(createGroup);
		return existingGroups;
	}

	public ConcurrentMap<String, GroupVO> getGroupNamesOnInstance()
			throws IOException, URISyntaxException {
		ConcurrentMap<String, GroupVO> names = new ConcurrentHashMap<>();
		RestConnection connection = pool.borrow();
		try {
			GroupUriBuilder groupUriBuilder = new GroupUriBuilder(connection);
			List<GroupVO> groups = groupUriBuilder.getGroups();
			for (GroupVO group : groups) {
				String name = group.getName();
				if (name != null) {
					names.put(name, group);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.giveBack(connection);
		}
		return names;
	}

	public static class CreateGroup implements Consumer<GroupDef> {

		private final int averageOwners;
		private final int averageParticipants;
		private final RestConnectionPool pool;
		private final List<UserVO> users;
		private final ConcurrentMap<String, GroupVO> groups;

		public CreateGroup(RestConnectionPool pool, int averageOwners, int averageParticipants,
						   List<UserVO> users, ConcurrentMap<String, GroupVO> groups) {
			this.pool = pool;
			this.averageOwners = averageOwners;
			this.averageParticipants = averageParticipants;
			this.users = users;
			this.groups = groups;
		}

		@Override
		public void accept(GroupDef groupDef) {
			RestConnection connection = pool.borrow();
			try {
				GroupUriBuilder groupBuilder = new GroupUriBuilder(connection);
				GroupVO group = groupBuilder.createGroup(groupDef.getName(), groupDef.getDescription());
				if(group != null) {
					Random rnd = new Random();
					groups.put(group.getName(), group);

					boolean owners = getRandom(rnd, 0.10);
					boolean participants = getRandom(rnd, 0.05);
					groupBuilder.setConfiguration(group.getKey(), owners, participants);

					List<Membership> memberships = new ArrayList<>();
					long numOfParticipants = getRandomNumOfUsers(rnd, averageParticipants, users);
					for (int j = 0; j < numOfParticipants; j++) {
						UserVO user = getRandomUser(rnd, users);
						memberships.add(new Membership(user, false));
					}

					long numOfOwners = getRandomNumOfUsers(rnd, averageOwners, users);
					for (int j = 0; j < numOfOwners; j++) {
						UserVO user = getRandomUser(rnd, users);
						memberships.add(new Membership(user, true));
					}

					CreateMembership createMembership = new CreateMembership(group, groupBuilder);
					memberships.stream().forEach(createMembership);

					System.out.println("Group created: " + group.getName());
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				pool.giveBack(connection);
			}
		}

		public boolean getRandom(Random rnd, double average) {
			double point = Math.abs(rnd.nextGaussian());
			return point < average;
		}

		public long getRandomNumOfUsers(Random rnd, int average, List<UserVO> choices) {
			double numOfParticipantsD = Math.abs(rnd.nextGaussian() * average);
			return Math.min(choices.size(), (long)numOfParticipantsD);
		}

		public UserVO getRandomUser(Random rnd, List<UserVO> names) {
			double randomPosition = rnd.nextDouble() * names.size();
			long first = Math.min(Math.round(randomPosition), names.size() - 1);
			return names.get((int)first);
		}
	}

	public static class GroupDef {

		private final String name;
		private final String description;

		public GroupDef(String name, String description) {
			this.name = name;
			this.description = description;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}
	}

	public static class CreateMembership implements Consumer<Membership> {

		private final GroupVO group;
		private final GroupUriBuilder groupBuilder;

		public CreateMembership(GroupVO group, GroupUriBuilder groupBuilder) {
			this.group = group;
			this.groupBuilder = groupBuilder;
		}

		@Override
		public void accept(Membership membership) {
			try {
				UserVO user = membership.getUser();
				if(membership.isCoach()) {
					groupBuilder.addOwner(group, user);
				} else {
					groupBuilder.addParticipant(group, user);
				}
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	public static class Membership {

		private UserVO user;
		private boolean coach;

		public Membership(UserVO user, boolean coach) {
			this.user = user;
			this.coach = coach;
		}

		public UserVO getUser() {
			return user;
		}

		public boolean isCoach() {
			return coach;
		}
	}
}
