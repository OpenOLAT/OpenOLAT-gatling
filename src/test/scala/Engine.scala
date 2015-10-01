import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder

object Engine extends App {

	val props = new GatlingPropertiesBuilder
	props.dataDirectory(IDEPathHelper.dataDirectory.toString)
	props.resultsDirectory(IDEPathHelper.resultsDirectory.toString)
	props.bodiesDirectory(IDEPathHelper.bodiesDirectory.toString)
	props.binariesDirectory(IDEPathHelper.mavenBinariesDirectory.toString)

	props.runDescription("QTI 1.2 simulation") // do not ask for a descr. upon run
	props.simulationClass("frentix.QTI12Simulation") // do not ask for a simulation to run upon run

	Gatling.fromMap(props.build)
}