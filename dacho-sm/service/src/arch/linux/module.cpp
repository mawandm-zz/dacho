#include "../../module.h"
#include <iostream>
#include <fstream>

#ifndef MAX_PATH
#define MAX_PATH 4096
#endif

static std::wstring GetModuleLocation(){
	return std::wstring();
}

std::string GetDachoHome(){
  //std::wstring location = GetModuleLocation();
  std::string location = "/host/Users/mawandm/Documents/Projects/kakooge/dacho/dacho-sm/dist";
  char path[MAX_PATH] = {0};
  return location;
}
