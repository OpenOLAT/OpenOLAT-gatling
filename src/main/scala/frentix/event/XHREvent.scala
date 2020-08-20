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

/**
 * Created by srosse on 29.09.15.
 */
object XHREvent {
  
  val OXHREVENT = "o_XHREvent('";

  def apply(link:String): XHREvent = {
    val startIndex = link.indexOf(OXHREVENT)
    val endIndex = link.indexOf("');", startIndex)
    val cleanedLink = link.substring(startIndex + OXHREVENT.length, endIndex).replace("'", "")
    val splittedLink: Array[String] = cleanedLink.split(",")
    new XHREvent(splittedLink(0), splittedLink(3), splittedLink(4))
  }
}

class XHREvent(uri:String, cmd:String, cmdValue:String) {

  private val _dispatchUri = uri
  private val _cmd = cmd
  private val _cmdValue = cmdValue

  def dispatchUri = _dispatchUri
  def command = _cmd
  def commandValue = _cmdValue

  def url():String = {
    dispatchUri
  }

}

