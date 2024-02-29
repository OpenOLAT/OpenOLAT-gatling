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
package org.olat.gatling.event;

import java.util.HashMap;
import java.util.Map;


/**
 * 
 * Initial date: 29 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public record FFXHREvent(String formName,  String formId,
		String dispatchUri, String elementId, String eventFieldId, String actionId,
		String command, String commandValue, String subCommand, String subCommandValue) {
	
	private static final String OFFXHREVENT = "o_ffXHREvent('";
	
	public static final FFXHREvent valueOf(String onclick) {
		if(onclick == null) {
			return null;
		}
		
		int startIndex = onclick.indexOf(OFFXHREVENT);
		int endIndex = onclick.indexOf("');", startIndex);
		String cleanedLink = onclick.substring(startIndex + OFFXHREVENT.length(), endIndex);
		String[] splittedLink = cleanedLink.split(",");
		for(int i=0; i<splittedLink.length; i++) {
			splittedLink[i] = EventHelper.strip(splittedLink[i]);
		}
		
		String optionalSubCommand = null;
		String optionalSubCommandValue = null;
		if(splittedLink.length > 9) {
			optionalSubCommand = splittedLink[10];
			optionalSubCommandValue = splittedLink[11];
		}

		return new FFXHREvent(splittedLink[0], splittedLink[0].replace("ofo_", ""),
				splittedLink[1], splittedLink[2], splittedLink[3], splittedLink[4],
				splittedLink[8], splittedLink[9], optionalSubCommand, optionalSubCommandValue);
	}
	
	public Map<String,String> toMap(String csrfToken) {
		Map<String,String> parameters = new HashMap<>();
		parameters.put("dispatchuri", elementId);
		parameters.put("dispatchevent", actionId);
		if(csrfToken != null && csrfToken.length() > 0) {
			parameters.put("_csrf", csrfToken);
		}
		parameters.put(command, commandValue);
		if(subCommand != null) {
			parameters.put(subCommand, subCommandValue);
		}
		return Map.copyOf(parameters);
	}
}
