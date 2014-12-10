package frentix

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.request.builder.{HttpRequestWithParamsBuilder, HttpRequestBuilder}

import scala.collection.mutable
import scala.util.Random


/**
 * Created by srosse on 09.12.14.
 */
object QTI12TestPage extends HttpHeaders {

  /**
   * Login, save the logout button for reuse and
   * save the start button of the test
   *
   * @return The request builder
   */
  def login: HttpRequestWithParamsBuilder = http("Login to test")
    .post("""/dmz/1:1:oolat_login:1:0:ofo_:fid/""")
    .headers(headers_post)
    .formParam("""dispatchuri""", """o_fiooolat_login_button""")
    .formParam("""dispatchevent""", """2""")
    .formParam("""o_fiooolat_login_name""", "${username}")
    .formParam("""o_fiooolat_login_pass""", "${password}")
    .check(status.is(200))
    .check(css(".o_logout", "href").saveAs("logoutlink"))
    .check(css("a.o_sel_start_qti12_test", "href").saveAs("startTest"))

  /**
   * Start the test
   * @return The request builder
   */
  def startWithSections: HttpRequestBuilder = http("Start test")
    .get("""${startTest}""")
    .headers(headers)
    .check(status.is(200))
    .check(css("#o_qti_run"))
    .check(css("#o_qti_run_menu"))
    .check(css("""div#o_qti_menu div.o_qti_menu_section a""","href")
      .findAll
      .transform(_.map(href => href))
      .optional
      .saveAs("qtiSections"))
    .check(css("""div#o_qti_menu div.o_qti_menu_section a""","href")
      .count
      .saveAs("numOfSections"))
    .check(css("""div#o_qti_menu div.o_qti_menu_item a""","href")
      .findAll
      .transform(_.map(href => href))
      .optional
      .saveAs("qtiItems"))
    .check(css("""div#o_qti_menu div.o_qti_menu_item a""","href")
      .count
      .saveAs("numOfItems"))

  /**
   * Click on a section link.
   *
   * @return The builder
   */
  def startSection: ChainBuilder = exec(session => {
      val sectionPos = session("sectionPos").as[Int]
      val sectionList = session("qtiSections").as[mutable.Buffer[String]]
      session.set("currentSection", sectionList(sectionPos))
    }).exec(
      http("Select section:${sectionPos}")
        .get("""${currentSection}""")
        .headers(headers)
        .check(status.is(200))
        .check(css("""div#o_qti_run_content_inner h3"""))
        .check(css("""div#o_qti_menu div.o_qti_menu_section a""","href")
          .findAll
          .transform(_.map(href => href))
          .optional
          .saveAs("qtiSections"))
        .check(css("""div#o_qti_menu a.o_sel_qti_menu_item""","href")
          .findAll
          .transform(_.map(href => href))
          .optional
          .saveAs("qtiItems")))

  /**
   * Reload the first section to grab the finish button
   *
   * @return The builder
   */
  def reloadFirstSectionToFinish: ChainBuilder = exec(session => {
      val sectionList = session("qtiSections").as[mutable.Buffer[String]]
      session.set("currentSection", sectionList(0))
    }).exec(
      http("Reload first section")
        .get("""${currentSection}""")
        .headers(headers)
        .check(status.is(200))
        .check(css("""div#o_qti_run_content_inner h3"""))
        .check(css("""div.o_header_with_buttons div.o_button_group a.btn-default""","href")
          .find(0)
          .saveAs("finishTest")))

  /**
   * Finish the test and save the close button
   *
   * @return The builder
   */
  def finish: ChainBuilder = exec(
      http("Finish test")
        .get("""${finishTest}""")
        .headers(headers)
        .check(status.is(200))
        .check(css("""div.o_header_with_buttons div.o_button_group a.btn-primary""","href")
          .find(0)
          .saveAs("closeTest"))
        .check(css("""#o_qti_results""").exists))

  /**
   * Close the test and save the logout button.
   *
   * @return The builder
   */
  def close: ChainBuilder = exec(
    http("Close test")
      .get("""${closeTest}""")
      .headers(headers)
      .check(status.is(200))
      .check(css(".o_logout", "href").saveAs("logoutlink")))

  /**
   * Click on an item of the menu and save the data
   * of the question.
   *
   * @return The builder
   */
  def startItem: ChainBuilder = exec(session => {
      val itemPos = session("itemPos").as[Int]
      val itemList = session("qtiItems").as[mutable.Buffer[String]]
      if(itemPos < itemList.length) {
        session.set("currentItem", itemList(itemPos))
      } else {
        session.remove("currentItem")
      }
    }).doIf(session => session.contains("currentItem")) {
      exec(
        http("Select item:${itemPos}")
          .get("""${currentItem}""")
          .headers(headers)
          .check(status.is(200))
          .check(css("""form#ofo_iq_item""", "action")
            .find(0)
            .saveAs("formAction"))
          .check(css("""div.o_qti_item>input[type=\'hidden\']""", "name")
            .find(0)
            .saveAs("formHidden"))
          //single choice
          .check(css("""div.o_qti_item_choice div.o_qti_item_choice_option input[type=\'radio\']""", "name")
            .findAll
            .optional
            .saveAs("singleChoiceNames"))
          .check(css("""div.o_qti_item_choice div.o_qti_item_choice_option input[type=\'radio\']""", "value")
            .findAll
            .optional
            .saveAs("singleChoiceValues"))
          //multiple choices
          .check(css("""div.o_qti_item_choice input[type=\'checkbox\']""", "name")
            .findAll
            .optional
            .saveAs("multipleChoiceNames"))
          .check(css("""div.o_qti_item_choice input[type=\'checkbox\']""", "value")
            .findAll
            .optional
            .saveAs("multipleChoiceValues"))
          //kprim
          .check(css("""table.o_qti_item_kprim td.o_qti_item_kprim_input input[type=\'radio\']""", "name")
            .findAll
            .optional
            .saveAs("kprimNames"))
          //fillin
          .check(css("""div.o_qti_item input[type=\'text\']""", "name")
            .findAll
            .optional
            .saveAs("fillinNames"))
          //text
          .check(css("""div.o_qti_item textarea""", "name")
            .findAll
            .optional
            .saveAs("textNames"))
      )
    }

  /**
   * Answer a question with the data from a previous
   * call to startItem.
   *
   * @return The builder
   */
  def postItem: ChainBuilder = exec(session => {
      val parameters = collection.mutable.HashMap[String, String]()
      if(session.contains("singleChoiceNames")) {
        val singleChoiceNames = session("singleChoiceNames").as[mutable.Buffer[String]]
        val singleChoiceValues = session("singleChoiceValues").as[mutable.Buffer[String]]
        val i = Random.nextInt(singleChoiceNames.length)
        parameters.put(singleChoiceNames(i), singleChoiceValues(i))
      } else if(session.contains("multipleChoiceNames")) {
        val multipleChoiceNames = session("multipleChoiceNames").as[mutable.Buffer[String]]
        val multipleChoiceValues = session("multipleChoiceValues").as[mutable.Buffer[String]]
        for (i <- 0 until multipleChoiceNames.length) {
          if(Random.nextBoolean()) {
            parameters.put(multipleChoiceNames(i), multipleChoiceValues(i))
          }
        }
      } else if(session.contains("kprimNames")) {
        val names = session("kprimNames").as[mutable.Buffer[String]]
        val parameters = collection.mutable.HashMap[String, String]()
        names.foreach(name => {
          if(!parameters.contains(name)) {
            val responseSuffix = if(Random.nextBoolean()) ":correct" else ":wrong"
            val response = name.substring(name.lastIndexOf('§') + 1) + responseSuffix
            parameters.put(name, response)
          }
        })
      } else if(session.contains("fillinNames")) {
        val names = session("fillinNames").as[mutable.Buffer[String]]
        names.foreach(name => {
          parameters.put(name, randomWord)
        })
      } else if(session.contains("textNames")) {
        val names = session("textNames").as[mutable.Buffer[String]]
        parameters.put(names(0), randomText)
      }
      session.set("formParameters", parameters.toMap[String,String])
  }).exec(
      http("Post item:${itemPos}")
        .post("""${formAction}""")
        .formParamMap("""${formParameters}""")
        .formParam("olat_fosm", "Save")
        .formParam("""${formHidden}""", "")
        .headers(headers_post)
        .check(status.is(200))
        .check(css("""div#o_qti_menu div.o_qti_menu_section a""", "href")
          .findAll
          .transform(_.map(href => href))
          .optional
          .saveAs("qtiSections"))
        .check(css("""div#o_qti_menu a.o_sel_qti_menu_item""", "href")
          .findAll
          .transform(_.map(href => href))
          .optional
          .saveAs("qtiItems")))
    .exec(session => {
      session
        .removeAll("multipleChoiceNames", "multipleChoiceValues",
          "singleChoiceNames", "singleChoiceValues",
          "kprimNames", "kprimNames", "fillinNames", "textNames")
    })

  def randomWord: String = {
    val rnd = Random.nextInt(10)
    var word = "test"
    rnd match {
      case 0 => word = "Test"
      case 1 => word = "Akula"
      case 2 => word = "961 bis 964 BGB"
      case 3 => word = "тест" //Test (russian)
      case 4 => word = "您好" //Hi (chinese)
      case 5 => word = "綾波レイ" //Ayanami Rei (japanese)
      case 6 => word = "Lorem ipsum"
      case 7 => word = "C11++"
      case 8 => word = "PostgreSQL"
      case 9 => word = "not"
    }
    word
  }

  def randomText: String = {
    val rnd = Random.nextInt(2)
    var text = "test"
    rnd match {
      case 0 => text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce at dui sagittis, sagittis felis sed, consequat elit. Duis efficitur sagittis magna ac lacinia. Nunc molestie metus nec leo molestie ornare. Vestibulum scelerisque lacus consequat, cursus sapien et, rhoncus sapien. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus."
      case 1 => text = "A learning management system (LMS) is a software application for the administration, documentation, tracking, reporting and delivery of electronic educational technology (also called e-learning) education courses or training programs.[1] LMSs range from systems for managing training and educational records to software for distributing online or blended/hybrid college courses over the Internet with features for online collaboration. Colleges and universities use LMSs to deliver online courses and augment on-campus courses. Corporate training departments use LMSs to deliver online training, as well as to automate record-keeping and employee registration."
    }
    text
  }
}
