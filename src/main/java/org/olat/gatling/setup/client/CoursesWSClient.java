/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.gatling.setup.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.gatling.setup.model.CourseConfigVO;
import org.olat.gatling.setup.model.CourseVO;
import org.olat.gatling.setup.model.GroupVO;
import org.olat.gatling.setup.model.OrganisationVO;
import org.olat.gatling.setup.model.RepositoryEntryLifecycleVO;
import org.olat.gatling.setup.model.RepositoryEntryStatusEnum;
import org.olat.gatling.setup.model.RepositoryEntryVO;
import org.olat.gatling.setup.model.TaxonomyLevelVO;
import org.olat.gatling.setup.model.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 05.08.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoursesWSClient {
	
	public static final String CLOSED = "closed";
	public static final String PUB_PREPARATION = "preparation";

	public static final String OWNERS = "owners";
	public static final String COACHES = "coaches";
	public static final String PARTICIPANTS = "participants";

	private final Logger log = LoggerFactory.getLogger(CoursesWSClient.class);
	private final JsonFactory jsonFactory = new JsonFactory();
	
	private final RestConnection conn;
	
	public CoursesWSClient(RestConnection conn) {
		this.conn = conn;
	}
	
	public List<CourseVO> getCourseByExternalId(String externalId)
	throws IOException, URISyntaxException, InterruptedException {
		NameValuePair searchAttributes = new NameValuePair("externalId", externalId);
		HttpResponse<InputStream> response = conn.getJson("repo/courses", searchAttributes);
		if(response.statusCode() == 200) {
			return conn.parseList(response, CourseVO.class);
		} 
		log.error("getCourseByExternalId doesn't return correctly: {} for external id: {}", response.statusCode(), externalId);
		ConnectionUtilities.consume(response);
		if(response.statusCode() == 404) {
			return Collections.emptyList();
		}
		return null;
	}
	
	public List<CourseVO> getCourseByRepositoryEntryKey(String key)
	throws IOException, URISyntaxException, InterruptedException {
		NameValuePair searchAttributes = new NameValuePair("repositoryEntryKey", key);
		HttpResponse<InputStream> response = conn.getJson("repo/courses", searchAttributes);
		if(response.statusCode() == 200) {
			return conn.parseList(response, CourseVO.class);
		}
		log.error("getCourseByRepositoryEntryKey doesn't return correctly: {} for repository entry key: {}" , response.statusCode() , key);
		ConnectionUtilities.consume(response);
		if(response.statusCode() == 404) {
			return Collections.emptyList();
		}
		return null;
	}
	
	public List<RepositoryEntryVO> getManagedRepositoryEntries()
	throws IOException, URISyntaxException, InterruptedException {
		NameValuePair searchAttributes = new NameValuePair("managed", "true");
		HttpResponse<InputStream> response = conn.getJson("repo/entries", searchAttributes);
		if(response.statusCode() == 200) {
			return conn.parseList(response, RepositoryEntryVO.class);
		}
		log.error("getManagedRepositoryEntries doesn't return correctly: {}", response.statusCode());
		ConnectionUtilities.consume(response);
		if(response.statusCode() == 404) {
			return List.of();
		}
		return null;
	}
	
	public List<RepositoryEntryVO> getAllCourses()
	throws IOException, URISyntaxException, InterruptedException {
		NameValuePair searchAttributes = new NameValuePair("resourceType", "CourseModule");
		HttpResponse<InputStream> response = conn.getJson("repo/entries", searchAttributes);
		if(response.statusCode() == 200) {
			return conn.parseList(response, RepositoryEntryVO.class);
		}
		log.error("getManagedCourses doesn't return correctly: {}", response.statusCode());
		ConnectionUtilities.consume(response);
		if(response.statusCode() == 404) {
			return Collections.emptyList();
		}
		return null;
	}
	
	public RepositoryEntryVO getRepositoryEntry(Long repoEntryKey)
	throws IOException, URISyntaxException, InterruptedException {
		return getRepositoryEntry(repoEntryKey.toString());
	}
	
	public RepositoryEntryVO getRepositoryEntry(String repoEntryKey)
	throws IOException, URISyntaxException, InterruptedException {
		HttpResponse<InputStream> response = conn.getJson("repo/entries/" + repoEntryKey);
		int statusCode = response.statusCode();
		if(statusCode >= 200 && statusCode < 400) {
			return conn.parse(response, RepositoryEntryVO.class);
		}
		log.error("getRepositoryEntry doesn't return correctly: {} for key: {}", response.statusCode(), repoEntryKey);
		ConnectionUtilities.consume(response);
		return null;
	}
	
	public List<RepositoryEntryVO> getRepositoryEntryByExternalId(String externalId)
	throws IOException, URISyntaxException, InterruptedException {
		NameValuePair searchAttributes = new NameValuePair("externalId", externalId);
		HttpResponse<InputStream> response = conn.getJson("repo/entries", searchAttributes);
		if(response.statusCode() == 200) {
			return conn.parseList(response, RepositoryEntryVO.class);
		} 
		log.error("getRepositoryEntryByExternalId doesn't return correctly: {} for external id: {}", response.statusCode(), externalId);
		ConnectionUtilities.consume(response);
		if(response.statusCode() == 404) {
			return Collections.emptyList();
		}
		return null;
	}
	
	public List<RepositoryEntryVO> getRepositoryEntryByExternalRef(String externalRef)
	throws IOException, URISyntaxException, InterruptedException {
		NameValuePair searchAttributes = new NameValuePair("externalRef", externalRef);
		HttpResponse<InputStream> response = conn.getJson("repo/entries", searchAttributes);
		if(response.statusCode() == 200) {
			return conn.parseList(response, RepositoryEntryVO.class);
		} 
		log.error("getRepositoryEntryByExternalRef doesn't return correctly: {} for external id: {}", response.statusCode(), externalRef);
		ConnectionUtilities.consume(response);
		if(response.statusCode() == 404) {
			return Collections.emptyList();
		}
		return null;
	}
	
	public RepositoryEntryVO updateRepositoryEntry(RepositoryEntryVO repositoryEntryVO) {
		try {
			Long repoEntryKey = repositoryEntryVO.getKey();
			HttpResponse<InputStream> response = conn.post("repo/entries/" + repoEntryKey, repositoryEntryVO);
			int statusCode = response.statusCode();
			if(statusCode >= 200 && statusCode < 400) {
				repositoryEntryVO = conn.parse(response, RepositoryEntryVO.class);
			} else {
				log.error("getRepositoryEntry doesn't return correctly: {} for key: {}", response.statusCode(), repoEntryKey);
				ConnectionUtilities.consume(response);
			}
		} catch (URISyntaxException | IOException | InterruptedException e) {
			log.error("Cannot update repository entry: {}", repositoryEntryVO.getKey(), e);
		}
		return repositoryEntryVO;
	}
	
	public void updateRepositoryEntryAccess(Long repoEntryKey, RepositoryEntryStatusEnum status) {
		try {
			HttpResponse<InputStream> response = conn.put("repo/entries/" + repoEntryKey + "/status",
					new NameValuePair("newStatus", status.name()));
			int statusCode = response.statusCode();
			ConnectionUtilities.consume(response);
			if(statusCode < 200 && statusCode >= 400) {
				log.error("updateRepositoryEntryAccess doesn't return correctly: {} for key: {}", response.statusCode(), repoEntryKey);
			}
		} catch (URISyntaxException | IOException | InterruptedException e) {
			log.error("Cannot update repository entry access: {}", repoEntryKey, e);
		}
	}
	
	public CourseVO uploadCourse(String externalId, String externalRef, String displayName, URL url)
	throws URISyntaxException, IOException, InterruptedException {
		byte[] data = ConnectionUtilities.toByteArray(url);
		
		List<NameValuePair> nameValues = List.of(new NameValuePair("externalId", externalId),
				new NameValuePair("externalRef", externalRef),
				new NameValuePair("displayname", displayName));
		HttpResponse<InputStream> response = conn.post("repo/courses", "course.zip", "application/zip", nameValues, data);
		int statusCode = response.statusCode();
		if(statusCode >= 200 && statusCode < 400) {
			return conn.parse(response, CourseVO.class);
		}
		log.error("uploadCourse doesn't return correctly: {}", response.statusCode());
		ConnectionUtilities.consume(response);
		return null;
	}
	
	public CourseVO createCourse(CourseVO course, OrganisationVO organisation, String shortTitle, boolean publish, String prePublisheStatus, String templateId, boolean setAuthor)
	throws IOException, URISyntaxException, InterruptedException {
		List<NameValuePair> pairList = new ArrayList<>();
		if(course.getTitle() != null && !course.getTitle().isEmpty()) {
			pairList.add(new NameValuePair("title", course.getTitle().trim()));
			pairList.add(new NameValuePair("displayName", course.getTitle().trim()));
		}
		
		if(shortTitle != null && !shortTitle.isEmpty()) {
			pairList.add(new NameValuePair("shortTitle", shortTitle.trim()));
		} else if(course.getTitle() != null && !course.getTitle().isEmpty()) {
			pairList.add(new NameValuePair("shortTitle", course.getTitle().trim()));
		}
		
		if(course.getSoftKey() != null && !course.getSoftKey().isEmpty()) {
			pairList.add(new NameValuePair("softKey", course.getSoftKey().trim()));
		}
		
		if(course.getExternalId() != null && !course.getExternalId().isEmpty()) {
			pairList.add(new NameValuePair("externalId", course.getExternalId().trim()));
		}
		
		if(course.getExternalRef() != null && !course.getExternalRef().isEmpty()) {
			pairList.add(new NameValuePair("externalRef", course.getExternalRef().trim()));
		}
		
		if(course.getManagedFlags() != null && !course.getManagedFlags().isEmpty()) {
			pairList.add(new NameValuePair("managedFlags", course.getManagedFlags()));
		}
		
		if(course.getLocation() != null && !course.getLocation().isEmpty()) {
			pairList.add(new NameValuePair("location", course.getLocation().trim()));
		}
		
		if(course.getDescription() != null && !course.getDescription().isEmpty()) {
			pairList.add(new NameValuePair("description", course.getDescription()));
		}
		
		if(course.getAuthors() != null && !course.getAuthors().isEmpty()) {
			pairList.add(new NameValuePair("authors", course.getAuthors().trim()));
		}
		
		if(organisation != null && organisation.getKey() != null) {
			pairList.add(new NameValuePair("organisationKey", organisation.getKey().toString()));
		}
		
		if(templateId != null && !templateId.isEmpty()) {
			pairList.add(new NameValuePair("copyFrom", templateId.trim()));
		}
		if(!setAuthor) {
			pairList.add(new NameValuePair("setAuthor", "false"));
		}
		
		pairList.add(new NameValuePair("status", publish ? "published" : prePublisheStatus));
		pairList.add(new NameValuePair("allUsers", "false"));
		pairList.add(new NameValuePair("guests", "false"));
		
		NameValuePair[] pairs = pairList.toArray(new NameValuePair[pairList.size()]);
		HttpResponse<InputStream> response = conn.put("repo/courses", pairs);
		int statusCode = response.statusCode();
		if(statusCode >= 200 && statusCode < 400) {
			return conn.parse(response, CourseVO.class);
		}
		log.error("createCourse doesn't return correctly: {}", response.statusCode());
		ConnectionUtilities.consume(response);
		return null;
	}
	
	public boolean publishCourse(CourseVO courseVo)
	throws IOException, URISyntaxException, InterruptedException {
		HttpResponse<InputStream> response = conn.post("repo/courses/" + courseVo.getKey() + "/publish?access=1&membersOnly=true");
		int statusCode = response.statusCode();
		if(statusCode >= 200 && statusCode < 400) {
			CourseVO vo = conn.parse(response, CourseVO.class);
			return vo != null;
		}
		
		log.error("publishCourse doesn't return correctly: {}", statusCode);
		ConnectionUtilities.consume(response);
		return false;
	}
	
	public RepositoryEntryVO updateLifecycle(Long repoEntryKey, RepositoryEntryLifecycleVO lifecycle)
	throws IOException, URISyntaxException, InterruptedException {
		RepositoryEntryVO repoEntryVo = getRepositoryEntry(repoEntryKey);
		if(repoEntryVo != null) {
			repoEntryVo.setLifecycle(lifecycle);
			try {
				HttpResponse<InputStream> response = conn.post("repo/entries/" + repoEntryKey, repoEntryVo);
				int statusCode = response.statusCode();
				if(statusCode >= 200 && statusCode < 400) {
					return conn.parse(response, RepositoryEntryVO.class);
				}
				log.error("updateLifecycle doesn't return correctly: {}", response.statusCode());
				ConnectionUtilities.consume(response);
				
			} catch (URISyntaxException | IOException | InterruptedException e) {
				log.error("Cannot update repository entry lifecycle: {}", repoEntryVo.getKey(), e);
			}
			return repoEntryVo;
		}
		return null;
	}
	
	public CourseConfigVO getCourseConfiguration(CourseVO course)
		throws URISyntaxException, IOException, InterruptedException  {
		return getCourseConfiguration(course.getKey());
	}
	
	public CourseConfigVO getCourseConfiguration(Long courseId)
		throws URISyntaxException, IOException, InterruptedException {
		HttpResponse<InputStream> response = conn.getJson("repo/courses/" + courseId + "/configuration");
		int statusCode = response.statusCode();
		if(statusCode >= 200 && statusCode < 400) {
			return conn.parse(response, CourseConfigVO.class);
		}
		log.error("getCourseConfiguration doesn't return correctly: {}", response.statusCode());
		ConnectionUtilities.consume(response);
		return null;
	}
	
	public void updateCourseConfiguration(CourseVO course, Boolean chat, Boolean calendar, Boolean efficencyStatement, Boolean teams) {
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<>();
			if(chat != null) {
				nameValuePairs.add(new NameValuePair("chat", chat.toString()));
			}
			if(calendar != null) {
				nameValuePairs.add(new NameValuePair("calendar", calendar.toString()));
			}
			if(efficencyStatement != null) {
				nameValuePairs.add(new NameValuePair("efficencyStatement", efficencyStatement.toString()));
			}
			if(teams != null) {
				nameValuePairs.add(new NameValuePair("teams", teams.toString()));
			}
			
			if(!nameValuePairs.isEmpty()) {
				HttpResponse<InputStream> response = conn.postFormRetry("repo/courses/" + course.getKey() + "/configuration", nameValuePairs);
				int statusCode = response.statusCode();
				if(statusCode >= 200 && statusCode < 400) {
					//ok
				} else {
					log.error("updateCourseConfiguration doesn't return correctly: {}", response.statusCode());
				}
				ConnectionUtilities.consume(response);
			}
		} catch (URISyntaxException | IOException | InterruptedException e) {
			log.error("Cannot update course configuration: {}", course.getRepoEntryKey(), e);
		}
	}
	
	public void closeCourse(RepositoryEntryVO repoEntryVo) {
		RepositoryEntryStatusEnum entryStatus = RepositoryEntryStatusEnum.valueOf(repoEntryVo.getEntryStatus());
		if(entryStatus != RepositoryEntryStatusEnum.closed
				&& entryStatus != RepositoryEntryStatusEnum.deleted
				&& entryStatus != RepositoryEntryStatusEnum.trash) {
			updateCourseStatus(repoEntryVo, CoursesWSClient.CLOSED);
		}
	}
	
	public void closeCourse(CourseVO course) {
		try {
			RepositoryEntryVO repoEntryVo = getRepositoryEntry(course.getRepoEntryKey());
			RepositoryEntryStatusEnum entryStatus = RepositoryEntryStatusEnum.valueOf(repoEntryVo.getEntryStatus());
			if(entryStatus != RepositoryEntryStatusEnum.closed
					&& entryStatus != RepositoryEntryStatusEnum.deleted
					&& entryStatus != RepositoryEntryStatusEnum.trash) {
				updateCourseStatus(course, CoursesWSClient.CLOSED);
			}
		} catch (IOException | URISyntaxException | InterruptedException e) {
			log.error("Cannot close course: {}", course.getRepoEntryKey(), e);
		}
	}

	public void updateCourseStatus(RepositoryEntryVO entry, String status) {
		updateCourseStatus(entry.getOlatResourceId(), status);
	}
	
	public void updateCourseStatus(CourseVO course, String status) {
		updateCourseStatus(course.getKey(), status);
	}

	private void updateCourseStatus(Long resourceableId, String status) {
		try {
			List<NameValuePair> nameValuePairs = List.of(new NameValuePair("newStatus", status));
			HttpResponse<InputStream> response = conn.postFormRetry("repo/courses/" + resourceableId + "/status", nameValuePairs);
			int statusCode = response.statusCode();
			if(statusCode >= 200 && statusCode < 400) {
				//ok
			} else {
				log.error("updateCourseStatus doesn't return correctly: {}", response.statusCode());
			}
			ConnectionUtilities.consume(response);
		} catch (URISyntaxException | IOException | InterruptedException e) {
			log.error("Cannot update course status: {}", resourceableId, e);
		}
	}
	
	/**
	 * This returns only the coaches of the repository entry itself
	 * and not via curriculum element.
	 * 
	 * @param course The course
	 * @return A list of users
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public List<UserVO> getCoaches(RepositoryEntryVO course)
	throws IOException, URISyntaxException, InterruptedException {
		return getMembers(course, COACHES);
	}
	
	/**
	 * This returns only the participants of the repository entry itself
	 * and not via curriculum element.
	 * 
	 * @param course The course
	 * @return A list of users
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public List<UserVO> getParticipant(CourseVO course)
	throws IOException, URISyntaxException, InterruptedException {
		return getMembers(course, PARTICIPANTS);
	}

	public List<UserVO> getMembers(CourseVO course, String membership)
	throws IOException, URISyntaxException, InterruptedException {
		if(course == null || course.getRepoEntryKey() == null) {
			return List.of();
		}
		return getMembers(course.getRepoEntryKey(), membership);
	}
	
	public List<UserVO> getMembers(RepositoryEntryVO entry, String membership)
	throws IOException, URISyntaxException, InterruptedException {
		if(entry == null || entry.getKey() == null) {
			return List.of();
		}
		return getMembers(entry.getKey(), membership);
	}

	private List<UserVO> getMembers(Long repoEntryKey, String membership)
	throws IOException, URISyntaxException, InterruptedException {
		HttpResponse<InputStream> response = conn.getJson("repo/entries/" + repoEntryKey + "/" + membership);
		int returnCode = response.statusCode();
		if(returnCode == 200) {
			return conn.parseList(response, UserVO.class);
		}
		log.error("Get course {} doesn't return correctly for course {} with code {}", membership, repoEntryKey, response.statusCode());
		ConnectionUtilities.consume(response);
		return Collections.emptyList();
	}
	
	public boolean addParticipants(List<UserVO> users, CourseVO course)
	throws IOException, URISyntaxException, InterruptedException {
		return addMembers(users, course, "participants");
	}
	
	public boolean addParticipants(List<UserVO> users, RepositoryEntryVO course)
	throws IOException, URISyntaxException, InterruptedException {
		return addMembers(users, course, "participants");
	}
	
	public boolean addCoaches(List<UserVO> users, CourseVO course)
	throws IOException, URISyntaxException, InterruptedException {
		return addMembers(users, course, "coaches");
	}
	
	public boolean addCoaches(List<UserVO> users, RepositoryEntryVO course)
	throws IOException, URISyntaxException, InterruptedException {
		return addMembers(users, course, "coaches");
	}
	
	public boolean addOwners(List<UserVO> users, CourseVO course)
	throws IOException, URISyntaxException, InterruptedException {
		return addMembers(users, course, "owners");
	}
	
	public boolean addOwners(List<UserVO> users, RepositoryEntryVO course)
	throws IOException, URISyntaxException, InterruptedException {
		return addMembers(users, course, "owners");
	}
	
	public boolean addMembers(List<UserVO> users, CourseVO course, String membership)
	throws IOException, URISyntaxException, InterruptedException {
		if(course == null) return false;
		return addMembers(users, course.getRepoEntryKey(), membership);
	}
	
	public boolean addMembers(List<UserVO> users, RepositoryEntryVO entry, String membership)
	throws IOException, URISyntaxException, InterruptedException {
		if(entry == null) return false;
		return addMembers(users, entry.getKey(), membership);
	}
	
	private boolean addMembers(List<UserVO> users, Long repoEntryKey, String membership)
	throws IOException, URISyntaxException, InterruptedException {
		if(repoEntryKey == null) return false;
		if(users.size() == 1) {
			return addMember(users.get(0).getKey(), repoEntryKey, membership);
		} else if(users.size() > 1) {
			UserVO[] userArr = users.toArray(new UserVO[users.size()]);
			HttpResponse<InputStream> response = conn.putRetry("repo/entries/" + repoEntryKey + "/" + membership, userArr);
			int returnCode = response.statusCode();
			ConnectionUtilities.consume(response);
			if(returnCode == 200) {
				return true;
			}
			log.error("Add users doesn't return correctly: {} added to course {} with code {}",
					users.size(), repoEntryKey, response.statusCode());
			return false;
		}
		return true;//nothing to do
	}
	
	public boolean addParticipant(Long identityKey, CourseVO course)
	throws IOException, URISyntaxException, InterruptedException {
		return addMember(identityKey, course, "participants");
	}
	
	public boolean addCoach(Long identityKey, RepositoryEntryVO course)
	throws IOException, URISyntaxException, InterruptedException {
		return addMember(identityKey, course, "coaches");
	}
	
	public boolean addOwner(Long identityKey, CourseVO course)
	throws IOException, URISyntaxException, InterruptedException {
		return addMember(identityKey, course, "owners");
	}
	
	public boolean addMember(Long identityKey, CourseVO course, String membership)
	throws IOException, URISyntaxException, InterruptedException {
		if(course == null) return false;
		return addMember(identityKey, course.getRepoEntryKey(), membership);
	}
	
	public boolean addMember(Long identityKey, RepositoryEntryVO entry, String membership)
	throws IOException, URISyntaxException, InterruptedException {
		if(entry == null) return false;
		return addMember(identityKey, entry.getKey(), membership);
	}
	
	private boolean addMember(Long identityKey, Long repoEntryKey, String membership)
	throws IOException, URISyntaxException, InterruptedException {
		HttpResponse<Void> response = conn.putNoResponse("repo/entries/" + repoEntryKey + "/" + membership + "/" + identityKey);
		int returnCode = response.statusCode();
		if(returnCode == 200) {
			return true;
		}
		log.error("addMembers doesn't return correctly: {} added to course {} with code {}", identityKey, repoEntryKey, response.statusCode());
		return false;
	}
	
	public boolean removeParticipant(Long identityKey, CourseVO course)
	throws IOException, URISyntaxException, InterruptedException {
		return removeMember(identityKey, course, "participants");
	}
	
	public boolean removeCoach(Long identityKey, CourseVO course)
	throws IOException, URISyntaxException, InterruptedException {
		return removeMember(identityKey, course, "coaches");
	}
	
	public boolean removeOwner(Long identityKey, CourseVO course)
	throws IOException, URISyntaxException, InterruptedException {
		return removeMember(identityKey, course, "owners");
	}
	
	public boolean removeMembers(List<UserVO> membersToRemove, RepositoryEntryVO entry, String membership)
	throws IOException, URISyntaxException, InterruptedException {
		boolean allOk = true;
		for(UserVO memberToRemove:membersToRemove) {
			allOk &= removeMember(memberToRemove.getKey(), entry, membership);
		}
		return allOk;
	}
	
	public boolean removeMember(Long identityKey, CourseVO course, String membership)
	throws IOException, URISyntaxException, InterruptedException {
		return removeMember(identityKey, course.getRepoEntryKey(), membership);
	}
	
	public boolean removeMember(Long identityKey, RepositoryEntryVO entry, String membership)
	throws IOException, URISyntaxException, InterruptedException {
		return removeMember(identityKey, entry.getKey(), membership);
	}
	
	private boolean removeMember(Long identityKey, Long repoEntryKey, String membership)
	throws IOException, URISyntaxException, InterruptedException {
		HttpResponse<InputStream> response = conn.deleteRetry("repo/entries/" + repoEntryKey + "/" + membership + "/" + identityKey);
		int returnCode = response.statusCode();
		ConnectionUtilities.consume(response);
		if(returnCode == 200) {
			return true;
		}
		log.error("removeMembers doesn't return correctly: {} added to course {} with code {}",
				identityKey, repoEntryKey, response.statusCode());
		return false;
	}
	
	public List<GroupVO> getBusinesssGroups(RepositoryEntryVO course)
	throws IOException, URISyntaxException, InterruptedException {
		HttpResponse<InputStream> response = conn.getJson("repo/courses/" + course.getOlatResourceId() + "/groups");
		if(response.statusCode() == 200) {
			return conn.parseList(response, GroupVO.class);
		}
		log.error("getBusinesssGroups doesn't return correctly: {}", response.statusCode());
		ConnectionUtilities.consume(response);
		return Collections.emptyList();
	}
	
	public GroupVO addBusinesssGroup(RepositoryEntryVO course, GroupVO group)
	throws IOException, URISyntaxException, InterruptedException {
		HttpResponse<InputStream> response = conn.post("repo/courses/" + course.getOlatResourceId() + "/groups/" + group.getKey());
		if(response.statusCode() == 200) {
			return conn.parse(response, GroupVO.class);
		}
		log.error("addBusinesssGroup doesn't return correctly: {}", response.statusCode());
		ConnectionUtilities.consume(response);
		return null;
	}
	
	public void removeBusinesssGroup(RepositoryEntryVO course, GroupVO group)
	throws IOException, URISyntaxException, InterruptedException {
		HttpResponse<InputStream> response = conn.delete("repo/courses/" + course.getOlatResourceId() + "/groups/" + group.getKey());
		ConnectionUtilities.consume(response);
		if(response.statusCode() != 200) {
			log.error("removeBusinesssGroup doesn't return correctly: {}", response.statusCode());
		}
	}
	
	public List<OrganisationVO> getOrganisations(RepositoryEntryVO entry)
	throws IOException, URISyntaxException, InterruptedException {
		return getOrganisationsByRepositoryEntryKey(entry.getKey());
	}

	public List<OrganisationVO> getOrganisationsByRepositoryEntryKey(Long entryKey)
	throws IOException, URISyntaxException, InterruptedException {
		HttpResponse<InputStream> response = conn.getJson("repo/entries/" + entryKey + "/organisations");
		if(response.statusCode() == 200) {
			return parseOrganisationVOArray(response);
		}
		log.error("getOrganisations doesn't return correctly: {}", response.statusCode());
		ConnectionUtilities.consume(response);
		return Collections.emptyList();
	}
	
	public boolean addOrganisation(CourseVO course, OrganisationVO organisation)
	throws IOException, URISyntaxException, InterruptedException {
		HttpResponse<Void> response = conn.putNoResponse("repo/entries/" + course.getRepoEntryKey() + "/organisations/" + organisation.getKey());
		int statusCode = response.statusCode();
		if(statusCode == 200 || statusCode == 304) {
			return true;
		}
		log.error("addOrganisation doesn't return correctly: {}", response.statusCode());
		return false;
	}
	
	public boolean addOrganisation(RepositoryEntryVO entry, OrganisationVO organisation)
	throws IOException, URISyntaxException, InterruptedException {
		HttpResponse<Void> response = conn.putNoResponse("repo/entries/" + entry.getKey() + "/organisations/" + organisation.getKey());
		int statusCode = response.statusCode();
		if(statusCode == 200 || statusCode == 304) {
			return true;
		}
		log.error("addOrganisation doesn't return correctly: {}", response.statusCode());
		return false;
	}
	
	public boolean removeOrganisation(RepositoryEntryVO entry, OrganisationVO organisation)
	throws IOException, URISyntaxException, InterruptedException {
		return removeOrganisation(entry.getKey(), organisation);
	}
	
	public boolean removeOrganisation(CourseVO course, OrganisationVO organisation)
	throws IOException, URISyntaxException, InterruptedException {
		return removeOrganisation(course.getRepoEntryKey(), organisation);
	}
	
	private boolean removeOrganisation(Long repositoryEntryKey, OrganisationVO organisation)
	throws IOException, URISyntaxException, InterruptedException {
		HttpResponse<InputStream> response = conn.delete("repo/entries/" + repositoryEntryKey + "/organisations/" + organisation.getKey());
		int statusCode = response.statusCode();
		if(statusCode == 200 || statusCode == 304) {
			ConnectionUtilities.consume(response);
			return true;
		}
		log.error("removeOrganisation doesn't return correctly: {}", response.statusCode());
		ConnectionUtilities.consume(response);
		return false;
	}
	
	public void syncCourseTaxonomy(CourseVO course, TaxonomyLevelVO taxonomyLevel) {
		syncCourseTaxonomy(course.getRepoEntryKey(), taxonomyLevel);
	}
	
	public void syncCourseTaxonomy(RepositoryEntryVO course, TaxonomyLevelVO taxonomyLevel) {
		syncCourseTaxonomy(course.getKey(), taxonomyLevel);
	}

	private void syncCourseTaxonomy(Long repositoryEntryKey, TaxonomyLevelVO taxonomyLevel) {
		try {
			List<TaxonomyLevelVO> currentLevels = getTaxonomyLevels(repositoryEntryKey);
			if(currentLevels == null) return;// error
			
			if(taxonomyLevel == null) {
				for(TaxonomyLevelVO currentLevel:currentLevels) {
					removeTaxonomyLevel(repositoryEntryKey, currentLevel);
				}
			} else {
				boolean add = true;
				for(TaxonomyLevelVO currentLevel:currentLevels) {
					if(currentLevel.getKey().equals(taxonomyLevel.getKey())) {
						add = false;
					} else {
						removeTaxonomyLevel(repositoryEntryKey, currentLevel);
					}
				}
				if(add) {
					addTaxonomyLevel(repositoryEntryKey, taxonomyLevel);
				}
			}
		} catch (IOException | URISyntaxException | InterruptedException e) {
			log.error("", e);
		}
	}
	
	public List<TaxonomyLevelVO> getTaxonomyLevels(CourseVO course)
	throws IOException, URISyntaxException, InterruptedException {
		return getTaxonomyLevels(course.getRepoEntryKey());
	}
	
	private List<TaxonomyLevelVO> getTaxonomyLevels(Long repoEntryKey)
	throws IOException, URISyntaxException, InterruptedException {
		HttpResponse<InputStream> response = conn.getJson("repo/entries/" + repoEntryKey + "/taxonomy/levels");
		if(response.statusCode() == 200) {
			return parseTaxonomyLevelVOArray(response);
		}
		log.error("getTaxonomyLevels of course {} doesn't return correctly: {}", repoEntryKey, response.statusCode());
		ConnectionUtilities.consume(response);
		return Collections.emptyList();
	}
	
	public boolean addTaxonomyLevel(CourseVO course, TaxonomyLevelVO taxonomyLevel)
	throws IOException, URISyntaxException, InterruptedException {
		return addTaxonomyLevel(course.getRepoEntryKey(), taxonomyLevel);
	}

	private boolean addTaxonomyLevel(Long repoEntryKey, TaxonomyLevelVO taxonomyLevel)
	throws IOException, URISyntaxException, InterruptedException {
		HttpResponse<InputStream> response = conn.put("repo/entries/" + repoEntryKey + "/taxonomy/levels/" + taxonomyLevel.getKey());
		int statusCode = response.statusCode();
		if(statusCode == 200 || statusCode == 304) {
			ConnectionUtilities.consume(response);
			return true;
		}
		log.error("addTaxonomyLevel doesn't return correctly: {}", response.statusCode());
		ConnectionUtilities.consume(response);
		return false;
	}
	
	public boolean removeTaxonomyLevel(CourseVO course, TaxonomyLevelVO taxonomyLevel)
	throws IOException, URISyntaxException, InterruptedException {
		return removeTaxonomyLevel(course.getRepoEntryKey(), taxonomyLevel);
	}
	
	private boolean removeTaxonomyLevel(Long repoEntryKey, TaxonomyLevelVO taxonomyLevel)
	throws IOException, URISyntaxException, InterruptedException {
		HttpResponse<InputStream> response = conn.delete("repo/entries/" + repoEntryKey + "/taxonomy/levels/" + taxonomyLevel.getKey());
		int statusCode = response.statusCode();
		if(statusCode == 200 || statusCode == 304) {
			ConnectionUtilities.consume(response);
			return true;
		}
		log.error("removeTaxonomyLevel doesn't return correctly: {}", response.statusCode());
		ConnectionUtilities.consume(response);
		return false;
	}
	
	protected List<TaxonomyLevelVO> parseTaxonomyLevelVOArray(HttpResponse<InputStream> response) {
		try(InputStream body = response.body()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<TaxonomyLevelVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected List<OrganisationVO> parseOrganisationVOArray(HttpResponse<InputStream> response) {
		try(InputStream body = response.body()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<OrganisationVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected List<UserVO> parseUserArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<UserVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
