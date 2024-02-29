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
 * Settings of the test course element:
 * - Show results after test has been submitted
 * - Show score on test homepage
 * - Overview results
 * - Disable option "LMS hidden"
 * 
 * Initial date: 29 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21Simulation extends Simulation {
	
	private Integer numOfUsers = Integer.getInteger("users", 100);
	private int numOfUsersToRendezVous = Math.round(numOfUsers.intValue() * 0.7f);
	private Integer ramp = Integer.getInteger("ramp", 60);
	private String url = System.getProperty("url", "http://localhost:8080");
	private String jump = System.getProperty("jump", "/url/RepositoryEntry/1007091712/CourseNode/96184839926893");
	private Integer thinks = Integer.getInteger("thinks", 5);
	private int thinksToRendezVous = thinks * 2;
	private double rate = numOfUsers.doubleValue() / ramp.doubleValue();
	
	// Add the HttpProtocolBuilder:
	private HttpProtocolBuilder httpProtocol = http
		.baseUrl(url)
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("de-de")
		.connectionHeader("keep-alive")
		.userAgentHeader("Mozilla/5.0")
		.warmUp(url)
		//.inferHtmlResources()
		.silentResources();
	
	// Add the ScenarioBuilder:
	ScenarioBuilder qtiScn = scenario("Test QTI 2.1")
		.exec(LoginPage.loginScreen)
		.pause(thinks)
		.feed(UsersFeeder.feeder(1, numOfUsers))
		.exec(LoginPage.restUrl(jump))
		.exec(LoginPage.loginUsername)
		.pause(1)
		.exec(QTI21TestPage.loginPassword)
		.rendezVous(numOfUsersToRendezVous)
		.pause(1, thinksToRendezVous)
		.exec(QTI21TestPage.startTest)
		.exec(QTI21TestPage.startWithItems)
			.pause(1, thinks)
	    .exec(QTI21TestPage.postItem)
	    .repeat("#{numOfItems}", "itemPos").on(
	        exec(QTI21TestPage.startItem)
	        	.exec(QTI21TestPage.postItem)
	        	.pause(1, thinks)
		)
	    .exec(QTI21TestPage.endTestPart).pause(1)
	    .exec(QTI21TestPage.closeTestConfirm).pause(1)
	    .exec(QTI21TestPage.confirmCloseTestAndCloseResults).pause(1)
	    .exec(LoginPage.logout)
		;

	{
	    setUp(qtiScn.injectOpen(constantUsersPerSec(rate).during(ramp)).protocols(httpProtocol));
	}
}
