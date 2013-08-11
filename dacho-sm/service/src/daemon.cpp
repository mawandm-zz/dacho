/*
Reference:
http://msdn.microsoft.com/en-us/library/bb540476%28v=VS.85%29.aspx
http://www.codeproject.com/Articles/499465/Simple-Windows-Service-in-Cplusplus
*/

#include "daemon.h"
  
void Daemon::Start(){
  dachoJVM.init();
  dachoJVM.executeMethod("org/kakooge/dacho/loader/BootstrapDaemon", "initServiceManager", "()V");
}

void Daemon::Stop(){
  dachoJVM.executeMethod("org/kakooge/dacho/loader/BootstrapDaemon", "destroyServiceManager", "()V");
  dachoJVM.destroy();
}

Daemon::Daemon(std::vector<std::string> &v_config) : dachoJVM(v_config){}
Daemon::~Daemon() throw(){}
