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

/**
 * 
 * Initial date: 29 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public record FFEvent(String onclick, String formName, String formId, String uri, String element, String eventField, String action) {
	
	private static final String start = "o_ffEvent(event,'";
	
	public static final FFEvent valueOf(String onclick) {
		if(onclick == null) {
			return null;
		}

		int startIndex = onclick.indexOf(start) + start.length();
	    int endIndex = onclick.indexOf("')");
	    String cleanedLink = onclick.substring(startIndex, endIndex);
	    String[] splittedLink = cleanedLink.split(",");
	    for(int i=0; i<splittedLink.length; i++) {
	    	splittedLink[i] = EventHelper.strip(splittedLink[i]);
	    }

		return new FFEvent(onclick, splittedLink[0], splittedLink[0].replace("ofo_", ""),
				splittedLink[1], splittedLink[2], splittedLink[3], splittedLink[4]);
	}
	
	public String url() {
		return "/auth/1%3A1%3A" + formId + "%3A1%3A1%3Aofo_%3Afid/";
	}
}
