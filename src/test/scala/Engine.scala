import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder

object Engine extends App {

	val props = new GatlingPropertiesBuilder
	props.dataDirectory(IDEPathHelper.dataDirectory.toString)
	props.resultsDirectory(IDEPathHelper.resultsDirectory.toString)
	props.bodiesDirectory(IDEPathHelper.bodiesDirectory.toString)
	props.binariesDirectory(IDEPathHelper.mavenBinariesDirectory.toString)

	props.runDescription("OO simulation") // do not ask for a descr. upon run
	//props.simulationClass("frentix.uibk.UIBKSTSimulation")
  //props.simulationClass("frentix.QTI12Simulation")
  props.simulationClass("frentix.OOSimulation")

	Gatling.fromMap(props.build)
}