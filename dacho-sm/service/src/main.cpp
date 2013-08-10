#include "dachojvm.h"
#include <iostream>

#if _WIN32
#include <windows.h>
#endif

#include "service.h"
#include "config.h"

#define SERVICE

#if _WIN32
int main(int argc, TCHAR *argv[]){
#else
int main(int argc, char *argv[]){
#endif

  std::map<std::string, std::string> properties;
#if _DEBUG
 #if _WIN32
  std::string dachohome = "C:/Users/mawandm/Documents/Projects/kakooge/dacho-sm/dist";
 #else
  std::string dachohome = "/host/Users/mawandm/Documents/Projects/kakooge/dacho-sm/dist/";
 #endif
#endif

  GetConfiguration(properties);

  /*
  properties["-Djava.compiler"]="NONE";
  properties["-Djava.class.path"]= dachohome + "/dacho-service-manager-1.0.jar";
  properties["-verbose:jni"]="";
  properties["-Dxml.config"]= dachohome + "/config/dacho.xml";
  properties["-Ddacho.home"]= dachohome;
  properties["-Djava.util.logging.config.file"] = dachohome + "/config/logging.properties";
  */

  Daemon daemon(properties);
  
  /*if(strcmp(argv[1], "/

  if(argc<2){
	  std::cout << "Usage: dacho.exe [/i, /a] /d\n";
	  std::cout << "       /i - to install the service\n";
	  std::cout << "       /a - to run as an application\n";
	  std::cout << "       /d - ro run with debuggin information\n";
	  return 1;
  }*/

#if defined(SERVICE)
RunService(&daemon);
#else

  daemon.Start();

#if _WIN32
  Sleep(60 * 1000);
#endif

  daemon.Stop();

  std::cout << "VM Destroyed";

#endif
}
