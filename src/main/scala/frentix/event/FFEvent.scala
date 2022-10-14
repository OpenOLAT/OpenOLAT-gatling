/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package frentix.event

import scala.collection.immutable

object FFEvent {
  def apply(link:String):FFEvent = {
    var start = "o_ffEvent(event,'"
    var startIndex = link.indexOf(start) + start.length()
    var endIndex = link.indexOf("')")
    var cleanedLink = link.substring(startIndex, endIndex)
    var splittedLink: Array[String] = cleanedLink.split(",")
    0 to splittedLink.length - 1 foreach { i =>
        splittedLink(i) = splittedLink(i).stripPrefix("'").stripSuffix("'")
    }
    new FFEvent(splittedLink(0), splittedLink(1), splittedLink(2), splittedLink(3), splittedLink(4))
  }
}

class FFEvent(form:String, uri:String, element:String, eventField:String, action:String) {
  
  private var _formName = form
  private var _formId = form.replace("ofo_", "")
  private var _dispatchUri = uri
  private var _elementId = element
  private var _eventFieldId = eventField
  private var _actionId = action
  
  def formName = _formName
  def formId = _formId
  def dispatchUri = _dispatchUri
  def elementId = _elementId
  def eventFieldId = _eventFieldId
  def actionId = _actionId
  
  def url():String = {
    "/auth/1%3A1%3A" + formId + "%3A1%3A1%3Aofo_%3Afid/"
  }

  def formMap(csrfToken:String) : immutable.Map[String,String] = {
    val parameters = collection.mutable.HashMap[String, String]()
    parameters.put("dispatchuri", elementId)
    parameters.put("dispatchevent", actionId)
    if(csrfToken != null && csrfToken.length() > 0) {
      parameters.put("_csrf", csrfToken)
    }
    parameters.toMap[String,String]
  }
  
  override def toString() = "o_ffEvent[" + formName + ":" + dispatchUri + ":" +
    elementId + ":" + eventFieldId + ":" + actionId + "]"
}