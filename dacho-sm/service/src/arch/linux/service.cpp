#include "service.h"

namespace{
  Daemon *serviceDaemon;
}

int RunService(Daemon *daemon){
  serviceDaemon = daemon;
  return 0;
}

void InstallService(){
}
