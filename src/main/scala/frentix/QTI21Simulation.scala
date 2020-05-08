package frentix

import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
 * Created by srosse on 09.12.14.
 */
class QTI21Simulation extends Simulation {

  val numOfUsers = Integer.getInteger("users", 100)
  val numOfUsersToRendezVous = (numOfUsers.toDouble * 0.7d).toInt
  val ramp = Integer.getInteger("ramp", 60)
  val url = System.getProperty("url", "http://localhost:8081")
  val jump = System.getProperty("jump", "/url/RepositoryEntry/35061760/CourseNode/92385798289062")
  val thinks = Integer.getInteger("thinks", 5)
  val thinksToRendezVous = (thinks.toInt * 2)

  val httpProtocol = http
    .baseUrl(url)
    .acceptHeader("text/html,application/json,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("de-de")
    .connectionHeader("keep-alive")
    .userAgentHeader("Mozilla/5.0")

  val qtiScn = scenario("Test QTI 2.1")
    .exec(LoginPage.loginScreen)
    .pause(1)
    .feed(csv("oo_user_credentials.csv"))
    .exec(LoginPage.restUrl(jump))
    .exec(QTI21TestPage.login)
    //.rendezVous(numOfUsersToRendezVous)
		.pause(1, thinksToRendezVous)
    .exec(QTI21TestPage.startTest)
    .exec(QTI21TestPage.startWithItems).pause(1, thinks)
    .exec(QTI21TestPage.postItem)
    .repeat("${numOfItems}", "itemPos") {
      exec(QTI21TestPage.startItem, QTI21TestPage.postItem).pause(1, thinks)
    }
    .exec(QTI21TestPage.endTestPart).pause(1)
    .exec(QTI21TestPage.closeTestConfirm).pause(1)
    .exec(QTI21TestPage.confirmCloseTestAndCloseResults).pause(1)
    .exec(LoginPage.logout)


  setUp(qtiScn.inject(rampUsers(numOfUsers) during (ramp seconds))).protocols(httpProtocol)
}
