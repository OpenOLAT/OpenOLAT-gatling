package org.openolat.gatling.setup;

import org.openolat.gatling.setup.builder.CourseUriBuilder;
import org.openolat.gatling.setup.builder.GroupUriBuilder;
import org.openolat.gatling.setup.builder.StatementsUriBuilder;
import org.openolat.gatling.setup.voes.CourseVO;
import org.openolat.gatling.setup.voes.GroupVO;
import org.openolat.gatling.setup.voes.OlatResourceVO;
import org.openolat.gatling.setup.voes.UserVO;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Create massive amount of efficiency statements
 *
 * Created by srosse on 20.02.15.
 */
public class SetupEfficiencyStatements {

	private final RestConnectionPool pool;
	private final List<CourseVO> courses;
	private static final AtomicInteger counter = new AtomicInteger();

	public SetupEfficiencyStatements(RestConnectionPool pool, Collection<CourseVO> courses) {
		this.pool = pool;
		this.courses = new ArrayList<>(courses);
	}

	public void createStatements() throws URISyntaxException, IOException {
		CreateStatementsForCourse createStatements = new CreateStatementsForCourse(pool);
		courses.parallelStream().forEach(createStatements);
	}

	public static final class CreateStatementsForCourse implements Consumer<CourseVO> {

		private final Random rnd = new Random();
		private final RestConnectionPool pool;

		private CourseUriBuilder courseBuilder;
		private GroupUriBuilder groupBuilder;
		private StatementsUriBuilder statementBuilder;

		public CreateStatementsForCourse(RestConnectionPool pool) {
			this.pool = pool;
		}

		@Override
		public void accept(CourseVO course) {
			RestConnection connection = pool.borrow();

			groupBuilder = new GroupUriBuilder(connection);
			courseBuilder = new CourseUriBuilder(connection);
			statementBuilder = new StatementsUriBuilder(connection);

			try {
				OlatResourceVO resource = courseBuilder.getOlatResource(course);
				createStatements(course, resource);
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				pool.giveBack(connection);
			}
		}

		private void createStatements(CourseVO course, OlatResourceVO resource)
				throws URISyntaxException, IOException {
			List<UserVO> participants = getParticipantsWithGroups(course);
			for(UserVO participant:participants) {
				if(getRandom(rnd, 0.6d)) {
					boolean hasStatement = statementBuilder.hasEfficiencyStatement(resource.getKey(), participant.getKey());
					if(!hasStatement) {
						boolean passed = getRandom(rnd, 0.7d);
						float score = getRandom(rnd, 10);
						boolean statement = statementBuilder
								.create(course.getTitle(), resource.getKey(), participant.getKey(), passed, score);
						if (statement) {
							int val = counter.incrementAndGet();
							if (val % 100 == 0) {
								System.out.println(val + " statements created.");
							}
						}
					}
				}
			}
		}

		private boolean getRandom(Random rand, double average) {
			double point = Math.abs(rand.nextGaussian());
			return point < average;
		}

		private long getRandom(Random rand, int average) {
			double numOfParticipantsD = Math.abs(rand.nextGaussian() * average);
			return Math.round(numOfParticipantsD);
		}

		private List<UserVO> getParticipantsWithGroups(CourseVO course) throws URISyntaxException, IOException {
			List<UserVO> participants = new ArrayList<>(50);
			Set<Long> participantKeys = new HashSet<>();

			List<UserVO> courseParticipants = courseBuilder.getParticipants(course);
			appendUsers(participants, participantKeys, courseParticipants);
			List<GroupVO> groups = courseBuilder.getGroups(course);
			if(groups != null && !groups.isEmpty()) {
				for(GroupVO group:groups) {
					List<UserVO> groupParticipants = groupBuilder.getParticipants(group);
					appendUsers(participants, participantKeys, groupParticipants);

				}
			}
			return participants;
		}

		private void appendUsers(List<UserVO> participants, Set<Long> participantKeys, List<UserVO> newParticipants) {
			if(newParticipants == null || newParticipants.isEmpty()) return;

			for(UserVO newParticipant:newParticipants) {
				if(!participantKeys.contains(newParticipant.getKey())) {
					participantKeys.add(newParticipant.getKey());
					participants.add(newParticipant);
				}
			}
		}
	}
}