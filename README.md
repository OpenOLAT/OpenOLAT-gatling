# OpenOLAT Gatling Test


## Setup the project with Eclipse

This project is tested with Scala-IDE 4.7.0 and Scala 2.12.3

To finish the set-up, add this in .project:

```bash
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
```
	
Then save and refresh your project Your project > Maven > Update Project...

## Setup the project with IDEA

I use the Plugin for Scala and Scala 2.12.3

## Before launching a test

You need to adapt your OpenOLAT instance to the Gatling tests. Remove the landing page
settings and set these settings in your olat.local.properties

```
history.back.enabled=false
history.resume.enabled=false
registration.enableDisclaimer=false
```

Disable the debug settings

```
olat.debug=false
localization.cache=true
```

Disable the SMS feature for password recovery

Check that the setting "server.legacy.context" is coherent with your setup.

If you start OpenOLAT in Eclipse or an other IDE, don't forget to limit the 
output in the console as it can kill the performance.

## Launch a test

Compile the code with your preferred IDE or with the following command:

```bash
$mvn compile
```

To test it out, simply execute the following command:

```bash
$mvn gatling:execute -Dgatling.simulationClass=frentix.OOSimulation
```

or simply:

```bash
$mvn gatling:execute
```

With all options:

```bash
$mvn gatling:execute -Dusers=100 -Dramp=50 -Durl=http://localhost:8080 -Dgatling.simulationClass=frentix.OOSimulation
```

```bash
$mvn gatling:execute -Dusers=500 -Dthinks=10 -Dramp=50 -Durl=http://localhost:8081 -Dgatling.simulationClass=frentix.QTI21Simulation
```

Where users are the number of users, the ramp is in seconds and the url is... the url of OpenOLAT

## Some results

The tests were done  on MacBook Pro 2015 (4 cores), PostgreSQL, without Apache

The test is the UIBK like test:
- login, open a course or the details page and return to "My courses" 5 five times, logout.
- 2000 users ramp up in 50 seconds
- 5 seconds between opening courses
- 50 seconds before logout

1. Standard configuration for OpenOLAT (chat, rating, comments)
..* Version: 10.0.2, 133 queries/s, 1% errors (timeout)

2. OpenOLAT without rating and comments but with chat
..* Version: 10.0.0, 180 queries/s, 0% errors (but there is some errors)
   
3. OpenOLAT without rating, comments and chat
..* Version: 10.0.0, 187 queries/s, 1% errors

--------------------------------------------------

The test is the UIBK like test as above:
- login, open a course or the details page and return to "My courses" 5 five times, logout.
- 4000 users ramp up in 50 seconds
- 1 seconds between opening courses
- 50 seconds before logout

1. Standard configuration for OpenOLAT (chat, rating, comments)
- Version: 10.4b, 315 queries/s, some errors due to users without courses, 99% < 7s
   

2015-11-18: Standard configuration for OpenOLAT (chat, rating, comments), UIBK like
- Version: 10.4b, 278 queries/s, some errors due to users without courses, 99% < 11s
   
2016-02-01: Standard configuration for OpenOLAT (chat, rating, comments), UIBK like
- Version: 10.5++, 388 queries/s, some errors due to users without courses, 99% < 8.1s
 
2017-09-21: Configuration for OpenOLAT: chat, rating, comment, lectures, assessment modes, but no portfolio v1, UIBK like
- Version: 12.1, 233 queries/s, some errors due to users without courses, 99% < 31s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
- With 6000 users: 266 queries/s, some errors due to users without courses, 99% < 43s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
   
2018-06-18: change to ramp from an exponential (discourage by gatling team) to a more classic one

2018-09-01: New hardware for testing MacBook Pro 2018 (hexacore, 2.9Ghz)
2018-09-27: Configuration for OpenOLAT: chat, rating, comment, lectures, assessment modes, but no portfolio v1, UIBK like
- Version: 13.0a,  283 queries/s, some errors due to users without courses, avg. <  6.5s, 99% < 14.0s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
- Adoptopenjdk  ,  260 queries/s, some errors due to users without courses, avg. <  7.5s, 99% < 15.5s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
- Adoptopenjdk11,  270 queries/s, some errors due to users without courses, avg. <  6.8s, 99% < 14.0s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
- With 6000 users: 265 queries/s, some errors due to users without courses, avg. < 12.5s, 99% < 26.0s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
- Adoptopenjdk11,  270 queries/s, some errors due to users without courses, avg. < 13.8s, 99% < 28.0s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
- With 8000 users: 276 queries/s, some errors due to users without courses, avg. < 21.0s, 99% < 49.0s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
- Adoptopenjdk11,  282 queries/s, some errors due to users without courses, avg. < 20.0s, 99% < 41.0s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads


