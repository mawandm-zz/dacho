#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <signal.h>
#include <syslog.h>
#include <iostream>
#include <limits>

#include "../../module.h"
#include "service.h"

namespace{
  Daemon *serviceDaemon;
  void terminateHandler(int, siginfo_t *, void *){
    syslog(LOG_INFO, "%s", "Terminating the daemon...");
    //    std::cout << "Terminating service...\n";
    serviceDaemon->Stop();
  }

  void registerTerminateHandler(){
    struct sigaction act;
    memset(&act, 0, sizeof act);
    act.sa_sigaction = &terminateHandler;
    act.sa_flags = SA_SIGINFO;
    int result = sigaction(SIGINT, &act, NULL);
    if (result!=0) {
      syslog(LOG_INFO, "%s", "Failed to register the termination signal handler");
      //      perror ("Failed to register the termination signal handler\n");
    }
  }
}

/**
   Run service
 */
int RunService(Daemon *daemon){
  serviceDaemon = daemon;

#if 0
  /* Our process ID and Session ID */
  pid_t pid, sid;

  syslog(LOG_INFO, "%s", "Starting the dacho daemon...");

  /* Fork off the parent process */
  pid = fork();
  if (pid < 0) {
    syslog(LOG_INFO, "%s", "Failed to fork off parent...");
    exit(EXIT_FAILURE);
  }

  /* If we got a good PID, then
     we can exit the parent process. */
  if (pid > 0) {
    exit(EXIT_SUCCESS);
  }

  /* Change the file mode mask */
  umask(0);
  
  /* Open any logs here */
  /* Create a new SID for the child process */
  sid = setsid();
  if (sid < 0) {
    syslog(LOG_INFO, "%s", "Could not create new SID for the child process...");
    /* Log the failure */
    exit(EXIT_FAILURE);
  }

  std::string &dachohome = GetDachoHome();
  /* Change the current working directory */
  if ((chdir(dachohome.c_str())) < 0) {
    /* Log the failure */
    exit(EXIT_FAILURE);
  }
  /* Close out the standard file descriptors */
  close(STDIN_FILENO);
  close(STDOUT_FILENO);
  close(STDERR_FILENO);

  syslog(LOG_INFO, "%s", "Starting the daemon...");
#endif
  /* Daemon-specific initialization goes here */
  daemon->Start();
  registerTerminateHandler();
  sleep(std::numeric_limits<unsigned>::max());
  return 0;
}

void InstallService(){
}

void UninstallService(){
}
