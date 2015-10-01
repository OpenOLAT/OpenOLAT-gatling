package org.openolat.gatling.setup;


import org.openolat.gatling.setup.voes.CourseVO;
import org.openolat.gatling.setup.voes.GroupVO;
import org.openolat.gatling.setup.voes.UserVO;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Setup an instance with some datas
 *
 * Created by srosse on 20.02.15.
 */
public class SetupInstance {

	private static final String url = "http://localhost:8080";
	private static final String username = "kanu";
	private static final String password = "kanu01";

	public static final File avatars = new File("/Users/srosse/Pictures/Avatars");

	public static void main(String[] args) {
		try {
			//build the connection pool
			List<RestConnection> connections = new ArrayList<>(4);
			connections.add(new RestConnection(new URL(url), "admin1", "admin01"));
			connections.add(new RestConnection(new URL(url), "admin2", "admin02"));
			connections.add(new RestConnection(new URL(url), "admin3", "admin03"));
			connections.add(new RestConnection(new URL(url), "admin4", "admin04"));
			for(RestConnection connection:connections) {
				connection.login();
			}

			RestConnectionPool pool = new RestConnectionPool(connections);
			//start
			new SetupInstance().setup(mediumInstance, pool);
			System.out.println("The End");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setup(Config instance, RestConnectionPool pool)
			throws IOException, URISyntaxException, InterruptedException {

		// create or load users
		ConcurrentMap<String,UserVO> users = new SetupUsers(pool)
				.createUsers("zac", instance.numOfUsers);
				//.loadExistingUsers();

		//create groups
		ConcurrentMap<String, GroupVO> groups = new SetupBusinessGroups(pool, users.values())
				.createBusinessGroups("zgrp-3", instance.numOfGroups,
						instance.averageGroupOwners, instance.averageGroupParticipants);
				//.getGroupNamesOnInstance();

		//create courses
		ConcurrentMap<String,CourseVO> courses = new SetupCourses(pool, users.values(), groups.values())
				.creatEmptyCourses("zcourse-1", instance.numOfEmptyCourses,
						instance.averageCourseOwners, instance.averageCourseTutors,
						instance.averageCourseParticipants, instance.averageCourseGroups);
				//.getCourseNamesOnInstance();

		//create efficiency statements
		new SetupEfficiencyStatements(pool, courses.values())
				.createStatements();

	}

	private static Config smallInstance = new Config();
	private static Config mediumInstance = new Config();
	private static Config largeInstance = new Config();
	private static Config extraLargeInstance = new Config();

	static {
		smallInstance.numOfUsers = 500;
		smallInstance.numOfGroups = 100;
		smallInstance.averageGroupOwners = 5;
		smallInstance.averageGroupParticipants = 20;
		smallInstance.numOfEmptyCourses = 100;
		smallInstance.averageCourseOwners = 3;
		smallInstance.averageCourseTutors = 5;
		smallInstance.averageCourseParticipants = 10;
		smallInstance.averageCourseGroups = 2;
		smallInstance.maxDepth = 3;
		smallInstance.averageCatalogChildCourses = 10;
		smallInstance.averageCatalogChildNodes = 20;

		mediumInstance.numOfUsers = 7500;
		mediumInstance.numOfGroups = 1000;
		mediumInstance.averageGroupOwners = 7;
		mediumInstance.averageGroupParticipants = 40;
		mediumInstance.numOfEmptyCourses = 1000;
		mediumInstance.averageCourseOwners = 3;
		mediumInstance.averageCourseTutors = 10;
		mediumInstance.averageCourseParticipants = 40;
		mediumInstance.averageCourseGroups = 4;
		mediumInstance.maxDepth = 3;
		mediumInstance.averageCatalogChildCourses = 20;
		mediumInstance.averageCatalogChildNodes = 35;

		largeInstance.numOfUsers = 10000;
		largeInstance.numOfGroups = 5000;
		largeInstance.averageGroupOwners = 10;
		largeInstance.averageGroupParticipants = 100;
		largeInstance.numOfEmptyCourses = 5500;
		largeInstance.averageCourseOwners = 10;
		largeInstance.averageCourseTutors = 10;
		largeInstance.averageCourseParticipants = 50;
		largeInstance.averageCourseGroups = 10;
		largeInstance.maxDepth = 2;
		largeInstance.averageCatalogChildCourses = 50;
		largeInstance.averageCatalogChildNodes = 20;

		extraLargeInstance.numOfUsers = 15000;
		extraLargeInstance.numOfGroups = 10000;
		extraLargeInstance.averageGroupOwners = 10;
		extraLargeInstance.averageGroupParticipants = 100;
		extraLargeInstance.numOfEmptyCourses = 10000;
		extraLargeInstance.averageCourseOwners = 10;
		extraLargeInstance.averageCourseTutors = 10;
		extraLargeInstance.averageCourseParticipants = 50;
		extraLargeInstance.averageCourseGroups = 10;
		extraLargeInstance.maxDepth = 2;
		extraLargeInstance.averageCatalogChildCourses = 50;
		extraLargeInstance.averageCatalogChildNodes = 20;
	}

	public static class Config {
		private int numOfUsers;
		private int numOfGroups;
		private int numOfEmptyCourses;

		private int averageGroupOwners;
		private int averageGroupParticipants;

		private int averageCourseOwners;
		private int averageCourseTutors;
		private int averageCourseParticipants;
		private int averageCourseGroups;

		private int maxDepth;
		private int averageCatalogChildNodes;
		private int averageCatalogChildCourses;
	}
}