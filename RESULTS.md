Results on MacBook Pro, PostgreSQL, without Apache
--------------------------------------------------

The test is the UIBK like test:
- login, open a course or the details page and
  return to "My courses" 5 five times, logout.
- 2000 users ramp up in 50 seconds
- 5 seconds between opening courses
- 50 seconds before logout

1. Standard configuration for OpenOLAT (chat, rating, comments)
   - Version: 10.0.2, 133 queries/s, 1% errors (timeout)

2. OpenOLAT without rating and comments but with chat
   - Version: 10.0.0, 180 queries/s, 0% errors (but there is some errors)
   
3. OpenOLAT without rating, comments and chat
   - Version: 10.0.0, 187 queries/s, 1% errors

--------------------------------------------------

The test is the UIBK like test as above:
- login, open a course or the details page and
  return to "My courses" 5 five times, logout.
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
  
