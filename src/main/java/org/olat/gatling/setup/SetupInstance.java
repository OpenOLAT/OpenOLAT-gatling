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
package org.olat.gatling.setup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.olat.gatling.setup.client.CoursesWSClient;
import org.olat.gatling.setup.client.RestConnection;
import org.olat.gatling.setup.client.UsersWSClient;
import org.olat.gatling.setup.model.CourseVO;
import org.olat.gatling.setup.model.ErrorVO;
import org.olat.gatling.setup.model.RepositoryEntryStatusEnum;
import org.olat.gatling.setup.model.RepositoryEntryVO;
import org.olat.gatling.setup.model.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Start the setup with this command line arguments:<br>
 * -Durl= URL of the OpenOlat instance<br>
 * -Dusername= User with administration rights to create users and courses<br>
 * -Dpassword= Password of the user<br>
 * -Dsize= Size of the instance to create (test, small, medium, large)<br>
 * 
 * 
 * Initial date: 3 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SetupInstance {

	private static final Logger log = LoggerFactory.getLogger(SetupInstance.class);
	
	private static final String USER_PREFIX = "OO_";
	private static final String REF = "VerySecure#12";
	private static final SecureRandom rnd = new SecureRandom();
	
	private static final List<Template> COURSE_TEMPLATES = List.of(
			new Template("TMPL-Form-v1", SetupInstance.class.getResource("/data/Test_1_Forum.zip")),
			new Template("TMPL-CP-Assessment-v1", SetupInstance.class.getResource("/data/Test_2_CP_Assessment.zip")),
			new Template("TMPL-Participant-folder-v1", SetupInstance.class.getResource("/data/Test_3_Participant_folder.zip")),
			new Template("TMPL-Calendar-v1", SetupInstance.class.getResource("/data/Test_4_Calendar.zip")),
			new Template("TMPL-Small-course-1-v1", SetupInstance.class.getResource("/data/Test_5_Very_small_course.zip")),
			new Template("TMPL-Small-course-2-v1", SetupInstance.class.getResource("/data/Test_5_Very_small_course.zip")),
			new Template("TMPL-Structured-v1", SetupInstance.class.getResource("/data/Test_6_Structured.zip")),
			new Template("TMPL-Date-enrollment-v1", SetupInstance.class.getResource("/data/Test_7_Date_enrollment.zip")),
			//SetupCourses.class.getResource("/data/Test_8_Group_enrollment.zip"),
			new Template("TMPL-Folder-v1", SetupInstance.class.getResource("/data/Test_9_Folder.zip")),
			new Template("TMPL-Notifications-v1", SetupInstance.class.getResource("/data/Test_10_Notifications.zip")),
			new Template("TMPL-Checkslist-v1", SetupInstance.class.getResource("/data/Test_11_Checklist.zip")),
			new Template("TMPL-Content-page-v1", SetupInstance.class.getResource("/data/Test_12_Content_Editor_Formel_Latex.zip")),
			new Template("TMPL-Calendar-v2", SetupInstance.class.getResource("/data/Test_13_Calendar.zip")),
			new Template("TMPL-Content-page-v3", SetupInstance.class.getResource("/data/Test_14_Content_text.zip")),
			new Template("TMPL-Topic-broker-v1", SetupInstance.class.getResource("/data/Test_15_Topic_broker.zip"))
		);

	
	public static final void main(String[] args) {
		try {
			String url = System.getProperty("url");
			String username = System.getProperty("username");
			String creds = System.getProperty("password");
			String size = System.getProperty("size", "test");
			Config config = Config.valueOf(size);
			
			URL baseUrl = new URL(url);
			RestConnection connection = new RestConnection(baseUrl, "restapi/", username, creds, "UTF-8", 30);
			UsersWSClient usersBean = new UsersWSClient(connection);
			CoursesWSClient coursesBean = new CoursesWSClient(connection);
			SetupInstance setup = new SetupInstance();

			setup.setup(config, usersBean, coursesBean);
		} catch (MalformedURLException e) {
			log.error("", e);
		}
	}
	
	private void setup(Config config, UsersWSClient usersBean, CoursesWSClient coursesBean) {
		log.info("Start setup");
		log.info("Synchronize templates");
		List<RepositoryEntryVO> templates = setupTemplates(coursesBean);
		log.info("Synchronize courses");
		List<RepositoryEntryVO> courses = setupCourses(config.numOfCourses(), templates, coursesBean);
		log.info("Synchronize users");
		List<UserVO> users = setupUsers(config.numOfUsers(), usersBean);
		LoopUsers loopUsers = new LoopUsers(users);
		log.info("Synchronize courses members");
		setupMembers(config.numOfCoaches(), config.numOfParticipants(), courses, loopUsers, coursesBean);
		log.info("Ensure at least 5 courses pro users as participant");
		ensureParticipations(courses, loopUsers, coursesBean);
		log.info("End setup");
	}
	
	private void ensureParticipations(List<RepositoryEntryVO> courses, LoopUsers users, CoursesWSClient coursesBean) {
		try {
			int count = 0;
			List<UserVO> usersList = users.getUsers();
			for(UserVO user:usersList) {
				if(user == null) continue;
				
				for(int i=users.getParticipation(user); i<5; i++) {
					RepositoryEntryVO course = courses.get(count++ % courses.size());
					coursesBean.addMember(user.getKey(), course, "participants");
				}
			}
		} catch (IOException | URISyntaxException | InterruptedException e) {
			log.error("", e);
		}
	}
	
	private void setupMembers(int numOfCoaches, int numOfParticipants,
			List<RepositoryEntryVO> courses, LoopUsers users, CoursesWSClient coursesBean) {
		for(RepositoryEntryVO course:courses) {
			setupMembers(numOfCoaches, "coaches", course, users, coursesBean);
			List<UserVO> participants = setupMembers(numOfParticipants, "participants", course, users, coursesBean);
			users.addParticipation(participants);
		}
	}
	
	private List<UserVO> setupMembers(int numOfMembers, String membership, RepositoryEntryVO course, LoopUsers users, CoursesWSClient coursesBean) {
		try {
			List<UserVO> members = coursesBean.getMembers(course, membership);
			for(int i=members.size(); i<numOfMembers; i++) {
				UserVO user = users.get();
				coursesBean.addMember(user.getKey(), course, membership);
				members.add(user);
			}
			return members;
		} catch (IOException | URISyntaxException | InterruptedException e) {
			log.error("", e);
			return List.of();
		}
	}
	
	private List<RepositoryEntryVO> setupCourses(int numOfCourses, List<RepositoryEntryVO> templates, CoursesWSClient coursesBean) {
		List<RepositoryEntryVO> courses = new ArrayList<>();
		for(int i=0; i<numOfCourses; i++) {
			String externalId = "GTAL_" + i;
			String displayName = "Kurs - " + i;
			RepositoryEntryVO template = templates.get(i % templates.size());
			List<RepositoryEntryVO> entries = createCourse(externalId, displayName, template, coursesBean);
			courses.addAll(entries);
		}
		
		return courses;
	}
	
	private List<RepositoryEntryVO> createCourse(String externalId, String displayName, RepositoryEntryVO template, CoursesWSClient coursesBean) {
		try {
			List<RepositoryEntryVO> entries = coursesBean.getRepositoryEntryByExternalId(externalId);
			if(entries.isEmpty()) {
				CourseVO courseVo = new CourseVO();
				courseVo.setTitle(displayName);
				courseVo.setExternalId(externalId);
				courseVo.setExternalRef(externalId);	
				CourseVO course = coursesBean.createCourse(courseVo, null, displayName, true,
						RepositoryEntryStatusEnum.published.name(), template.getKey().toString(), true);
				if(course != null) {
					RepositoryEntryVO entry = coursesBean.getRepositoryEntry(course.getRepoEntryKey());
					return entry == null ? List.of() : List.of(entry);
				}
			} else {
				return entries;
			}
		} catch (IOException | URISyntaxException | InterruptedException e) {
			log.error("", e);
		}
		return List.of();
	}
	
	private List<RepositoryEntryVO> setupTemplates(CoursesWSClient coursesBean) {
		List<RepositoryEntryVO> templates = new ArrayList<>();
		try {
			for(Template template:COURSE_TEMPLATES) {
				List<RepositoryEntryVO> entries = coursesBean.getRepositoryEntryByExternalId(template.externalId());
				if(entries.isEmpty()) {
					CourseVO course = coursesBean.uploadCourse(template.externalId(), template.externalId(), template.externalId(), template.url());
					if(course != null) {
						RepositoryEntryVO entry = coursesBean.getRepositoryEntry(course.getRepoEntryKey());
						if(entry != null) {
							templates.add(entry);
						}
					}
				} else {
					templates.addAll(entries);
				}
			}
		} catch (URISyntaxException | IOException | InterruptedException e) {
			log.error("", e);
		}
		return templates;
	}
	
	private List<UserVO> setupUsers(int numOfUsers, UsersWSClient usersBean) {	
		try {
			Map<String,UserVO> externalIds = usersBean.searchAllUsers().stream()
					.filter(usr -> StringHelper.containsNonWhitespace(usr.getExternalId()))
					.collect(Collectors.toMap(UserVO::getExternalId, usr -> usr, (u, v) -> u));
			NameDefs nameDefs = loadNames();

			List<UserVO> usersList = new ArrayList<>();
			for(int i=0; i<numOfUsers; i++) {
				String externalId = USER_PREFIX + i;
				UserVO user = externalIds.get(externalId);
				if(user == null) {
					user = createUser(externalId, nameDefs, usersBean);
				}
				if(user != null) {
					usersList.add(user);
				}
			}
			return usersList;
		} catch (IOException | URISyntaxException | InterruptedException e) {
			log.error("", e);
			return List.of();
		}
	}
	
	private UserVO createUser(String externalId, NameDefs nameDefs, UsersWSClient usersBean)
	throws IOException, URISyntaxException, InterruptedException {
		UserVO user = new UserVO();
		user.setExternalId(externalId);
		
		String firstName = nameDefs.randomFirstName();
		String lastName = nameDefs.randomLastName();
		
		user.setEmail(email(externalId, firstName, lastName));
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setLogin(externalId.toLowerCase());
		user.setPassword(REF);
		
		List<ErrorVO> errors = new ArrayList<>();
		UserVO newUser = usersBean.createUser(user, errors);
		if(!errors.isEmpty()) {
			for(ErrorVO error:errors) {
				log.error("Cannot create user with externalId {}: {}", externalId, error.getTranslation());
			}
		}
		return newUser;
	}
	
	private String email(String externalId, String firstName, String lastName) {
		String email = (firstName + "." + lastName + "." + externalId + "@mt2014.com");
		String emailNormalized = Normalizer.normalize(email, Normalizer.Form.NFKD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+","")
				.replace(" ", "");
		return emailNormalized.toLowerCase();
	}
	
	private NameDefs loadNames() {
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
			log.error("", e);
		}

		return new NameDefs(firstNames, lastNames);
	}
	
	public record NameDefs(List<String> firstNames, List<String> lastNames) {
		
		public String randomFirstName() {
			return getName(firstNames);
		}
		
		public String randomLastName() {
			return getName(lastNames);
		}
		
		private String getName(List<String> names) {
			double randomPosition = rnd.nextDouble() * names.size();
			long first = Math.min(Math.round(randomPosition), names.size() - 1);
			return names.get((int)first);
		}
	}
		
	public static final class LoopUsers {
		
		private int current = 0;
		private final List<UserVO> users;
		private final Map<UserVO,AtomicInteger> participation = new HashMap<>();
		
		public LoopUsers(List<UserVO> users) {
			this.users = users;
		}
		
		public List<UserVO> getUsers() {
			return List.copyOf(users);
		}
		
		public UserVO get() {
			if(current < 0 || current >= users.size()) {
				current = 0;
			}
			UserVO user = users.get(current);
			current++;
			return user;
		}
		
		public int getParticipation(UserVO user) {
			AtomicInteger count = participation.get(user);
			return count == null ? 0 : count.get();
		}
		
		public void addParticipation(List<UserVO> participants) {
			for(UserVO participant:participants) {
				participation
					.computeIfAbsent(participant, usr -> new AtomicInteger(0))
					.incrementAndGet();
			}
		}
	}
	
	public record Template(String externalId, URL url) {
		//
	}
	
	public record Config(int numOfUsers, int numOfCourses, int numOfCoaches, int numOfParticipants) {
		
		public static final Config valueOf(String size) {
			return switch(size) {
				case "small" -> smallInstance();
				case "medium" -> mediumInstance();
				case "large" -> largeInstance();
				default -> test();
			};
		}
		
		public static final Config test() {
			return new Config(100, 5, 3, 10);
		}
		
		public static final Config smallInstance() {
			return new Config(500, 100, 5, 10);
		}
		
		public static final Config mediumInstance() {
			return new Config(7500, 1000, 6, 40);
		}
		
		public static final Config largeInstance() {
			return new Config(40000, 10000, 7, 50);
		}
	}
}
