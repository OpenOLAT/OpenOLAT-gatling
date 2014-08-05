OpenOLAT Gatling Test
=========================

This project is tested with Scala-IDE 3.0.3 and Scala 2.10.4


To set-up add this in .project:

	<buildSpec>
		<buildCommand>
			<name>org.scala-ide.sdt.core.scalabuilder</name>
			<arguments>
			</arguments>
		</buildCommand>
		<buildCommand>
			<name>org.eclipse.m2e.core.maven2Builder</name>
			<arguments>
			</arguments>
		</buildCommand>
	</buildSpec>
	<natures>
		<nature>org.scala-ide.sdt.core.scalanature</nature>
		<nature>org.eclipse.jdt.core.javanature</nature>
		<nature>org.eclipse.m2e.core.maven2Nature</nature>
	</natures> 
	
Then save and refresh your project Your project > Maven > Update Project...



To test it out, simply execute the following command:

    $mvn gatling:execute -Dgatling.simulationClass=frentix.OOSimulation

or simply:

    $mvn gatling:execute
