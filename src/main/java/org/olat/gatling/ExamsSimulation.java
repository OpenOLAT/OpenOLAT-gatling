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
import org.olat.gatling.page.LoginPage;
import org.olat.gatling.page.QTI21TestPage;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

/**
 * Starts for tests with different numbers of users. The jump
 * URLs need to be adjusted in code.
 * 
 * Initial date: 29 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExamsSimulation extends Simulation {
	
	private Integer numOfUsers = Integer.getInteger("users", 100);
	private Integer ramp = Integer.getInteger("ramp", 10);
	private String url = System.getProperty("url", "http://localhost:8080"); 
	private static Integer thinks = Integer.getInteger("thinks", 5);
	private static int thinksToRendezVous = thinks.intValue() * 2;
	private double rate = numOfUsers.doubleValue() / ramp.doubleValue();

	private HttpProtocolBuilder httpProtocol = http
		.baseUrl(url)
		.acceptHeader("text/html,application/json,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("de-de")
		.connectionHeader("keep-alive")
		.userAgentHeader("Mozilla/5.0")
		.warmUp(url)
		.inferHtmlResources()
		.silentResources();
	
	private static ScenarioBuilder exam(String scenarioName, String jumpTo, int firstUser, int maxUsers) {
		int numOfUsers = maxUsers - firstUser;
		int numOfUsersToRendezVous = Math.round(numOfUsers * 0.7f);
		return scenario(scenarioName)
			.exec(LoginPage.loginScreen)
			.pause(1)
			.feed(UsersFeeder.feeder(firstUser, maxUsers))
			.exec(LoginPage.restUrl(jumpTo))
			.exec(LoginPage.loginUsername)
				.pause(1)
			.exec(QTI21TestPage.loginPassword)
			.rendezVous(numOfUsersToRendezVous)
				.pause(1, thinksToRendezVous)
			.exec(QTI21TestPage.startTest)
			.exec(QTI21TestPage.startWithItems).pause(1, thinks)
			.exec(QTI21TestPage.postItem)
			.repeat("#{numOfItems}", "itemPos").on(
					exec(QTI21TestPage.startItem, QTI21TestPage.postItem).pause(1, thinks)
			)
			.rendezVous(numOfUsersToRendezVous)
				.pause(1, thinksToRendezVous)
			.exec(QTI21TestPage.endTestPart).pause(1)
			.exec(QTI21TestPage.closeTestConfirm).pause(1)
			.exec(QTI21TestPage.confirmCloseTestAndCloseResults).pause(1)
			.exec(LoginPage.logout);
	}
	
	private ScenarioBuilder qti1Scn = exam("Test Gatling SR", "/url/RepositoryEntry/950272/CourseNode/101941541272702", 1500, 500);
	private ScenarioBuilder qti2Scn = exam("Test Gatling SR2", "/url/RepositoryEntry/1769472/CourseNode/101941664272850", 1, 500);
	private ScenarioBuilder qti3Scn = exam("Test Gatling SR3", "/url/RepositoryEntry/1769474/CourseNode/101941664272902", 500, 500);
	private ScenarioBuilder qti4Scn = exam("Test Gatling SR4", "/url/RepositoryEntry/1769476/CourseNode/101941664272971", 1000, 1000);
	
	{
		setUp(
			qti1Scn.injectOpen(constantUsersPerSec(rate).during(ramp)),
			qti2Scn.injectOpen(constantUsersPerSec(rate).during(ramp)),
			qti3Scn.injectOpen(constantUsersPerSec(rate).during(ramp)),
			qti4Scn.injectOpen(constantUsersPerSec(rate).during(ramp))
		).protocols(httpProtocol);
	}
}
