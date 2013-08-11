#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <signal.h>
#include <limits>
#include <iostream>

#include "service.h"

namespace{
  Daemon *serviceDaemon;
  void terminateHandler(int signum){
    std::cout << "Terminating service...\n";
    serviceDaemon->Stop();
  }

  void registerTerminateHandler(){
    struct sigaction act;
    memset(&act, 0, sizeof act);
    act.sa_handler = &terminateHandler;
    // Use the sa_sigaction field, not sa_handler.
    act.sa_flags = SA_SIGINFO;
 
    if (!sigaction(SIGINT, &act, NULL)) {
      perror ("Failed to register the termination signal handler\n");
    }
  }
}


/**
   Run service
 */
int RunService(Daemon *daemon){
  serviceDaemon = daemon;
  /*
  FILE *fp= NULL;
  pid_t process_id = 0;
  pid_t sid = 0;
  
  // Create child process
  process_id = fork();

  // Indication of fork() failure
  if (process_id < 0){
    printf("fork failed!\n");
    
    // Return failure in exit status
    exit(1);
  }

  // PARENT PROCESS. Need to kill it.
  if (process_id > 0){
    printf("process_id of child process %d \n", process_id);
    
    // return success in exit status
    exit(0);
  }

  //unmask the file mode
  umask(0);

  //set new session
  sid = setsid();
  if(sid < 0){
    // Return failure
    exit(1);
  }

  // Change the current working directory to root.
  chdir("/");

  // Close stdin. stdout and stderr
  close(STDIN_FILENO);
  close(STDOUT_FILENO);
  close(STDERR_FILENO);
*/
  //  registerTerminateHandler();
  daemon->Start();
  sleep(std::numeric_limits<unsigned>::max());
  return 0;
}

void InstallService(){
}

void UninstallService(){
}
