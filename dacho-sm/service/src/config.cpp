#include "config.h"
#include "module.h"
#include <iostream>
#include <fstream>

void GetConfiguration(std::vector<std::string> &config){
	
  const std::string &dachohome = GetDachoHome();
  //-Ddacho.home=location\..
	
#if _WIN32
#define SEPARATOR "\\"
#else
#define SEPARATOR "/"
#endif

  const std::string &location = dachohome + SEPARATOR + "config" + SEPARATOR "djvm.properties";
  const std::string &classpath = dachohome + SEPARATOR + "lib"  + SEPARATOR + "dacho-service-manager-1.0.jar";
  const std::string &configpath = dachohome + SEPARATOR + "config" + SEPARATOR "dacho.xml";
  const std::string &logger = dachohome + SEPARATOR + "config" + SEPARATOR + "logging.properties";

  config.push_back("-Ddacho.home=" + dachohome);
  config.push_back("-Djava.class.path=" + classpath);
  config.push_back("-Dxml.config=" + configpath);
  config.push_back("-Djava.util.logging.config.file=" + logger);

  std::ifstream input(location.c_str());
  std::string line;
  while(std::getline(input, line)){
    size_t pos = 0;
    if(line[0] == '#')
      continue;
    config.push_back(line);
  }
}
