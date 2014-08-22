OpenOLAT Gatling Test
=========================


Setup the project
-----------------

This project is tested with Scala-IDE 3.0.3 and Scala 2.10.4

To finish the set-up, add this in .project:

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


Before launching a test
-----------------------

You need to adapt your OpenOLAT instance to the Gatling tests. Remove the landing page
settings and set these settings in your olat.local.properties

history.back.enabled=false
history.resume.enabled=false
registration.enableDisclaimer=false

Disable the debug settings

olat.debug=false
localization.cache=true

Check that this setting "server.legacy.context" is coherent with your setup.

If you start OpenOLAT in Eclipse or an other IDE, don't forget to limit the 
output in the console as it can kill the performance.

Launch a test
-------------

To test it out, simply execute the following command:

    $mvn gatling:execute -Dgatling.simulationClass=frentix.OOSimulation

or simply:

    $mvn gatling:execute


OS Optimization for Mac
-----------------------
Source: gatling.io

sudo sysctl -w kern.maxfilesperproc=300000
sudo sysctl -w kern.maxfiles=300000
sudo sysctl -w net.inet.ip.portrange.first=1024

My settings before as backup:
kern.maxfilesperproc: 10240
kern.maxfiles: 12288
net.inet.ip.portrange.first: 49152