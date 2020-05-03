import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder

object Engine extends App {

	val props = new GatlingPropertiesBuilder
	props.resultsDirectory(IDEPathHelper.resultsDirectory.toString)
	props.binariesDirectory(IDEPathHelper.mavenBinariesDirectory.toString)

	props.runDescription("OO simulation") // do not ask for a descr. upon run
	//props.simulationClass("frentix.uibk.UIBKSTSimulation")
  //props.simulationClass("frentix.QTI12Simulation")
	//props.simulationClass("frentix.QTI21Simulation")
  props.simulationClass("frentix.OOSimulation")

	Gatling.fromMap(props.build)
}