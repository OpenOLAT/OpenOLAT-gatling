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
import io.gatling.http.check.HttpCheck


/**
 * Created by srosse on 09.12.14.
 */
object QTI21TestPage extends HttpHeaders {
	
	/**
	 * List of checks: find the next button and optionally save it as
	 * "nextElement" in session.
	 */
	val checkListAssessmentItemForm: Seq[HttpCheck] = Seq(
			css("""div#itemBody input[type=\'hidden\'][name^=\'qtiworks_presented_\']""", "name")
            .find(0)
            .saveAs("formPresentedHidden"),
      css("""div.qtiworks.o_assessmentitem.o_assessmenttest button.btn-primary""", "onclick")
            .find
            .transform(onclick => FFEvent(onclick))
            .saveAs("submitItem"),
      css("""div#itemBody tr.choiceinteraction td.control input[type=\'radio\']""", "name")
            .findAll
            .optional
            .saveAs("singleChoiceNames"),
      css("""div#itemBody tr.choiceinteraction td.control input[type=\'radio\']""", "value")
            .findAll
            .optional
            .saveAs("singleChoiceValues"),
          //multiple choices
      css("""div#itemBody tr.choiceinteraction td.control input[type=\'checkbox\']""", "name")
            .findAll
            .optional
            .saveAs("multipleChoiceNames"),
      css("""div#itemBody tr.choiceinteraction td.control input[type=\'checkbox\']""", "value")
            .findAll
            .optional
            .saveAs("multipleChoiceValues"),
          //fillin
      css("""div#itemBody span.textEntryInteraction input[type=\'text\']""", "name")
            .findAll
            .optional
            .saveAs("fillinNames"),
          //text
      css("""div#itemBody div.extendedTextInteraction textarea""", "name")
            .findAll
            .optional
            .saveAs("textNames"))
  
  /**
   * Find and save the list of items in the menu, count them too and
   * save the button "End test part"
   */
  val menuCheckList: Seq[HttpCheck] = Seq(
		  css("""div#o_qti_menu li.o_assessmentitem a.o_sel_assessmentitem""","onclick")
        .findAll
        .transform(_.map(onclick => FFXHREvent(onclick)))
        .optional
        .saveAs("qtiItems"),
      css("""div#o_qti_menu li.o_assessmentitem a.o_sel_assessmentitem""","onclick")
        .count
        .saveAs("numOfItems"),
      css("""a.o_sel_end_testpart""", "href")
        .find
        .transform(href => FFEvent(href))
        .optional
        .saveAs("endTestPartButton")
		  )

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
    .check(css("""form input[name=_csrf]""","value")
			.find(0)
			.optional
			.saveAs("csrfToken"))
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
    .check(checkListAssessmentItemForm: _*)
      

  /**
   * Finish the test and save the close button
   *
   * @return The builder
   */
  def endTestPart: ChainBuilder = exec(session => {
      val endTestPartButton = session("endTestPartButton").as[FFEvent]
      val parameters = collection.mutable.HashMap[String, String]()
      parameters.put("dispatchuri", endTestPartButton.elementId)
      parameters.put("dispatchevent", endTestPartButton.actionId)
      val csrfToken = session("csrfToken").as[String]
      parameters.put("_csrf", csrfToken)
      session.set("formParameters", parameters.toMap[String,String])
    }).exec(
      http("End test part")
        .post("""${itemAction}""")
        .formParamMap("""${formParameters}""")
        .headers(headers_json)
        .check(status.is(200))
        .transformResponse(extractJsonResponse)
        .check(css("""div#o_qti_run a.o_sel_close_test""","href")
          .find
          .transform(href => FFEvent(href))
          .optional
          .saveAs("closeTest"))
        .check(css("""div.modal-dialog a[onclick*='link_0']""","onclick")
          .find
          .transform(onclick => XHREvent(onclick))
          .optional
          .saveAs("confirmClose")))

  /**
   * Close the test and save the confirm button
   *
   * @return The builder
   */
  def closeTestConfirm: ChainBuilder = doIf(session => session.contains("closeTest")) {
	  exec(session => {
      val closeButton = session("closeTest").as[FFEvent]
      val parameters = collection.mutable.HashMap[String, String]()
      parameters.put("dispatchuri", closeButton.elementId)
      parameters.put("dispatchevent", closeButton.actionId)
      val csrfToken = session("csrfToken").as[String]
      parameters.put("_csrf", csrfToken)
      session.set("formParameters", parameters.toMap[String,String])
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
    )}

  /**
   * Confirm closing the test AND assert the status info
   * on the course node.
   *
   * @return The builder
   */
  def confirmCloseTest: ChainBuilder = 
      exec(session => {
      val confirmCloseButton = session("confirmClose").as[XHREvent]
      val closeUrl = confirmCloseButton.url();
      session.set("closeUrl", closeUrl)
    }).exec(
      http("Confirm close")
        .post("""${closeUrl}""")
        .formParam("cid","link_0")
        .headers(headers_json)
        .check(status.is(200))
        .transformResponse(extractJsonResponse)
        .check(css("""div.o_statusinfo""")
        		.find
        		.saveAs("statusInfo")
        )
    )
  
  /**
   * Confirm closing the test, close the results AND assert
   * the status info on the course node.
   *
   * @return The builder
   */
  def confirmCloseTestAndCloseResults: ChainBuilder = exec(session => {
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
          .check(css("""div#o_qti_run a.o_sel_close_results""","href")
            .find
            .transform(href => FFEvent(href))
            .saveAs("closeResults"))
    ).exec(session => {
      val closeResultsButton = session("closeResults").as[FFEvent]
      val parameters = collection.mutable.HashMap[String, String]()
      parameters.put("dispatchuri", closeResultsButton.elementId)
      parameters.put("dispatchevent", closeResultsButton.actionId)
      val csrfToken = session("csrfToken").as[String]
      parameters.put("_csrf", csrfToken)
      session.set("formParameters", parameters.toMap[String,String])
    }).exec(
      http("Close results")
        .post("""${itemAction}""")
        .formParamMap("""${formParameters}""")
        .headers(headers_json)
        .check(status.is(200))
        .transformResponse(extractJsonResponse)
        .check(css("""div.o_statusinfo""")
        		.find
        		.saveAs("statusInfo")
        )
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
      val csrfToken = session("csrfToken").as[String]
      if(itemPos < itemList.length) {
        val parameters = itemList(itemPos).formMap(csrfToken)
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
          .check(checkListAssessmentItemForm: _*)
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
      val csrfToken = session("csrfToken").as[String]
      parameters.put("_csrf", csrfToken)
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
          .optional
          .saveAs("toSectionButton"))
        .check(menuCheckList: _*)
    ).exec(session => {
      session
        .removeAll("multipleChoiceNames", "multipleChoiceValues",
          "singleChoiceNames", "singleChoiceValues",
          "kprimNames", "kprimNames", "fillinNames", "textNames")
    })

  def toSection : ChainBuilder = exec(session => {
    val toSectionButton = session("toSectionButton").as[FFXHREvent];
    val csrfToken = session("csrfToken").as[String]
    val parameters = toSectionButton.formMap(csrfToken);
    session.set("formParameters", parameters)
  }).exec(
    http("To section:${itemPos}")
      .post("""${itemAction}""")
      .formParamMap("""${formParameters}""")
      .headers(headers_json)
      .check(status.is(200))
      .transformResponse(extractJsonResponse)
      .check(css("#o_qti_run"))
      .check(menuCheckList: _*)
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
