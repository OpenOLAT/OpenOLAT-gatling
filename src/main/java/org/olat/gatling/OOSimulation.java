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
package org.olat.gatling;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import org.olat.gatling.feeder.UsersFeeder;
import org.olat.gatling.page.CoursePage;
import org.olat.gatling.page.LoginPage;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;


/**
 * You can start this simulation with the following command line:<br>
 * mvn gatling:execute -Dusers=100 -Dramp=50 -Durl=http://localhost:8080 -Dgatling.simulationClass=frentix.OOSimulation
 * 
 * Initial date: 22 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OOSimulation extends Simulation {
	
	private Integer numOfUsers = Integer.getInteger("users", 100);
	private Integer ramp = Integer.getInteger("ramp", 60);
	private String url = System.getProperty("url", "http://localhost:8080");
	private Integer thinks = Integer.getInteger("thinks", 5);
	private double rate = numOfUsers.doubleValue() / ramp.doubleValue();
	
	// Add the HttpProtocolBuilder:
	private HttpProtocolBuilder httpProtocol = http
			.baseUrl(url)
			.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
			.acceptEncodingHeader("gzip, deflate")
			.acceptLanguageHeader("de-de")
			.connectionHeader("keep-alive")
			.userAgentHeader("Mozilla/5.0")
			.warmUp(url);
	
	// Add the ScenarioBuilder:
	ScenarioBuilder uibkScn = scenario("UIBK like")
			.exec(LoginPage.loginScreen)
			.pause(thinks)
			.feed(UsersFeeder.feeder(1, numOfUsers))
			.exec(LoginPage.loginUsername)
			.pause(1)
			.exec(LoginPage.loginPasswordToMyCourses)
			.repeat(5, "n").on(
				//exec(CoursePage.selectCourseNavigateAndBack(thinks, thinks))
				exec(CoursePage.selectCourseAndBack(thinks))
			)
			.pause(thinks)
			.exec(LoginPage.logout);

	
	{
	    setUp(uibkScn.injectOpen(constantUsersPerSec(rate).during(ramp)).protocols(httpProtocol));
	}
}
