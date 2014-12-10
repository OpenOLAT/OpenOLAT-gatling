package frentix

import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
 * Created by srosse on 09.12.14.
 */
class QTI12Simulation extends Simulation {

  val numOfUsers = Integer.getInteger("users", 10)
  val ramp = Integer.getInteger("ramp", 5)
  val url = System.getProperty("url", "http://localhost:8080")
  val jump = System.getProperty("jump", "/url/RepositoryEntry/2457600/CourseNode/90764634688340")
  val thinks = Integer.getInteger("thinks", 5)

  val httpProtocol = http
    .baseURL(url)
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("de-de")
    .connection("keep-alive")
    .userAgentHeader("Lynx")

  val qtiScn = scenario("Test QTI 1.2")
    .exec(LoginPage.loginScreen)
    .pause(1)
    .feed(csv("oo_user_credentials.csv"))
    .exec(LoginPage.restUrl(jump))
    .exec(QTI12TestPage.login)
    .exec(QTI12TestPage.startWithSections).pause(1, thinks)
    .repeat("${numOfSections}", "sectionPos") {
      exec(QTI12TestPage.startSection)
    }
    .repeat("${numOfItems}", "itemPos") {
      exec(QTI12TestPage.startItem, QTI12TestPage.postItem).pause(1, thinks)
    }
    .exec(QTI12TestPage.reloadFirstSectionToFinish).pause(1, thinks)
    .exec(QTI12TestPage.finish).pause(1, thinks)
    .exec(QTI12TestPage.close).pause(1, thinks)
    .exec(LoginPage.logout)


  setUp(qtiScn.inject(rampUsers(numOfUsers) over (ramp seconds))).protocols(httpProtocol)
}
