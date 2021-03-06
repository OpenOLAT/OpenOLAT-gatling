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
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import org.openolat.gatling.setup.builder.CourseUriBuilder;
import org.openolat.gatling.setup.voes.CourseVO;
import org.openolat.gatling.setup.voes.GroupVO;
import org.openolat.gatling.setup.voes.OrganisationVO;
import org.openolat.gatling.setup.voes.RepositoryEntryStatusEnum;
import org.openolat.gatling.setup.voes.UserVO;

/**
 * Make a lot of courses, in parallel
 *
 * Created by srosse on 20.02.15.
 */
public class SetupCourses {
	
	private final URL[] courseArchiveUrls = new URL[] {
		SetupCourses.class.getResource("/data/Test_1_Forum.zip"),
		SetupCourses.class.getResource("/data/Test_2_CP_Assessment.zip"),
		SetupCourses.class.getResource("/data/Test_3_Participant_folder.zip"),
		SetupCourses.class.getResource("/data/Test_4_Calendar.zip"),
		SetupCourses.class.getResource("/data/Test_5_Very_small_course.zip"),
		SetupCourses.class.getResource("/data/Test_5_Very_small_course.zip"),
		SetupCourses.class.getResource("/data/Test_6_Structured.zip"),
		SetupCourses.class.getResource("/data/Test_7_Date_enrollment.zip"),
		//SetupCourses.class.getResource("/data/Test_8_Group_enrollment.zip"),
		SetupCourses.class.getResource("/data/Test_9_Folder.zip"),
		SetupCourses.class.getResource("/data/Test_10_Notifications.zip"),
		SetupCourses.class.getResource("/data/Test_11_Checklist.zip")
	};

	private final RestConnectionPool pool;

	private final Random rnd = new Random();
	private final List<UserVO> users;
	private final List<GroupVO> groups;
	private final OrganisationVO defOrganisation;

	public SetupCourses(RestConnectionPool pool, OrganisationVO defOrganisation,
						Collection<UserVO> users, Collection<GroupVO> groups) {
		this.pool = pool;
		this.users = new ArrayList<>(users);
		this.groups = new ArrayList<>(groups);
		this.defOrganisation = defOrganisation;
	}

	protected ConcurrentMap<String,CourseVO> getCourseNamesOnInstance()
			throws URISyntaxException, IOException {

		ConcurrentMap<String, CourseVO> courses = new ConcurrentHashMap<>();
		RestConnection connection = pool.borrow();
		try {
			CourseUriBuilder courseUriBuilder = new CourseUriBuilder(connection);
			List<CourseVO> courseList = courseUriBuilder.getAllCourses();
			for(CourseVO course:courseList) {
				String name = course.getTitle();
				if(name != null && name.length() > 0) {
					courses.put(name, course);
				}
			}
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		} finally {
			pool.giveBack(connection);
		}
		return courses;
	}

	protected ConcurrentMap<String,CourseVO> createCourses(String prefix,
									int numOfCourses, int averageAuthors,
									int averageCoach, int averageParticipant,
									int averageGroups)
			throws URISyntaxException, IOException {

		List<CourseDef> coursesToCreate = new ArrayList<>();
		ConcurrentMap<String,CourseVO> existingCourses = getCourseNamesOnInstance();
		for(int i=0; i<numOfCourses; i++) {
			String title = prefix + "_" + i;
			if(existingCourses.containsKey(title)) {
				System.out.println("Course already exists: " + title);
			} else {
				int access = (int)Math.round(rnd.nextDouble() * 5.0d);
				RepositoryEntryStatusEnum status = RepositoryEntryStatusEnum.values()[access];
				
				boolean allUsers = false;
				boolean guests = false;
				if(rnd.nextDouble() < 0.2d) {
					allUsers = true;
					if(rnd.nextDouble() < 0.3d) {
						guests = true;
					}
				}
				coursesToCreate.add(new CourseDef(title, status, allUsers, guests));
			}
		}

		URL smallCourseUrl = getRandomCourseArchive();
		File smallCourse = new File(smallCourseUrl.toURI());
		CreateCourse createCourse = new CreateCourse(pool, smallCourse,
				averageAuthors, averageCoach, averageParticipant, averageGroups,
				users, groups, existingCourses, defOrganisation);
		coursesToCreate.parallelStream().forEach(createCourse);
		return existingCourses;
	}
	
	private URL getRandomCourseArchive() {
		int i = rnd.nextInt(courseArchiveUrls.length);
		return courseArchiveUrls[i];
	}

	public static class CreateCourse implements Consumer<CourseDef> {

		private final Random rnd = new Random();
		private final File smallCourse;
		private final RestConnectionPool pool;
		private CourseUriBuilder courseUriBuilder;

		private final int averageAuthors;
		private final int averageCoach;
		private final int averageParticipant;
		private final int averageGroups;

		private final List<UserVO> users;
		private final List<GroupVO> groups;
		private final OrganisationVO organisation;
		private final ConcurrentMap<String, CourseVO> courses;

		public CreateCourse(RestConnectionPool pool, File smallCourse,
							int averageAuthors, int averageCoach,
							int averageParticipant, int averageGroups,
							List<UserVO> users,  List<GroupVO> groups,
							ConcurrentMap<String, CourseVO> courses,
							OrganisationVO organisation) {
			this.smallCourse = smallCourse;
			this.pool = pool;
			this.averageAuthors = averageAuthors;
			this.averageCoach = averageCoach;
			this.averageParticipant = averageParticipant;
			this.averageGroups = averageGroups;
			this.organisation = organisation;

			this.users = users;
			this.groups = groups;
			this.courses = courses;
		}

		@Override
		public void accept(CourseDef courseDef) {

			String title = courseDef.getName();
			RestConnection connection = pool.borrow();
			try {
				courseUriBuilder = new CourseUriBuilder(connection);
				CourseVO course = courseUriBuilder.importCourse(title, smallCourse,
						organisation, courseDef.getStatus(), courseDef.isAllUsers(), courseDef.isGuests());

				if (course != null) {
					courses.put(course.getTitle(), course);
					addMembership(course, averageAuthors, averageCoach, averageParticipant);
					addGroups(course, averageGroups);
					System.out.println("Empty course created: " + title);
				} else {
					System.out.println("Create empty course return null!");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				pool.giveBack(connection);
			}
		}

		private void addGroups(CourseVO course, int avgGroups)
				throws URISyntaxException, IOException {
			long numOfGroups = getRandomNumOfUsers(rnd, avgGroups, groups);
			for(int j=0; j<numOfGroups; j++) {
				GroupVO group = getRandomUser(rnd, groups);
				if(group != null) {
					courseUriBuilder.addGroup(group, course);
				}
			}
		}

		private void addMembership(CourseVO course, int averagAuthors, int avgCoach, int avgParticipants)
				throws IOException, URISyntaxException {
			long numOfAuthors = getRandomNumOfUsers(rnd, averagAuthors, users);
			for(int j=0; j<numOfAuthors; j++) {
				addMembership(course, Roles.author);
			}

			long numOfTutors = getRandomNumOfUsers(rnd, avgCoach, users);
			for(int j=0; j<numOfTutors; j++) {
				addMembership(course, Roles.tutor);
			}

			long numOfParticipants = getRandomNumOfUsers(rnd, avgParticipants, users);
			for(int j=0; j<numOfParticipants; j++) {
				addMembership(course, Roles.participant);
			}
		}

		private void addMembership(CourseVO course, Roles membership)
				throws IOException, URISyntaxException {
			UserVO user = getRandomUser(rnd, users);
			if(user != null) {
				switch(membership) {
					case author: courseUriBuilder.addAuthor(user, course); break;
					case tutor: courseUriBuilder.addCoach(user, course); break;
					case participant: courseUriBuilder.addParticipant(user, course); break;
				}
			}
		}

		public long getRandomNumOfUsers(Random rand, int average, List<?> choices) {
			double numOfParticipantsD = Math.abs(rand.nextGaussian() * average);
			return Math.min(choices.size(), (long)numOfParticipantsD);
		}

		public <U> U getRandomUser(Random rand, List<U> names) {
			double randomPosition = rand.nextDouble() * names.size();
			long first = Math.min(Math.round(randomPosition), names.size() - 1);
			return names.get((int)first);
		}
	}

	public static class CourseDef {

		private final String name;
		private final boolean guests;
		private final boolean allUsers;
		private final RepositoryEntryStatusEnum status;

		public CourseDef(String name, RepositoryEntryStatusEnum status, boolean allUsers, boolean guests) {
			this.name = name;
			this.status = status;
			this.guests = guests;
			this.allUsers = allUsers;
		}

		public String getName() {
			return name;
		}

		public RepositoryEntryStatusEnum getStatus() {
			return status;
		}

		public boolean isGuests() {
			return guests;
		}

		public boolean isAllUsers() {
			return allUsers;
		}
	}

	private enum Roles {
		author,
		tutor,
		participant,
	}
}
