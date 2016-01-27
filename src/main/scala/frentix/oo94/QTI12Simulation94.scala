package frentix.oo94

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import frentix.LoginPage

/**
 * Created by srosse on 09.12.14.
 */
class QTI12Simulation94 extends Simulation {

  val numOfUsers = Integer.getInteger("users", 500)
  val ramp = Integer.getInteger("ramp", 50)
  val url = System.getProperty("url", "http://localhost:8080")
  val jump = System.getProperty("jump", "/url/RepositoryEntry/4390912/CourseNode/91506544713400")
  val thinks = Integer.getInteger("thinks", 5)

  val httpProtocol = http
    .baseURL(url)
    .acceptHeader("text/html")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("de-de")
    .connection("keep-alive")
    .userAgentHeader("Lynx")

  val qtiScn = scenario("Test QTI 1.2")
    .exec(LoginPage.loginScreen)
    .pause(1)
    .feed(csv("oo94_user_credentials.csv"))
    .exec(LoginPage.restUrl(jump))
    .exec(QTI12TestPage94.login)
    .exec(QTI12TestPage94.startWithSections).pause(1, thinks)
    .repeat("${numOfSections}", "sectionPos") {
      exec(QTI12TestPage94.startSection)
    }
    .repeat("${numOfItems}", "itemPos") {
      exec(QTI12TestPage94.startItem, QTI12TestPage94.postItem).pause(1, thinks)
    }
    .exec(QTI12TestPage94.reloadFirstSectionToFinish).pause(1, thinks)
    .exec(QTI12TestPage94.finish).pause(1, thinks)
    .exec(QTI12TestPage94.close).pause(1, thinks)
    .exec(LoginPage.logout)


  setUp(qtiScn.inject(rampUsers(numOfUsers) over (ramp seconds))).protocols(httpProtocol)
}
