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
public record XHREvent(String onclick, String uri, String cmd, String cmdValue)  {

	public static final String OXHREVENT = "o_XHREvent('";
	
	public static final XHREvent valueOf(String onclick, String place) {
		if(onclick == null || place == null) {
			return null;
		}
		
		int startIndex = onclick.indexOf(OXHREVENT);
		int endIndex = onclick.indexOf("');", startIndex);
		String cleanedLink = onclick.substring(startIndex + OXHREVENT.length(), endIndex).replace("'", "");
		String[] splittedLink = cleanedLink.split(","); 
		return new XHREvent(onclick, splittedLink[0], splittedLink[3], splittedLink[4]);
	}
	
	public String url() {
		return uri;
	}
}
