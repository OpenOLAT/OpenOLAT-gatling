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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.openolat.gatling.setup.RestConnection;
import org.openolat.gatling.setup.voes.*;


/**
 * 
 * <h3>Description:</h3>
 * <p>This is an helper class to build courses
 * <p>
 * Initial Date:  7 feb. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class CourseUriBuilder {
	
	private final RestConnection connection;
	
	private List<CourseVO> allCourses = new ArrayList<CourseVO>();
	
	public CourseUriBuilder(RestConnection connection) {
		this.connection = connection;
	}
	
	public CourseVO getCourseByRepositoryEntry(Long repoEntryKey)
	throws URISyntaxException, IOException  {
		URI uri = getRepositoryEntriesUri().path(repoEntryKey.toString()).build();
		HttpGet method = connection.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200) {
			InputStream body = response.getEntity().getContent();
			RepositoryEntryVO entry = connection.parse(body, RepositoryEntryVO.class);
			if(allCourses == null || allCourses.isEmpty()) {
				allCourses = getAllCourses();
			}
			for(CourseVO course:allCourses) {
				if(entry.getKey().equals(course.getRepoEntryKey())) {
					return course;
				}
			}
		} else {
			EntityUtils.consume(response.getEntity());
			System.out.println("getCourseByRepositoryEntry HTTP Error code: " + response.getStatusLine().getStatusCode());
		}
		return null;
	}
	
	public CourseVO getCourseByKey(Long courseKey)
	throws URISyntaxException, IOException  {
		URI uri = getCoursesUri().path(courseKey.toString()).build();
		HttpGet method = connection.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200) {
			InputStream body = response.getEntity().getContent();
			return connection.parse(body, CourseVO.class);
		} else {
			EntityUtils.consume(response.getEntity());
			System.out.println("getCourseByKey HTTP Error code: " + response.getStatusLine().getStatusCode());
		}
		return null;
	}
	
	public OlatResourceVO getOlatResource(CourseVO course)
	throws URISyntaxException, IOException  {
		URI uri = getCoursesUri().path(course.getKey().toString()).path("resource").build();
		HttpGet method = connection.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200) {
			InputStream body = response.getEntity().getContent();
			return connection.parse(body, OlatResourceVO.class);
		} else {
			EntityUtils.consume(response.getEntity());
			System.out.println("getCourseByKey HTTP Error code: " + response.getStatusLine().getStatusCode());
		}
		return null;
	}
	
	public List<CourseVO> getAllCourses()
	throws URISyntaxException, IOException {
		URI uri = getCoursesUri().build();
		HttpGet method = connection.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200) {
			InputStream body = response.getEntity().getContent();
			List<CourseVO> courseVOes = connection.parseCourseArray(body);
			return courseVOes;
		} else {
			EntityUtils.consume(response.getEntity());
			System.out.println("getAllCourses HTTP Error code: " + response.getStatusLine().getStatusCode());
		}
		return null;
	}
	
	public boolean deleteCourseByKey(Long courseKey)
	throws URISyntaxException, IOException  {
		URI uri = getCoursesUri().path(courseKey.toString()).build();
		HttpDelete method = connection.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		int code = response.getStatusLine().getStatusCode();
		EntityUtils.consume(response.getEntity());
		return code == 200;
	}
	
	public List<UserVO> getAuthors(CourseVO course)
	throws URISyntaxException, IOException {
		URI uri = getCourseAuthorsUri(course).build();
		HttpGet method = connection.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200) {
			InputStream body = response.getEntity().getContent();
			return connection.parseUserArray(body);
		} else {
			EntityUtils.consume(response.getEntity());
			System.out.println("getAllCourses HTTP Error code: " + response.getStatusLine().getStatusCode());
		}
		return null;
	}
	
	public boolean addAuthor(UserVO author, CourseVO course) 
	throws URISyntaxException, IOException {
		URI uri = getCourseAuthorUri(course, author).build();
		HttpPut method = connection.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		int code = response.getStatusLine().getStatusCode();
		EntityUtils.consume(response.getEntity());
		return (code == 200 || code == 201);
	}
	
	public boolean removeAuthor(UserVO author, CourseVO course) 
	throws URISyntaxException, IOException {
		URI uri = getCourseAuthorUri(course, author).build();
		HttpDelete method = connection.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		int code = response.getStatusLine().getStatusCode();
		EntityUtils.consume(response.getEntity());
		return (code == 200 || code == 201);
	}
	
	public boolean addCoach(UserVO tutor, CourseVO course) 
	throws URISyntaxException, IOException {
		URI uri = getCourseCoachUri(course, tutor).build();
		HttpPut method = connection.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		int code = response.getStatusLine().getStatusCode();
		EntityUtils.consume(response.getEntity());
		return (code == 200 || code == 201);
	}
	
	public List<UserVO> getParticipants(CourseVO course)
	throws URISyntaxException, IOException {
		URI uri = getCourseParticipantsUri(course).build();
		HttpGet method = connection.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200) {
			InputStream body = response.getEntity().getContent();
			return connection.parseUserArray(body);
		} else {
			EntityUtils.consume(response.getEntity());
			System.out.println("getAllCourses HTTP Error code: " + response.getStatusLine().getStatusCode());
		}
		return null;
	}
	
	public boolean addParticipant(UserVO participant, CourseVO course) 
	throws URISyntaxException, IOException {
		URI uri = getCourseParticipantUri(course, participant).build();
		HttpPut method = connection.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		int code = response.getStatusLine().getStatusCode();
		EntityUtils.consume(response.getEntity());
		return (code == 200 || code == 201);
	}
	
	public boolean addGroup(GroupVO group, CourseVO course)
	throws URISyntaxException, IOException {
		URI uri = getCourseGroupsUri(course).build();
		HttpPut method = connection.createPut(uri, MediaType.APPLICATION_JSON);
		connection.addJsonEntity(method, group);
		HttpResponse response = connection.execute(method);
		int code = response.getStatusLine().getStatusCode();
		EntityUtils.consume(response.getEntity());
		return (code == 200 || code == 201);
	}
	
	public List<GroupVO> getGroups(CourseVO course)
	throws URISyntaxException, IOException {
		URI uri = getCourseGroupsUri(course).build();
		HttpGet method = connection.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200) {
			InputStream body = response.getEntity().getContent();
			return connection.parseGroupArray(body);
		}
		EntityUtils.consume(response.getEntity());
		System.out.println("Cannot get groups: " + response.getStatusLine().getStatusCode());
		return Collections.emptyList();
	}
	
	public CourseVO createEmptyCourse(String shortTitle, String title)
	throws URISyntaxException, IOException {
		//create an empty course
		URI uri = getCoursesUri().queryParam("shortTitle", shortTitle).queryParam("title", title).build();
		HttpPut method = connection.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200) {
			InputStream body = response.getEntity().getContent();
			return connection.parse(body, CourseVO.class);
		}
		EntityUtils.consume(response.getEntity());
		return null;
	}
	
	public CourseVO copyCourse(String shortTitle, String title, Long originalCourseKey)
	throws URISyntaxException, IOException {
		//create an empty course
		URI uri = getCoursesUri().queryParam("shortTitle", shortTitle)
				.queryParam("title", title).queryParam("copyFrom", originalCourseKey.toString())
				.build();

		HttpPut method = connection.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200) {
			InputStream body = response.getEntity().getContent();
			return connection.parse(body, CourseVO.class);
		}
		EntityUtils.consume(response.getEntity());
		return null;
	}
	
	public CourseVO importCourse(String shortTitle, File courseZip, int access, boolean membersOnly)
	throws IOException, URISyntaxException {
		URI uri = getCoursesUri().build();
		HttpPost method = connection.createPost(uri, MediaType.APPLICATION_JSON);

		MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addTextBody("resourcename", shortTitle)
				.addTextBody("displayname", shortTitle)
				.addTextBody("softkey", UUID.randomUUID().toString().replace("-", "").substring(0, 30))
				.addTextBody("filename", courseZip.getName())
				.addBinaryBody("file", courseZip, ContentType.APPLICATION_OCTET_STREAM, courseZip.getName());

		if(membersOnly) {
			entityBuilder.addTextBody("access", "1").addTextBody("membersOnly", "true");
		} else {
			String accessStr = "3";
			if(access > 0 && access < 5) {
				accessStr = Integer.toString(access);
			}
			entityBuilder.addTextBody("access", accessStr);
		}

		method.setEntity(entityBuilder.build());
		HttpResponse response = connection.execute(method);
		CourseVO course = null;
		if(response.getStatusLine().getStatusCode() == 200) {
			course = connection.parse(response, CourseVO.class);
		} else {
			System.err.println("Error code: " + response.getStatusLine().getStatusCode());
		}
		return course;
	}
	
	public boolean updateCourseConfiguration(CourseVO course, Boolean calendar, Boolean chat,
			String cssLayoutRef, Boolean efficencyStatement, String glossarySoftkey, String sharedFolderSoftkey)
			throws URISyntaxException, IOException {
		
		UriBuilder uriBuilder = getCoursesUri().path(course.getKey().toString()).path("configuration");
		if(calendar != null) {
			uriBuilder.queryParam("calendar", calendar.toString());
		}
		if(chat != null) {
			uriBuilder.queryParam("chat", chat.toString());
		}
		if(cssLayoutRef != null) {
			uriBuilder.queryParam("cssLayoutRef", cssLayoutRef);
		}
		if(efficencyStatement != null) {
			uriBuilder.queryParam("efficencyStatement", efficencyStatement.toString());
		}
		if(glossarySoftkey != null && glossarySoftkey.length() > 0) {
			uriBuilder.queryParam("glossarySoftkey", glossarySoftkey);
		}
		if(sharedFolderSoftkey != null && sharedFolderSoftkey.length() > 0) {
			uriBuilder.queryParam("sharedFolderSoftkey", sharedFolderSoftkey);
		}
		
		URI uri = uriBuilder.build();
		HttpPost method = connection.createPost(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		int code = response.getStatusLine().getStatusCode();
		EntityUtils.consume(response.getEntity());
		return code == 200;
	}
	
	/**
	 * Publish the course
	 * @param course The course to publish
	 * @return The published course
	 * @throws java.io.IOException
	 */
	public CourseVO publish(CourseVO course)
	throws URISyntaxException, IOException {
		URI uri = getCoursePublishUri(course).queryParam("locale", "DE").build();
		HttpPost method = connection.createPost(uri, MediaType.APPLICATION_JSON);

		HttpResponse response = connection.execute(method);
		if(response.getStatusLine().getStatusCode() == 200) {
			InputStream body = response.getEntity().getContent();
			return connection.parse(body, CourseVO.class);
		}
		EntityUtils.consume(response.getEntity());
		return null;
	}
	
	
	/**
	 * @return The URI to repository entries of http://localhost:8080/olat/restapi/repo/entries
	 */
	public UriBuilder getRepositoryEntriesUri() throws URISyntaxException {
		return connection.getContextURI().path("repo").path("entries");
	}
	
	/**
	 * @return The URI to courses of http://localhost:8080/olat/restapi/repo/courses
	 */
	public UriBuilder getCoursesUri() throws URISyntaxException {
		return connection.getContextURI().path("repo").path("courses");
	}
	
	/**
	 * @return The URI to courses of http://localhost:8080/olat/restapi/repo/courses/{courseKey}/elements
	 */
	public UriBuilder getElementsUri(Long courseKey) throws URISyntaxException {
		return getCoursesUri().path(String.valueOf(courseKey)).path("elements");
	}
	
	/**
	 * @param course The course
	 * @return The URI to courses of http://localhost:8080/olat/restapi/repo/courses/{courseKey}/elements
	 */
	public UriBuilder getElementsUri(CourseVO course) throws URISyntaxException {
		return getCoursesUri().path(course.getKey().toString()).path("elements");
	}
	
	public UriBuilder getSharedFolderUri(CourseVO course) throws URISyntaxException {
		return getCoursesUri().path(course.getKey().toString()).path("resourcefolders").path("sharedfolder");
	}
	
	public UriBuilder getCourseFolderUri(CourseVO course) throws URISyntaxException {
		return getCoursesUri().path(course.getKey().toString()).path("resourcefolders").path("coursefolder");
	}
	
	/**
	 * @param course The course
	 * @return The URI to course of http://localhost:8080/olat/restapi/repo/courses/{courseKey}/authors/{userKey}
	 */
	public UriBuilder getCourseAuthorsUri(CourseVO course) throws URISyntaxException {
		return getCoursesUri().path(course.getKey().toString()).path("authors");
	}
	
	/**
	 * @param course The course
	 * @param author The author
	 * @return The URI to course of http://localhost:8080/olat/restapi/repo/courses/{courseKey}/authors/{userKey}
	 */
	public UriBuilder getCourseAuthorUri(CourseVO course, UserVO author) throws URISyntaxException {
		return getCourseAuthorsUri(course).path(author.getKey().toString());
	}
	
	/**
	 * @param course The course
	 * @return The URI to course of http://localhost:8080/olat/restapi/repo/courses/{courseKey}/tutors/{userKey}
	 */
	public UriBuilder getCourseCoachsUri(CourseVO course) throws URISyntaxException {
		return getCoursesUri().path(course.getKey().toString()).path("tutors");
	}
	
	/**
	 * @param course The course
	 * @param coach The coach
	 * @return The URI to course of http://localhost:8080/olat/restapi/repo/courses/{courseKey}/tutors/{userKey}
	 */
	public UriBuilder getCourseCoachUri(CourseVO course, UserVO coach) throws URISyntaxException {
		return getCourseCoachsUri(course).path(coach.getKey().toString());
	}
	
	/**
	 * @param course The course
	 * @return The URI to course of http://localhost:8080/olat/restapi/repo/courses/{courseKey}/participants/{userKey}
	 */
	public UriBuilder getCourseParticipantsUri(CourseVO course) throws URISyntaxException {
		return getCoursesUri().path(course.getKey().toString()).path("participants");
	}
	
	/**
	 * @param course The course
	 * @param participant The participant
	 * @return The URI to course of http://localhost:8080/olat/restapi/repo/courses/{courseKey}/participants/{userKey}
	 */
	public UriBuilder getCourseParticipantUri(CourseVO course, UserVO participant) throws URISyntaxException {
		return getCourseParticipantsUri(course).path(participant.getKey().toString());
	}
	
	/**
	 * @param course The course
	 * @return The URI to course of http://localhost:8080/olat/restapi/repo/courses/{courseKey}/publish
	 */
	public UriBuilder getCoursePublishUri(CourseVO course) throws URISyntaxException {
		return getCoursesUri().path(course.getKey().toString()).path("publish");
	}
	
	/**
	 * @param course The course
	 * @return The URI to courses of http://localhost:8080/olat/restapi/repo/courses/{courseKey}/groups
	 */
	public UriBuilder getCourseGroupsUri(CourseVO course) throws URISyntaxException {
		return getCoursesUri().path(course.getKey().toString()).path("groups");
	}
	
	/**
	 * @return The URI to courses run structure of http://localhost:8080/olat/restapi/repo/courses/{courseKey}/runstructure
	 */
	public UriBuilder getRunStructureUri(Long courseKey) throws URISyntaxException {
		return getCoursesUri().path(String.valueOf(courseKey)).path("runstructure");
	}
	
	/**
	 * @return The URI to courses editor tree model of http://localhost:8080/olat/restapi/repo/courses/{courseKey}/editortreemodel
	 */
	public UriBuilder getEditorTreeModelUri(Long courseKey) throws URISyntaxException {
		return getCoursesUri().path(String.valueOf(courseKey)).path("editortreemodel");
	}
}