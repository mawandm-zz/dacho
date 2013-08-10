#include "config.h"
#include "module.h"
#include <iostream>
#include <fstream>
#include <windows.h>
#include "Shlwapi.h"

static std::string GetDachoHome(){
	//std::wstring location = GetModuleLocation();
	std::string location = "C:\\Users\\mawandm\\Documents\\projects\\kakooge\\dacho\\dacho-sm\\dist\\config\\djvm.properties";
	char path[MAX_PATH] = {0};
	strcpy(path, location.c_str());
	PathRemoveFileSpec(path);
	PathRemoveFileSpec(path);
	return path;
}

void GetConfiguration(std::map<std::string, std::string> &config){
	
	std::string &dachohome = GetDachoHome();
	//-Ddacho.home=location\..
	
#if _WIN32
#define SEPARATOR "\\"
#else
#define SEPARATOR "/"
#endif

	std::string &location = dachohome + SEPARATOR + "config" + SEPARATOR "djvm.properties";
	std::string &classpath = dachohome + SEPARATOR + "lib"  + SEPARATOR + "dacho-service-manager-1.0.jar";
	std::string &configpath = dachohome + SEPARATOR + "config" + SEPARATOR "dacho.xml";
	std::string &logger = dachohome + SEPARATOR + "config" + SEPARATOR + "logging.properties";

	config["-Ddacho.home"] = dachohome;
	config["-Djava.class.path"] = classpath;
	config["-Dxml.config"] = configpath;
	config["-Djava.util.logging.config.file"] = logger;

	std::ifstream input(location.c_str());
	std::string line;
	while(std::getline(input, line)){
		size_t pos = 0;
		if(line[0] == '#')
			continue;
		while(pos>=0){
			pos = line.find('=', pos);
			if(pos==std::string::npos)
				break;
			else if(pos>0){
				if(line[pos-1]=='\\')
					continue;
				else{
					std::string &key = line.substr(0, pos);
					std::string &value = line.substr(pos+1, line.length());
					config[key] = value;
					break;
				}
			}
		}
	}
}