                                      Dacho Java Service Manager README

1. Introduction
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

2. Installation
The structure of the distribution is as
+
+ tests (the various unit tests that you could run to validate the software)
+ logs (the logs for the software)
+ config (configuration files)
+ bin (windows service, unix deamon binaries and shell scripts that can be used to quickly run the service)
+ services (this is where the services are installed. Services are packaged as normal jar files but fancily named ending in .dar)
+ examples (sample services that I've written to demonstrate how it works)
+ README.txt
+ LICENCE.txt

NOTE: The Windows service and unix daemon expect the environment variable JAVA_HOME to be set and because the JVM is loaded in server mode, it expects to find the %JAVA_HOME%\jre\bin\server\jvm.dll or %JAVA_HOME%\bin\server\jvm.dll file

My objective was it make it really easy to use and I really think it is. This all you have to do. You could look at the examples provided with this distribution to get a better understanding of the implementation of a service.
2.1. Extend ServiceBase
2.2. Implement OnStart, OnStop
2.3. In the OnStart, start a thread
2.4. Stop the thread in the OnSTop
2.5. Package and configure your services.xml file stating most importantly the service class and then the classpath entries
2.6. Test the application. A quick way is to test by providing a test Main class and attempt to run your service as a normal applications
   E.g. ServiceBaseImplementation.start()
    	Thread.sleep(<for some time enough for the various application components to be testes);
    	ServiceBaseImplementation.stop();
    	
    	Where ServiceBaseImplementation.start() has enclosed the OnStart(...) behaviour
    	and the ServiceBaseImplementation.stop() has enclosed the OnStop() behaviour
    	
    	Best to have a look at the examples such as the Derby Service as this is a really easy one follow
    	
2.7. Thats it

3. Licence
This software is copyrighted to Michael Sekamanya (myself) but really you are free to use it anyhow.
I'm however not liable for ANY damages whatsoever that may be caused as a result of either directly or indirectly
using the software.

7. Contact me dacho-sm@gmail.com
