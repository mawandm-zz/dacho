#include <csignal>
#include <iostream>
#include <string.h>
#include <limits>

#if _WIN32
#include <windows.h>
#else
#include <unistd.h>
#endif

#include "dachojvm.h"
#include "service.h"
#include "config.h"

void printUsage(){
	std::cout << "Usage: dacho [-a]\n";
	//These have been disabled so as not to allow misuse
	//std::cout << "Usage: dacho [-i, -u, -a]\n";
	/*std::cout << "       -i to install the service\n";
	std::cout << "       -u to uninstall the service\n";*/
	std::cout << "       -a to run as an application\n";
}

int main(int argc, char *argv[]){

  std::vector<std::string> v_config;
  GetConfiguration(v_config);
  Daemon daemon(v_config);
  
  if (argc==1){
    RunService(&daemon);
  }else if(argc==2){
    //This is only for testing purposes
    if(!strncmp(argv[1], "-a", 2)){
      //void (*jvmTerminate)(int);
      //jvmTerminate = signal(SIGTERM, terminateSignalHandler);
	  try{
		daemon.Start();
	  }catch(std::exception &e){
		std::cout << e.what() << "\n";
		return 1;
	  }
      //app_daemon = &daemon;
#if _WIN32
      //Sleep(std::numeric_limits<unsigned>::max());
      Sleep(10 * 1000);
#else
      //sleep(std::numeric_limits<unsigned>::max());
      sleep(10 * 1000);
#endif
	  try{
        daemon.Stop();
	  }catch(std::exception &e){
		std::cout << e.what() << "\n";
		return 1;
	  }
    }else{
      printUsage();
      return 1;
    }
  }else{
    printUsage();
    return 1;
  }
}
