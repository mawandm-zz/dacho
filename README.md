dacho
=====

The Dacho Java Services Manager

Introduction
Dacho is a java services container. It is a lightweight container that is able to
host any kind of java process. This could be a webservice such as Jetty, a download maanger
such as the download manager example. I currently use the dacho services manager for;

- Monitoring logs of a client's futures trading application. The service is hugely 
customizable using java regular expressions passed in through a configuration file. The
logging service then in turn sends email notification if the regular expression rules match

- Downloading (at regular intervals) prices for various financial securities and scheduling some financial
calculations on securities that are of interest to me.

- An ActiveMQ JMS application producing and sending thousands of message to multiple clients through and ActiveMQ broker  


The other objective is to have an implementation that is only dependent on the office Java API

Source Tree
----------

-dacho-sm (this is contains the source for the service manager)
 
--src (the service manager java source files)

--- service (windows service and unix daemon source files)

-dacho-sm-api (API that your application needs to build against)

-dist (where the final distribution is built to)

-- dacho-service-manager-1.0.zip is the release build

-- release (dacho-service-manager-1.0.zip is built from here)

-examples

-- jetty-ws (Jetty refactored as a dacho web service)

-- download-manager (a scheduled download manager)

-tests

-external (all the necessary libraries needed to build the various components)

-README.md

See also: www.student.city.ac.uk/~abgs750/dacho/
