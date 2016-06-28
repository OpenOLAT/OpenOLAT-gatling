package frentix

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import frentix.event.{FFEvent, XHREvent, FFXHREvent}
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import io.gatling.http.request.builder.{HttpRequestBuilder}
import io.gatling.http.response._

import scala.collection.mutable
import scala.collection.immutable
import scala.util.Random

import java.nio.charset.StandardCharsets.UTF_8


/**
 * Created by srosse on 09.12.14.
 */
object QTI21TestPage extends HttpHeaders {

  /**
   * Login, save the logout button for reuse and
   * save the start button of the test
   *
   * @return The request builder
   */
  def login: HttpRequestBuilder = http("Login to test")
    .post("""/dmz/1:1:oolat_login:1:0:ofo_:fid/""")
    .headers(headers_post)
    .formParam("""dispatchuri""", """o_fiooolat_login_button""")
    .formParam("""dispatchevent""", """2""")
    .formParam("""o_fiooolat_login_name""", "${username}")
    .formParam("""o_fiooolat_login_pass""", "${password}")
    .check(status.is(200))
    .check(css(".o_logout", "href").saveAs("logoutlink"))
    .check(css("a.o_sel_start_qti21assessment", "onclick")
      .find
      .transform(onclick => XHREvent(onclick))
      .saveAs("startTest"))

  def startTest: ChainBuilder = exec(session => {
    val startUrl = session("startTest").as[XHREvent]
    session.set("startUrl", startUrl.url())
  })

  /**
   * Start the test
   * @return The request builder
   */
  def startWithItems: HttpRequestBuilder = http("Start test")
    .post("""${startUrl}""")
    .formParam("cid","start")
    .headers(headers_json)
    .transformResponse(extractJsonResponse)
    .check(status.is(200))
    .check(css("#o_qti_run"))
    .check(css("""div#o_qti_menu li.o_assessmentitem a.o_sel_assessmentitem""","onclick")
      .findAll
      .transform(_.map(onclick => FFXHREvent(onclick)))
      .optional
      .saveAs("qtiItems"))
    .check(css("""div#o_qti_menu li.o_assessmentitem a.o_sel_assessmentitem""","onclick")
      .count
      .saveAs("numOfItems"))
    .check(css("""div#o_main_center_content_inner form""","action")
      .find
      .saveAs("itemAction"))

  /**
   * Finish the test and save the close button
   *
   * @return The builder
   */
  def endTestPart: ChainBuilder = exec(session => {
      val endTestPartButton = session("endTestPartButton").as[FFXHREvent]
      val parameters = endTestPartButton.formMap();
      session.set("formParameters", parameters)
    }).exec(
      http("End test part")
        .post("""${itemAction}""")
        .formParamMap("""${formParameters}""")
        .headers(headers_json)
        .check(status.is(200))
        .transformResponse(extractJsonResponse)
        .check(css("""div#o_qti_run a.o_sel_end_testpart""","onclick")
          .find
          .transform(onclick => FFXHREvent(onclick))
          .saveAs("closeTest")))

  def endTestConfirm: ChainBuilder = exec(session => {
    val endTestPartButton = session("closeTest").as[FFXHREvent]
    val parameters = endTestPartButton.formMap();
    session.set("formParameters", parameters)
  }).exec(
    http("End test and confirm")
      .post("""${itemAction}""")
      .formParamMap("""${formParameters}""")
      .headers(headers_json)
      .check(status.is(200))
      .transformResponse(extractJsonResponse)
      .check(css("""div.modal-dialog a[onclick*='link_0']""","onclick")
        .find
        .transform(onclick => XHREvent(onclick))
        .saveAs("confirmClose"))
  )

  def confirmCloseTest: ChainBuilder = exec(session => {
    val endTestPartButton = session("confirmClose").as[XHREvent]
    val closeUrl = endTestPartButton.url();
    session.set("closeUrl", closeUrl)
  }).exec(
      http("Confirm close")
        .post("""${closeUrl}""")
        .formParam("cid","link_0")
        .headers(headers_json)
        .check(status.is(200))
        .transformResponse(extractJsonResponse)
        .check(css("""div.o_statusinfo""").find)
    )

  /**
   * Click on an item of the menu and save the data
   * of the question.
   *
   * @return The builder
   */
  def startItem: ChainBuilder = exec(session => {
      val itemPos = session("itemPos").as[Int]
      val itemList = session("qtiItems").as[immutable.Vector[FFXHREvent]]
      if(itemPos < itemList.length) {
        val parameters = itemList(itemPos).formMap()
        session.set("itemParameters",parameters)
      } else {
        session.remove("itemParameters")
      }
    }).doIf(session => session.contains("itemParameters")) {
      exec(
        http("Select item:${itemPos}")
          .post("""${itemAction}""")
          .formParamMap("""${itemParameters}""")
          .headers(headers_json)
          .transformResponse(extractJsonResponse)
          .check(status.is(200))
          .check(css("""div.qtiworks.o_assessmentitem.o_assessmenttest"""))
          .check(css("""div#itemBody input[type=\'hidden\'][name^=\'qtiworks_presented_\']""", "name")
            .find(0)
            .saveAs("formPresentedHidden"))
          //submit button
          .check(css("""div#itemBody button.btn-primary""", "onclick")
            .find
            .transform(onclick => FFEvent(onclick))
            .saveAs("submitItem"))
          //single choice
          .check(css("""div#itemBody tr.choiceinteraction td.control input[type=\'radio\']""", "name")
            .findAll
            .optional
            .saveAs("singleChoiceNames"))
          .check(css("""div#itemBody tr.choiceinteraction td.control input[type=\'radio\']""", "value")
            .findAll
            .optional
            .saveAs("singleChoiceValues"))
          //multiple choices
          .check(css("""div#itemBody tr.choiceinteraction td.control input[type=\'checkbox\']""", "name")
            .findAll
            .optional
            .saveAs("multipleChoiceNames"))
          .check(css("""div#itemBody tr.choiceinteraction td.control input[type=\'checkbox\']""", "value")
            .findAll
            .optional
            .saveAs("multipleChoiceValues"))
          //fillin
          .check(css("""div#itemBody span.textEntryInteraction input[type=\'text\']""", "name")
            .findAll
            .optional
            .saveAs("fillinNames"))
          //text
          .check(css("""div#itemBody div.extendedTextInteraction textarea""", "name")
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
        val singleChoiceNames = session("singleChoiceNames").as[immutable.Vector[String]]
        val singleChoiceValues = session("singleChoiceValues").as[immutable.Vector[String]]
        val i = Random.nextInt(singleChoiceNames.length)
        parameters.put(singleChoiceNames(i), singleChoiceValues(i))
      } else if(session.contains("multipleChoiceNames")) {
        val multipleChoiceNames = session("multipleChoiceNames").as[immutable.Vector[String]]
        val multipleChoiceValues = session("multipleChoiceValues").as[immutable.Vector[String]]
        0 to multipleChoiceNames.length - 1 foreach { i =>
          if(Random.nextBoolean()) {
            parameters.put(multipleChoiceNames(i), multipleChoiceValues(i))
          }
        }
      } else if(session.contains("kprimNames")) {
        val names = session("kprimNames").as[immutable.Vector[String]]
        names.foreach(name => {
          if(!parameters.contains(name)) {
            val responseSuffix = if(Random.nextBoolean()) ":correct" else ":wrong"
            val response = name.substring(name.lastIndexOf('§') + 1) + responseSuffix
            parameters.put(name, response)
          }
        })
      } else if(session.contains("fillinNames")) {
        val names = session("fillinNames").as[immutable.Vector[String]]
        names.foreach(name => {
          parameters.put(name, randomWord)
        })
      } else if(session.contains("textNames")) {
        val names = session("textNames").as[immutable.Vector[String]]
        parameters.put(names(0), randomText)
      }
      //submit
      val submitItem = session("submitItem").as[FFEvent]
      parameters.put("dispatchuri", submitItem.elementId)
      parameters.put("dispatchevent", submitItem.actionId)
      session.set("formParameters", parameters.toMap[String,String])
  }).exec(
      http("Post item:${itemPos}")
        .post("""${itemAction}""")
        .formParamMap("""${formParameters}""")
        .formParam("""${formPresentedHidden}""", "1")
        .headers(headers_json)
        .check(status.is(200))
        .transformResponse(extractJsonResponse)
        .check(css("""button.o_sel_question_menu""", "onclick")
          .find
          .transform(onclick => FFXHREvent(onclick))
          .saveAs("toSectionButton")
        )
    ).exec(session => {
      session
        .removeAll("multipleChoiceNames", "multipleChoiceValues",
          "singleChoiceNames", "singleChoiceValues",
          "kprimNames", "kprimNames", "fillinNames", "textNames")
    })

  def toSection : ChainBuilder = exec(session => {
    val toSectionButton = session("toSectionButton").as[FFXHREvent];
    val parameters = toSectionButton.formMap();
    session.set("formParameters", parameters)
  }).exec(
    http("To section:${itemPos}")
      .post("""${itemAction}""")
      .formParamMap("""${formParameters}""")
      .headers(headers_json)
      .check(status.is(200))
      .transformResponse(extractJsonResponse)
      .check(css("#o_qti_run"))
      .check(css("""div#o_qti_menu li.o_assessmentitem a.o_sel_assessmentitem""","onclick")
        .findAll
        .transform(_.map(onclick => FFXHREvent(onclick)))
        .optional
        .saveAs("qtiItems"))
      .check(css("""div#o_qti_menu li.o_assessmentitem a.o_sel_assessmentitem""","onclick")
        .count
        .saveAs("numOfItems"))
      .check(css("""a.o_sel_end_testpart""", "onclick")
        .find
        .transform(onclick => FFXHREvent(onclick))
        .saveAs("endTestPartButton")
      )
  )

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
