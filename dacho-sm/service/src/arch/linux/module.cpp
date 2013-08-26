#include <stdlib.h>
#include <fstream>
#include <dlfcn.h>
#include <sstream>
#include <iostream>
#include <stdexcept>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include "../../module.h"

#ifndef MAX_PATH
#define MAX_PATH 256
#endif

void * handleJVM;

static void GetModuleLocation(std::string &location){
  char buf[MAX_PATH] = {0};
  readlink("/proc/self/exe", buf, sizeof(buf)-1);
  location = buf;
}

std::string GetDachoHome(){
  //std::wstring location = GetModuleLocation();
  //std::string location = "/host/Users/mawandm/Documents/Projects/kakooge/dacho/dacho-sm/dist";
  std::string location;
  GetModuleLocation(location);
  location = location.substr(0, location.rfind('/'));
  location = location.substr(0, location.rfind('/'));
  return location;
}

static void FindJVM(std::string &JVMPath){

  char LIBPath[MAX_PATH];
  int result;
  struct stat buf;
  
  char* pPath = getenv("JAVA_HOME");
  if(pPath==NULL){
    return;
  }
  
  memset(LIBPath, 0, sizeof LIBPath);
  const char * dllPathEnd = "lib/libjvm.so";
  
  sprintf(LIBPath, "%s/jre/%s", pPath, dllPathEnd);
  result = stat(LIBPath, &buf);
  JVMPath = LIBPath;
  
  if (result != 0 || (buf.st_mode & S_IFREG) != S_IFREG){
    sprintf(LIBPath, "%s/%s", pPath, dllPathEnd);
    result = stat(LIBPath, &buf);
    JVMPath = LIBPath;
  }
}

//- Loads the JVM
void * CreateJavaVM(){ 
  std::string JVMPath;
  FindJVM(JVMPath);
  handleJVM = dlopen(JVMPath.c_str(), RTLD_LAZY);
  if (handleJVM == NULL){ 
    std::stringstream msgstream;
    msgstream << __FILE__ << ":" << __LINE__ << " Could not find JVM at " << JVMPath << ". Did you set %JAVA_HOME% ?";
    throw std::runtime_error(msgstream.str().c_str());
  }
  return dlsym(handleJVM, "JNI_CreateJavaVM");
}

//- Attempts to decrease the reference count of the JVM
void DestroyJavaVM(){
  if(handleJVM==NULL)
    return;
  dlclose(handleJVM);
}
