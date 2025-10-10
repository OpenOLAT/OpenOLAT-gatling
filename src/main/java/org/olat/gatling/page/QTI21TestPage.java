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
package org.olat.gatling.page;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.olat.gatling.event.FFEvent;
import org.olat.gatling.event.FFXHREvent;
import org.olat.gatling.event.NameMultipleValues;
import org.olat.gatling.event.XHREvent;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.CheckBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

/**
 * 
 * Initial date: 29 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21TestPage extends HttpHeaders {
	
	private static final Random random = new Random();
	private static final String PRESENTED = "qtiworks_presented_";
	
	public static final List<CheckBuilder> checkListAssessmentItemForm  = List.of(
		css("div#itemBody input[type=\'hidden\'][name^=\'qtiworks_presented_\']", "name")
			.find(0)
			.saveAs("formPresentedHidden"),
		css("div.qtiworks.o_assessmentitem.o_assessmenttest button.btn-primary", "onclick")
			.find()
			.transform(FFEvent::valueOf)
			.saveAs("submitItem"),
		css("div#itemBody tr.choiceinteraction td.control input[type=\'radio\']", "name")
			.findAll()
			.optional()
			.saveAs("singleChoiceNames"),
		css("div#itemBody tr.choiceinteraction td.control input[type=\'radio\']", "value")
			.findAll()
			.optional()
			.saveAs("singleChoiceValues"),
		//multiple choices
		css("div#itemBody tr.choiceinteraction td.control input[type=\'checkbox\']", "name")
			.findAll()
			.optional()
			.saveAs("multipleChoiceNames"),
		css("div#itemBody tr.choiceinteraction td.control input[type=\'checkbox\']", "value")
			.findAll()
			.optional()
			.saveAs("multipleChoiceValues"),
		//KPrims
		css("div#itemBody table.o_qti_item_kprim td.o_qti_item_kprim_input input[type=\'checkbox\']", "name")
			.findAll()
			.optional()
			.saveAs("kprimNames"),
		css("div#itemBody table.o_qti_item_kprim td.o_qti_item_kprim_input input[type=\'checkbox\']", "value")
			.findAll()
			.optional()
			.saveAs("kprimValues"),
		//fillin
		css("div#itemBody span.textEntryInteraction input[type=\'text\']", "name")
			.findAll()
			.optional()
			.saveAs("fillinNames"),
		css("div#itemBody span.textEntryInteraction input[type=\'text\']", "class")
			.findAll()
			.optional()
			.saveAs("fillinTypes"),	
		//text
		css("div#itemBody div.extendedTextInteraction textarea", "name")
			.findAll()
			.optional()
			.saveAs("textNames"),
		//hotspot
		css("div#itemBody div.hotspotInteraction map area", "data-qti-id")
			.findAll()
			.optional()
			.saveAs("hotspotAreas")	
			
	);
	
	public static final List<CheckBuilder> menuCheckList = List.of(
		css("div#o_qti_menu li.o_assessmentitem a.o_sel_assessmentitem", "onclick")
			.findAll()
			.transform(list -> {
				return list.stream().map(FFXHREvent::valueOf).toList();
			})
			.optional()
			.saveAs("qtiItems"),
		css("div#o_qti_menu li.o_assessmentitem a.o_sel_assessmentitem", "onclick")
			.count()
			.saveAs("numOfItems"),
		css("a.o_sel_end_testpart", "onclick")
			.find()
			.transform(FFEvent::dirtyValueOf)
			.optional()
			.saveAs("endTestPartButton")
	);
	
	public static final String randomWord() {
		int rnd = random.nextInt(10);
		return switch(rnd) {
			case 0 -> "Test";
			case 1 -> "Shark";
			case 2 -> "961 bis 964 BGB";
			case 3 -> "тест"; //Test (russian)
			case 4 -> "您好"; //Hi (chinese)
			case 5 -> "綾波レイ"; //Ayanami Rei (japanese)
			case 6 -> "Lorem ipsum";
			case 7 -> "C11++";
			case 8 -> "PostgreSQL";
			case 9 -> "not";
			default -> "test";
		};
	}
	
	public static final String randomText() {
		int rnd = random.nextInt(2);
		return switch(rnd) {
			case 0 -> "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce at dui sagittis, sagittis felis sed, consequat elit. Duis efficitur sagittis magna ac lacinia. Nunc molestie metus nec leo molestie ornare. Vestibulum scelerisque lacus consequat, cursus sapien et, rhoncus sapien. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus.";
			case 1 -> "A learning management system (LMS) is a software application for the administration, documentation, tracking, reporting and delivery of electronic educational technology (also called e-learning) education courses or training programs.[1] LMSs range from systems for managing training and educational records to software for distributing online or blended/hybrid college courses over the Internet with features for online collaboration. Colleges and universities use LMSs to deliver online courses and augment on-campus courses. Corporate training departments use LMSs to deliver online training, as well as to automate record-keeping and employee registration.";
			default -> "Some little text";
		};
	}
	
	public static final HttpRequestActionBuilder loginPassword = LoginPage
		.loginPasswordOptional
		.check(css("a.o_sel_start_qti21assessment", "onclick")
			      .find()
			      .transform(onclick -> XHREvent.valueOf(onclick, "Start test"))
			      .saveAs("startTest"));
	
	public static final ChainBuilder startTest  = exec(session -> {
		XHREvent startUrl = (XHREvent)session.get("startTest");
	    return session
	    	.set("startUrl", startUrl.url())
	    	.set("itemPos", Integer.valueOf(0));
	});
	
	public static final HttpRequestActionBuilder startWithItems = http("Start test")
		.post("#{startUrl}")
	    /*
	    .resources(
	      http("QtiWorksRendering.js").get("/raw/_noversion_/assessment/rendering/javascript/QtiWorksRendering.js"),
	      http("AsciiMathInputController.js").get("/raw/_noversion_/assessment/rendering/javascript/AsciiMathInputController.js"),
	      http("UpConversionAjaxController.js").get("/raw/_noversion_/assessment/rendering/javascript/UpConversionAjaxController.js"),
	      http("Custion QTI UI.js").get("/raw/_noversion_/js/jquery/ui/jquery-ui-1.11.4.custom.qti.min.js"),
	      http("Maphilight.js").get("/raw/_noversion_/js/jquery/maphilight/jquery.maphilight.js"),
	      http("Paint.js").get("/raw/_noversion_/js/jquery/openolat/jquery.paint.js"),
	      http("Taboverride.js").get("/raw/_noversion_/js/jquery/taboverride/taboverride-4.0.0.min.js"),
	      http("Dragula.js").get("/raw/_noversion_/js/dragula/dragula.min.js"),
	      http("Taboverride jQuery").get("/raw/_noversion_/js/jquery/taboverride/jquery.taboverride.min.js"),
	      http("Qti.js").get("/raw/_noversion_/js/jquery/qti/jquery.qti.min.js"),
	      http("Qti Timer.js").get("/raw/_noversion_/js/jquery/qti/jquery.qtiTimer.js"),
	      http("Qti Autosave.js").get("/raw/_noversion_/js/jquery/qti/jquery.qtiAutosave.js")
	    )*/
		.formParam("cid","start")
		.headers(headersJson)
		.transformResponse(extractJsonResponse)
		.check(status().is(200))
		.check(css("#o_qti_run"))
		.check(css("div#o_qti_menu li.o_assessmentitem a.o_sel_assessmentitem", "onclick")
			.findAll()
			.transform(list -> {
				return list.stream().map(FFXHREvent::valueOf).toList();
			})
			.optional()
			.saveAs("qtiItems"))
		.check(css("div#o_qti_menu li.o_assessmentitem a.o_sel_assessmentitem", "onclick")
			.count()
			.saveAs("numOfItems"))
		.check(css("div#o_qti_container form", "action")
			.find()
			.saveAs("itemAction"))
		.check(checkListAssessmentItemForm);
	
	public static final ChainBuilder startItem = exec(session -> {
			int itemPos = session.getInt("itemPos");
			List<FFXHREvent> itemList = session.getList("qtiItems");
			String csrfToken = csrfToken(session);
			if(itemPos < itemList.size()) {
				Map<String,String> parameters = itemList.get(itemPos).toMap(csrfToken);
				return session.set("itemParameters", parameters);
			}
			return session.remove("itemParameters");
	    }).doIf(session -> session.contains("itemParameters")).then(
	    	exec(http("Select item:#{itemPos}")
	    		.post("#{itemAction}")
	    		.formParamMap(session -> session.getMap("itemParameters"))
	    		.headers(headersJson)
	    		.transformResponse(extractJsonResponse)
	    		.check(status().is(200))
	    		.check(css("div.qtiworks.o_assessmentitem.o_assessmenttest"))
	    		.check(checkListAssessmentItemForm)
	    	)
	    );
	
	public static final ChainBuilder postItem = exec(session -> {
			Map<String,Object> parameters = new HashMap<>();
			String formPresentedHidden = session.getString("formPresentedHidden");
			if(formPresentedHidden != null) {
				parameters.put(formPresentedHidden, "1");
			}
			NameMultipleValues multivaluedParameters = new NameMultipleValues();
			if(session.contains("singleChoiceNames")) {
				List<String> singleChoiceNames = session.getList("singleChoiceNames");
				List<String> singleChoiceValues = session.getList("singleChoiceValues");
				int i = random.nextInt(singleChoiceNames.size());
				parameters.put(singleChoiceNames.get(i), singleChoiceValues.get(i));
			} else if(session.contains("multipleChoiceNames")) {
				List<String> names = session.getList("multipleChoiceNames");
				List<String> values = session.getList("multipleChoiceValues");
				transformMultipleChoices(multivaluedParameters, names, values);
			} else if(session.contains("kprimNames")) {
				List<String> names = session.getList("kprimNames");
				List<String> values = session.getList("kprimValues");
				transformKprimes(multivaluedParameters, names, values);
			} else if(session.contains("fillinNames")) {
				List<String> names = session.getList("fillinNames");
				List<String> types = session.getList("fillinTypes");
				transformTextEntries(parameters, names, types);
			} else if(session.contains("textNames")) {
				List<String> names = session.getList("textNames");
				parameters.put(names.get(0), randomText());
			} else if(session.contains("hotspotAreas")) {
				List<String> areas = session.getList("hotspotAreas");
				transformHotspotEntries(parameters, formPresentedHidden, areas);
			}
			//submit
			FFEvent submitItem = (FFEvent)session.get("submitItem");
			parameters.put("dispatchuri", submitItem.element());
			parameters.put("dispatchevent", submitItem.action());
			parameters.put("_csrf", csrfToken(session));

			return session
					.set("formParameterMulti", multivaluedParameters)
					.set("formParameters", Map.copyOf(parameters));
		}).exec(
			http("Post item:#{itemPos}")
				.post("#{itemAction}")
				.formParamMap(session -> session.getMap("formParameters"))
				.multivaluedFormParam(session -> {
					NameMultipleValues multiValues = (NameMultipleValues)session.get("formParameterMulti");
					return multiValues == null || multiValues.getName() == null
							? "ooo-empty-name-ooo" : multiValues.getName();	
				}, session -> {
					NameMultipleValues multiValues = (NameMultipleValues)session.get("formParameterMulti");
					return multiValues == null || multiValues.getName() == null || multiValues.getValues() == null
							? List.of() : multiValues.getValues();
				})
				.headers(headersJson)
				.check(status().is(200))
				.transformResponse(extractJsonResponse)
				.check(css("button.o_sel_question_menu", "onclick")
				.find()
				.transform(FFXHREvent::valueOf)
				.optional()
				.saveAs("toSectionButton"))
	        .check(menuCheckList)
	    ).exec(session -> {
	    	return session.removeAll("multipleChoiceNames", "multipleChoiceValues",
	    			"singleChoiceNames", "singleChoiceValues",
	    			"kprimNames", "kprimValues",
	    			"fillinNames", "fillinTypes",
	    			"textNames");
	    });

	private static void transformHotspotEntries(Map<String,Object> parameters, String formPresented, List<String> areas) {
		if(!formPresented.startsWith(PRESENTED)) return;
		
		String responseId = formPresented.substring(PRESENTED.length());
		int index = random.nextInt(areas.size());
		String response = areas.get(0);
		if(index >= 0 && index < areas.size()) {
			response = areas.get(index);
		}
		parameters.put("qtiworks_response_" + responseId, response);
	}
	
	private static void transformTextEntries(Map<String,Object> parameters, List<String> names, List<String> types) {
		if(names.isEmpty() || types.isEmpty()) return;
		
		int numOfEntries = names.size();
		int numOfTypes = types.size();
		for(int i=0; i<numOfEntries && i<numOfTypes; i++) {
			String name = names.get(i);
			String type = types.get(i);
			if("text".equals(type)) {
				parameters.put(name, randomWord());
			} else if("numeric".equals(type)) {
				int val = random.nextInt(42);
				parameters.put(name, Integer.toString(val));
			}
		}
	}
	
	private static void transformMultipleChoices(NameMultipleValues parameters, List<String> names, List<String> values) {
		if(names.isEmpty() || values.isEmpty()) return;
		parameters.setName(names.get(0));
		parameters.setValues(List.copyOf(values));
	}
	
	private static void transformKprimes(NameMultipleValues parameters, List<String> names, List<String> values) {
		if(names.isEmpty() || values.isEmpty()) return;
		
		parameters.setName(names.get(0));
		Set<String> neutralValues = new HashSet<>();
		for(String value:values) {
			int index = value.indexOf(' ');
			if(index >= 0) {
				String val = value.substring(0, index);
				neutralValues.add(val);
			}
		}
		List<Object> responses = new ArrayList<>();
		for(String neutralValue:neutralValues) {
			String responseSuffix = random.nextBoolean() ? "correct" : "wrong";
			responses.add(neutralValue + " " + responseSuffix);
		}
		parameters.setValues(responses);	
	}
	
	public static final ChainBuilder endTestPart = exec(session -> {
			FFEvent endTestPartButton = (FFEvent)session.get("endTestPartButton");
			Map<String,String> parameters = new HashMap<>();
			parameters.put("dispatchuri", endTestPartButton.element());
			parameters.put("dispatchevent", endTestPartButton.action());
			parameters.put("_csrf", csrfToken(session));
			return session.set("formParameters", Map.copyOf(parameters));
		}).exec(http("End test part")
			.post("#{itemAction}")
			.formParamMap(session -> session.getMap("formParameters"))
			.headers(headersJson)
			.check(status().is(200))
			.transformResponse(extractJsonResponse)
			.check(css("div#o_qti_run a.o_sel_close_test","href")
				.find()
				.transform(FFEvent::valueOf)
				.optional()
				.saveAs("closeTest"))
			.check(css("div.modal-dialog a[onclick*='link_0']","onclick")
				.find()
				.transform(onclick -> XHREvent.valueOf(onclick, "End test part"))
				.optional()
				.saveAs("confirmClose")
		)
	);
	
	public static final ChainBuilder closeTestConfirm = doIf(session -> session.contains("closeTest")).then(
		exec(session -> {
				FFEvent closeButton = (FFEvent)session.get("closeTest");
				Map<String,String> parameters = new HashMap<>();
				parameters.put("dispatchuri", closeButton.element());
				parameters.put("dispatchevent", closeButton.action());
				parameters.put("_csrf", csrfToken(session));
				return session.set("formParameters", Map.copyOf(parameters));
	    }).exec(http("End test and confirm")
	    	.post("#{itemAction}")
	        .formParamMap(session -> session.getMap("formParameters"))
	        .headers(headersJson)
	        .check(status().is(200))
	        .transformResponse(extractJsonResponse)
	        .check(css("div.modal-dialog a[onclick*='link_0']", "onclick")
	          .find()
	          .transform(onclick -> XHREvent.valueOf(onclick, "Close test confirm"))
	          .saveAs("confirmClose"))
	   ));
	
	public static final ChainBuilder confirmCloseTestAndCloseResults = exec(session -> {
			XHREvent confirmCloseButton = (XHREvent)session.get("confirmClose");
			String closeUrl = confirmCloseButton.url();
			return session.set("closeUrl", closeUrl);
		}).exec(http("Confirm close")
			.post("#{closeUrl}")
			.formParam("cid","link_0")
			.headers(headersJson)
			.check(status().is(200))
			.transformResponse(extractJsonResponse)
			.check(css("div#o_qti_container a.o_sel_close_results", "onclick")
				.find(0)
				.transform(FFEvent::valueOf)
				.saveAs("closeResults"))
		).exec(session -> {
			FFEvent closeResultsButton = (FFEvent)session.get("closeResults");
			Map<String,String> parameters = new HashMap<>();
			parameters.put("dispatchuri", closeResultsButton.element());
			parameters.put("dispatchevent", closeResultsButton.action());
			parameters.put("_csrf", csrfToken(session));
			return session.set("formParameters", Map.copyOf(parameters));
	    }).exec(http("Close results")
	    	.post("#{itemAction}")
	    	.formParamMap(session -> session.getMap("formParameters"))
	        .headers(headersJson)
	        .check(status().is(200))
	        .transformResponse(extractJsonResponse)
	        .check(css("div.o_course_iq fieldset.o_start_info_box div.o_note")
	        		.find()
	        		.saveAs("statusInfo")
	        )
	    );
}
