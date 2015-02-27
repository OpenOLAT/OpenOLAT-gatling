package org.openolat.gatling.setup;

import org.openolat.gatling.setup.builder.CourseUriBuilder;
import org.openolat.gatling.setup.voes.CourseVO;
import org.openolat.gatling.setup.voes.GroupVO;
import org.openolat.gatling.setup.voes.UserVO;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * Make a lot of courses, in parallel
 *
 * Created by srosse on 20.02.15.
 */
public class SetupCourses {

	private final RestConnectionPool pool;

	private final Random rnd = new Random();
	private final List<UserVO> users;
	private final List<GroupVO> groups;

	public SetupCourses(RestConnectionPool pool,
						Collection<UserVO> users, Collection<GroupVO> groups) {
		this.pool = pool;
		this.users = new ArrayList<>(users);
		this.groups = new ArrayList<>(groups);
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

	protected ConcurrentMap<String,CourseVO> creatEmptyCourses(String prefix,
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
				int access = (int)Math.round(rnd.nextDouble() * 4.0d);
				boolean membersOnly = true;
				coursesToCreate.add(new CourseDef(title, access, membersOnly));
			}
		}

		URL smallCourseUrl = SetupCourses.class.getResource("/data/Test_5_Very_small_course.zip");
		File smallCourse = new File(smallCourseUrl.toURI());
		CreateCourse createCourse = new CreateCourse(pool, smallCourse,
				averageAuthors, averageCoach, averageParticipant, averageGroups,
				users, groups, existingCourses);
		coursesToCreate.parallelStream().forEach(createCourse);
		return existingCourses;
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
		private final ConcurrentMap<String, CourseVO> courses;

		public CreateCourse(RestConnectionPool pool, File smallCourse,
							int averageAuthors, int averageCoach,
							int averageParticipant, int averageGroups,
							List<UserVO> users,  List<GroupVO> groups,
							ConcurrentMap<String, CourseVO> courses) {
			this.smallCourse = smallCourse;
			this.pool = pool;
			this.averageAuthors = averageAuthors;
			this.averageCoach = averageCoach;
			this.averageParticipant = averageParticipant;
			this.averageGroups = averageGroups;

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
				CourseVO course = courseUriBuilder.importCourse(title, title,
						smallCourse, courseDef.getAccess(), courseDef.isMembersOnly());

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

		private void addGroups(CourseVO course, int averageGroups)
				throws URISyntaxException, IOException {
			long numOfGroups = getRandomNumOfUsers(rnd, averageGroups, groups);
			for(int j=0; j<numOfGroups; j++) {
				GroupVO group = getRandomUser(rnd, groups);
				if(group != null) {
					courseUriBuilder.addGroup(group, course);
				}
			}
		}

		private void addMembership(CourseVO course, int averagAuthors, int averageCoach, int averageParticipant)
				throws IOException, URISyntaxException {
			long numOfAuthors = getRandomNumOfUsers(rnd, averagAuthors, users);
			for(int j=0; j<numOfAuthors; j++) {
				addMembership(course, Roles.author);
			}

			long numOfTutors = getRandomNumOfUsers(rnd, averageCoach, users);
			for(int j=0; j<numOfTutors; j++) {
				addMembership(course, Roles.tutor);
			}

			long numOfParticipants = getRandomNumOfUsers(rnd, averageParticipant, users);
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

		public long getRandomNumOfUsers(Random rnd, int average, List<?> choices) {
			double numOfParticipantsD = Math.abs(rnd.nextGaussian() * average);
			return Math.min(choices.size(), (long)numOfParticipantsD);
		}

		public <U> U getRandomUser(Random rnd, List<U> names) {
			double randomPosition = rnd.nextDouble() * names.size();
			long first = Math.min(Math.round(randomPosition), names.size() - 1);
			return names.get((int)first);
		}
	}

	public static class CourseDef {

		private final String name;
		private final int access;
		private final boolean membersOnly;

		public CourseDef(String name, int access, boolean membersOnly) {
			this.name = name;
			this.access = access;
			this.membersOnly = membersOnly;
		}

		public String getName() {
			return name;
		}

		public int getAccess() {
			return access;
		}

		public boolean isMembersOnly() {
			return membersOnly;
		}
	}

	private enum Roles {
		author,
		tutor,
		participant,
	}
}
