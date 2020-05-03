package frentix

import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
 * Created by srosse on 09.12.14.
 */
class QTI12Simulation extends Simulation {

  val numOfUsers = Integer.getInteger("users", 100)
  val numOfUsersToRendezVous = (numOfUsers.toDouble * 0.7d).toInt
  val ramp = Integer.getInteger("ramp", 35)
  val url = System.getProperty("url", "http://localhost:8080")
  val jump = System.getProperty("jump", "/url/RepositoryEntry/349437952/CourseNode/93077388958245")
  val thinks = Integer.getInteger("thinks", 5)

  val httpProtocol = http
    .baseUrl(url)
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("de-de")
    .connectionHeader("keep-alive")
    .userAgentHeader("Lynx")

  val qtiScn = scenario("Test QTI 1.2")
    .exec(LoginPage.loginScreen)
    .pause(1)
    .feed(csv("oo_user_credentials.csv"))
    .exec(LoginPage.restUrl(jump))
    .exec(QTI12TestPage.login)
    .rendezVous(numOfUsersToRendezVous)
    .pause(1,10)
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


  setUp(qtiScn.inject(rampUsers(numOfUsers) during (ramp seconds))).protocols(httpProtocol)
}
