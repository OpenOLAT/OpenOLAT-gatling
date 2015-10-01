package frentix.event

import scala.collection.mutable
import scala.collection.immutable

/**
 * Created by srosse on 29.09.15.
 */
object FFXHREvent {
  val OFFXHREVENT = "o_ffXHREvent('";

  def apply(link:String):FFXHREvent = {
    val startIndex = link.indexOf(OFFXHREVENT)
    val endIndex = link.indexOf("');", startIndex)
    val cleanedLink = link.substring(startIndex + OFFXHREVENT.length, endIndex).replace("true,true,", "")
    val splittedLink: Array[String] = cleanedLink.split("','")
    if(splittedLink.length == 7) {
      new FFXHREvent(splittedLink(0), splittedLink(1), splittedLink(2), splittedLink(3), splittedLink(4), splittedLink(5), splittedLink(6), null, null)
    } else {
      new FFXHREvent(splittedLink(0), splittedLink(1), splittedLink(2), splittedLink(3), splittedLink(4), splittedLink(5), splittedLink(6), splittedLink(7), splittedLink(8))
    }
  }
}

/**
 * o_ffXHREvent('ofo_1000006914','ofo_1000006914_dispatchuri','o_fi1000006923','ofo_1000006914_eventval','2',true,true,'cid','selectItem','item','id1399ad03-4981-4240-8a39-dfd9857bf0e8_1:2:1');
 * @param form
 * @param uri
 * @param element
 * @param eventField
 * @param action
 */

class FFXHREvent(form:String, uri:String, element:String, eventField:String, action:String,
                 cmd:String, cmdValue:String, subCmd:String, subCmdValue:String) {

  private val _formName = form
  private val _formId = form.replace("ofo_", "")
  private val _dispatchUri = uri
  private val _elementId = element
  private val _eventFieldId = eventField
  private val _actionId = action
  private val _cmd = cmd
  private val _cmdValue = cmdValue
  private val _subCmd = subCmd
  private val _subCmdValue = subCmdValue

  def formName :String = _formName
  def formId : String = _formId
  def dispatchUri : String = _dispatchUri
  def elementId : String = _elementId
  def eventFieldId : String = _eventFieldId
  def actionId : String = _actionId
  def command : String = _cmd
  def commandValue : String = _cmdValue
  def subCommand : String = _subCmd
  def subCommandValue : String = _subCmdValue

  def formMap() : immutable.Map[String,String] = {
    val parameters = collection.mutable.HashMap[String, String]()
    parameters.put("dispatchuri", elementId)
    parameters.put("dispatchevent", actionId)
    parameters.put(command, commandValue)
    if(subCommand != null) {
      parameters.put(subCommand, subCommandValue)
    }
    parameters.toMap[String,String]
  }

}
