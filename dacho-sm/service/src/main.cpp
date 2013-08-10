#include <csignal>
#include <iostream>

#include <limits>

#if _WIN32
#include <windows.h>
#else
#include <unistd.h>
#endif

#include "dachojvm.h"
#include "service.h"
#include "config.h"

namespace{
	// Pointer to hold daemon
	Daemon *app_daemon;

	// Exit function
	void terminateSignalHandler (int param){
		printf ("Terminating program...\n");
		app_daemon->Stop();
		exit(1);
	}
}

void printUsage(){
	std::cout << "Usage: dacho [-i, -u, -a]\n";
	std::cout << "       -i to install the service\n";
	std::cout << "       -u to uninstall the service\n";
	std::cout << "       -a to run as an application\n";
}

int main(int argc, char *argv[]){

  std::map<std::string, std::string> properties;
  GetConfiguration(properties);
  Daemon daemon(properties);
  
  if (argc==1){
	  RunService(&daemon);
  }else if(argc==2){
	  if(!strncmp(argv[1], "-i", 2)){
		  InstallService();
	  }else if(!strncmp(argv[1], "-u", 2)){
		  UninstallService();
	  }else if(!strncmp(argv[1], "-a", 2)){
		  void (*jvmTerminate)(int);
		  jvmTerminate = signal(SIGTERM, terminateSignalHandler);
		  daemon.Start();
		  app_daemon = &daemon;
#if _WIN32
		  //Sleep(std::numeric_limits<unsigned>::max());
		  Sleep(10 * 1000);
#else
		  //sleep(std::numeric_limits<unsigned>::max());
		  sleep(10 * 1000);
#endif
		  daemon.Stop();
	  }else{
		  printUsage();
		  return 1;
	  }
  }else{
	  printUsage();
	  return 1;
  }
}
