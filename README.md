The Dacho Java Services Manager
-------------------------------

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

Compiled and zipped binaries are zipped into the single file dist/dacho-service-manager-1.0.zip

Description
-----------
For a detailed description of the design of this piece of software, please visit www.student.city.ac.uk/~abgs750/dacho/
