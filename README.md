OpenOLAT Gatling Test
=========================


Setup the project with Eclipse
------------------------------

This project is tested with Scala-IDE 4.4.1 and Scala 2.11.8

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

Setup the project with IDEA
---------------------------

I use the Plugin for Scala and Scala 2.11.8


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

Check that the setting "server.legacy.context" is coherent with your setup.

If you start OpenOLAT in Eclipse or an other IDE, don't forget to limit the 
output in the console as it can kill the performance.

Launch a test
-------------

Compile the code with your preferred IDE or with the following command:

	$mvn compile

To test it out, simply execute the following command:

    $mvn gatling:execute -Dgatling.simulationClass=frentix.OOSimulation

or simply:

    $mvn gatling:execute

With all options:

    $mvn gatling:execute -Dusers=100 -Dramp=50 -Durl=http://localhost:8080 -Dgatling.simulationClass=frentix.OOSimulation
    
    $mvn gatling:execute -Dusers=500 -Dthinks=10 -Dramp=50 -Durl=http://localhost:8081 -Dgatling.simulationClass=frentix.QTI21Simulation

Where users are the number of users, the ramp is in seconds and the url is... the url of OpenOLAT


OS Optimization for Mac (Maverick and not Yosemite)
---------------------------------------------------
Source: gatling.io

sudo sysctl -w kern.maxfilesperproc=300000
sudo sysctl -w kern.maxfiles=300000
sudo sysctl -w net.inet.ip.portrange.first=1024

My settings before as backup:
kern.maxfilesperproc: 10240
kern.maxfiles: 12288
net.inet.ip.portrange.first: 49152