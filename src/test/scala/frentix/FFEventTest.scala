package frentix
import org.junit._
import Assert._

@Test
class FFEventTest {
  
    @Test
    def testOK() = {
      
      var link = "javascript:o_ffEvent('ofo_8000008724','ofo_8000008724_dispatchuri','o_fi8000008781','ofo_8000008724_eventval','2')";
      var ffEvent = FFEvent(link);

      assertNotNull(ffEvent.url)
      assertEquals("ofo_8000008724", ffEvent.formName)
      assertEquals("ofo_8000008724_dispatchuri", ffEvent.dispatchUri)
      assertEquals("o_fi8000008781", ffEvent.elementId)
      assertEquals("ofo_8000008724_eventval", ffEvent.eventFieldId)
      assertEquals("2", ffEvent.actionId)
     
    }

}