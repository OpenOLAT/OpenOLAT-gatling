package frentix

import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
 * Created by srosse on 09.12.14.
 */
class QTI21Simulation extends Simulation {

  val numOfUsers = Integer.getInteger("users", 490)
  val ramp = Integer.getInteger("ramp", 25)
  val url = System.getProperty("url", "http://localhost:8081")
  val jump = System.getProperty("jump", "/url/RepositoryEntry/35061760/CourseNode/92385798289062")
  val thinks = Integer.getInteger("thinks", 5)

  val httpProtocol = http
    .baseURL(url)
    .acceptHeader("text/html,application/json,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("de-de")
    .connection("keep-alive")
    .userAgentHeader("Mozilla/5.0")

  val qtiScn = scenario("Test QTI 2.1")
    .exec(LoginPage.loginScreen)
    .pause(1)
    .feed(csv("oo_user_credentials.csv"))
    .exec(LoginPage.restUrl(jump))
    .exec(QTI21TestPage.login)
    .exec(QTI21TestPage.startTest)
    .exec(QTI21TestPage.startWithItems).pause(1, thinks)
    .repeat("${numOfItems}", "itemPos") {
      exec(QTI21TestPage.startItem, QTI21TestPage.postItem, QTI21TestPage.toSection).pause(1, thinks)
    }
    .exec(QTI21TestPage.endTestPart).pause(1)
    .exec(QTI21TestPage.endTestConfirm).pause(1)
    .exec(QTI21TestPage.confirmCloseTest).pause(1)
    .exec(LoginPage.logout)


  setUp(qtiScn.inject(rampUsers(numOfUsers) over (ramp seconds))).protocols(httpProtocol)
}
