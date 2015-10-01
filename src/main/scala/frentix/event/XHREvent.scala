package frentix.event

/**
 * Created by srosse on 29.09.15.
 */
object XHREvent {

  def apply(link:String): XHREvent = {
    val cleanedLink = link.replace("o_XHREvent('", "").replace("'); return false;", "")
    val splittedLink: Array[String] = cleanedLink.split(",")
    new XHREvent(splittedLink(0).replace("'", ""), splittedLink(3).replace("'", ""), splittedLink(4).replace("'", ""))
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

