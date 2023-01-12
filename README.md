# OpenOlat Gatling Test

The tests are compatible with OpenOlat 15.3 and 15.4. They use Scala 2.13 and Java 11.

## Setup the project with IDEA

I use the Plugin for Scala and Maven.

## Before launching a test

You need to adapt your OpenOLAT instance to the Gatling tests. Remove the landing page
settings and set these settings in your olat.local.properties

```
history.back.enabled=false
history.resume.enabled=false
registration.enableDisclaimer=false
```

Disable the CSRF protection.

Disable the debug settings:

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
$mvn gatling:test -Dgatling.simulationClass=frentix.OOSimulation
```

or simply:

```bash
$mvn gatling:execute
```

With all options:

```bash
$mvn gatling:test -Dusers=100 -Dramp=60 -Durl=http://localhost:8080 -Dgatling.simulationClass=frentix.OOSimulation
```

```bash
$mvn gatling:test -Dusers=500 -Dthinks=10 -Dramp=60 -Durl=http://localhost:8081 -Dgatling.simulationClass=frentix.QTI21Simulation
```

Where users are the number of users, the ramp is in seconds and the url is... the url of OpenOLAT

## Some results

The tests were done  on MacBook Pro 2015 (4 cores), PostgreSQL, without Apache

The test is the UIBK like test:
- login, open a course or the details page and return to "My courses" 5 five times, logout.
- 2000 users ramp up in 50 seconds
- 5 seconds between opening courses
- 50 seconds before logout

1. Standard configuration for OpenOlat (chat, rating, comments)
..* Version: 10.0.2, 133 queries/s, 1% errors (timeout)

2. OpenOlat without rating and comments but with chat
..* Version: 10.0.0, 180 queries/s, 0% errors (but there is some errors)
   
3. OpenOlat without rating, comments and chat
..* Version: 10.0.0, 187 queries/s, 1% errors

--------------------------------------------------

The test is the UIBK like test as above:
- login, open a course or the details page and return to "My courses" 5 five times, logout.
- 4000 users ramp up in 50 seconds
- 1 seconds between opening courses
- 50 seconds before logout

1. Standard configuration for OpenOlat (chat, rating, comments)
- Version: 10.4b, 315 queries/s, some errors due to users without courses, 99% < 7s
   

2015-11-18: Standard configuration for OpenOlat (chat, rating, comments), UIBK like
- Version: 10.4b, 278 queries/s, some errors due to users without courses, 99% < 11s
   
2016-02-01: Standard configuration for OpenOlat (chat, rating, comments), UIBK like
- Version: 10.5++, 388 queries/s, some errors due to users without courses, 99% < 8.1s
 
2017-09-21: Configuration for OpenOlat: chat, rating, comment, lectures, assessment modes, but no portfolio v1, UIBK like
- Version: 12.1, 233 queries/s, some errors due to users without courses, 99% < 31s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
- With 6000 users: 266 queries/s, some errors due to users without courses, 99% < 43s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
   
2018-06-18: change to ramp from an exponential (discourage by gatling team) to a more classic one

2018-09-01: New hardware for testing MacBook Pro 2018 (hexacore, 2.9Ghz)

2018-09-27: Configuration for OpenOlat: chat, rating, comment, lectures, assessment modes, but no portfolio v1, UIBK like
- Version: 13.0a,  283 queries/s, some errors due to users without courses, avg. <  6.5s, 99% < 14.0s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
- Adoptopenjdk  ,  260 queries/s, some errors due to users without courses, avg. <  7.5s, 99% < 15.5s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
- Adoptopenjdk11,  270 queries/s, some errors due to users without courses, avg. <  6.8s, 99% < 14.0s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
- With 6000 users: 265 queries/s, some errors due to users without courses, avg. < 12.5s, 99% < 26.0s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
- Adoptopenjdk11,  270 queries/s, some errors due to users without courses, avg. < 13.8s, 99% < 28.0s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
- With 8000 users: 276 queries/s, some errors due to users without courses, avg. < 21.0s, 99% < 49.0s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
- Adoptopenjdk11,  282 queries/s, some errors due to users without courses, avg. < 20.0s, 99% < 41.0s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads

2019-12-23: Configuration for OpenOlat (14.2): chat, rating, comment, lectures, assessment modes, but no portfolio v1, UIBK like
- With 4000 users: 257 queries/s, some errors due to courses with glossaries, avg. < 7.6s, 99% < 18.0s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
- With 6000 users: 282 queries/s, some errors due to courses with glossaries, avg. < 13.7s, 99% < 33.0s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads
- With 8000 users: 271 queries/s, some errors due to courses with glossaries, connection timeout (<50), avg. < 21.6s, 99% < 55.0s, ramp in 60s, thinks 5s (logout too), 90 db connections, NIO2 with 84 threads

2020-05-01: Configuration for OpenOlat (14.2.9): chat, rating, comment, lectures, assessment modes, but no portfolio v1, UIBK like
- With 4000 users: 312 queries/s, some errors due to courses with glossaries, avg. < 4.7s, 99% < 13.6s, ramp in 60s, thinks 5s (logout too), 96 db connections, NIO2 with 84 threads
- With 6000 users: 333 queries/s, some errors due to courses with glossaries, avg. < 10.8s, 99% < 30.0s, ramp in 60s, thinks 5s (logout too), 96 db connections, NIO2 with 84 threads
- With 8000 users: 363 queries/s, some errors due to courses with glossaries, avg. < 14.5s, 99% < 37.3s, ramp in 60s, thinks 5s (logout too), 96 db connections, NIO2 with 84 threads

2020-05-01: Configuration for OpenOlat (14.2.9): chat, rating, comment, lectures, assessment modes, but no portfolio v1, UIBK like, with Hibernate 5.4.15, socket optimisation
- With 4000 users: 336 queries/s, some errors due to courses with glossaries, avg. < 4.0s, 99% < 11.2s, ramp in 60s, thinks 5s (logout too), 96 db connections, NIO2 with 84 threads
- With 6000 users: 371 queries/s, some errors due to courses with glossaries, avg. < 8.8s, 99% < 21.4s, ramp in 60s, thinks 5s (logout too), 96 db connections, NIO2 with 84 threads
- With 8000 users: 366 queries/s, some errors due to courses with glossaries, avg. < 14.3s, 99% < 36.9s, ramp in 60s, thinks 5s (logout too), 96 db connections, NIO2 with 84 threads

2020-05-01: Configuration for OpenOlat (15.pre.9): chat, rating, comment, lectures, assessment modes, UIBK like
- With 4000 users: 336 queries/s, some errors due to courses with glossaries, avg. < 3.8s, 99% < 12.7s, ramp in 60s, thinks 5s (logout too), 96 db connections, NIO2 with 84 threads
- With 6000 users: 371 queries/s, some errors due to courses with glossaries, avg. < 8.9s, 99% < 25.8s, ramp in 60s, thinks 5s (logout too), 96 db connections, NIO2 with 84 threads
- With 8000 users: 384 queries/s, some errors due to courses with glossaries, avg. < 13.2s, 99% < 40.1s, ramp in 60s, thinks 5s (logout too), 96 db connections, NIO2 with 84 threads

2020-05-03: Update to Gatling 3.3.1. This change has an implication on test results. As Gatling needs more than twice the CPU as earlier, the results of OpenOlat load test are lower. The results under are made with the exact same setup as the results above.
- With 4000 users: 240 queries/s, some errors due to courses with glossaries, avg. < 7.8s, 99% < 33.7s, ramp in 60s, thinks 5s (logout too), 96 db connections, NIO2 with 84 threads

2022: New hardware for testing MacBook Pro M1 (Apple M1 Max with 8 cores, 3.2 Ghz)

2022-10-14: Configuration for OpenOlat 17.1.0, update to Gatling 3.8.4, chat, rating, comment, lectures, assessment modes, UIBK like...
- With 1000 users: 110 queries/s, some errors due to courses with glossaries, avg. < 1.0s, 99% < 10.2s, ramp in 60s, thinks 5s (logout too), 96 db connections, NIO2 with 84 threads
- With 1000 users: 117 queries/s, some errors due to courses with glossaries, avg. < 0.2s, 99% < 9.9s, ramp in 60s, thinks 5s (logout too), 196 db connections, IO
- With 1500 users: 148 queries/s, some errors due to courses with glossaries, avg. < 3.0s, 99% < 17.6s, ramp in 60s, thinks 5s (logout too), 96 db connections, IO
- With 2000 users: 172 queries/s, some errors due to courses with glossaries, avg. < 4.9s, 99% < 30.1s, ramp in 60s, thinks 5s (logout too), 96 db connections, IO
- With 3000 users: 183 queries/s, some errors due to courses with glossaries, connection timeout (<350), avg. < 8.9s, 99% < 59.1s, ramp in 60s, thinks 5s (logout too), 96 db connections, IO
- With 3000 users: 184 queries/s, some errors due to courses with glossaries, connection timeout (<370), avg. < 8.5s, 99% < 57.7s, ramp in 60s, thinks 5s (logout too), 196 db connections, IO

2023-01-06: Configuration for OpenOlat 17.2.0, update to Gatling 3.9.0, chat, rating, comment, lectures, assessment modes, UIBK like...
- With 1000 users: 124 queries/s, some errors due to courses with glossaries, avg. = 0.02s, 99% < 0.1s, ramp in 60s, thinks 5s (logout too), 96 db connections, NIO2 with 90 threads
- With 1500 users: 185 queries/s, some errors due to courses with glossaries, avg. = 0.05s, 99% < 1.1s, ramp in 60s, thinks 5s (logout too), 96 db connections, NIO2 with 90 threads
- With 2000 users: 217 queries/s, some errors due to courses with glossaries, avg. < 1.10s, 99% < 7.6s, ramp in 60s, thinks 5s (logout too), 96 db connections, NIO2 with 90 threads
- With 3000 users: 265 queries/s, some errors due to courses with glossaries, avg. < 2.3s, 99% < 19.6s, ramp in 60s, thinks 5s (logout too), 96 db connections, NIO2 with 90 threads
- With 3000 users: 321 queries/s, some errors due to courses with glossaries, avg. < 1.5s, 99% < 6.0s, ramp in 60s, thinks 5s (logout too), 96 db connections, IO
- With 4000 users: 352 queries/s, some errors due to courses with glossaries, avg. < 3.8s, 99% < 11.0s, ramp in 60s, thinks 5s (logout too), 96 db connections, IO
- With 6000 users: 375 queries/s, some errors due to courses with glossaries, avg. < 8.7s, 99% < 23.1s, ramp in 60s, thinks 5s (logout too), 96 db connections, IO
OpenOlat crash with 8000 users due to a deadlock in the LegacyHiLoAlgorithmOptimizer (used to generate primary keys)
- With 8000 users: 280 queries/s, some errors due to courses with glossaries, connection timeout (<5500), avg. < 18.9s, 99% < 60.0s, ramp in 60s, thinks 5s (logout too), 96 db connections, IO



