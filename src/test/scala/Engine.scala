import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder

object Engine extends App {

	val props = new GatlingPropertiesBuilder
	props.dataDirectory(IDEPathHelper.dataDirectory.toString)
	props.resultsDirectory(IDEPathHelper.resultsDirectory.toString)
	props.requestBodiesDirectory(IDEPathHelper.requestBodiesDirectory.toString)
	props.binariesDirectory(IDEPathHelper.mavenBinariesDirectory.toString)

	props.runDescription("N/A") // do not ask for a descr. upon run
	props.simulationClass("frentix.OOSimulation") // do not ask for a simulation to run upon run

	Gatling.fromMap(props.build)
}