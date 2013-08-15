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

static void FindJVM(std::string &JVMPath){

  char LIBPath[MAX_PATH];
  int result;
  struct _stat buf;
  
  char* pPath = getenv("JAVA_HOME");
  if(pPath==NULL){
    return;
  }
  
  memset(LIBPath, 0, sizeof LIBPath);
  const char * dllPathEnd = "lib/libjvm.so";
  
  sprintf(LIBPath, "%s/jre/%s", pPath, dllPathEnd);
  result = _stat(LIBPath, &buf);
  JVMPath = LIBPath;
  
  if (result != 0 || (buf.st_mode & _S_IFREG) != _S_IFREG){
    sprintf(LIBPath, "%s/%s", pPath, dllPathEnd);
    result = _stat(LIBPath, &buf);
    JVMPath = LIBPath;
  }
}

void * CreateJavaVM(){ 
  std::string JVMPath;
  FindJVM(JVMPath);
  HINSTANCE hVM = LoadLibrary(JVMPath.c_str());
  if (hVM == NULL){ 
    std::stringstream msgstream;
    msgstream << __FILE__ << ":" << __LINE__ << " Could not find JVM at " << JVMPath << ". Did you set %JAVA_HOME% ?";
    throw std::runtime_error(msgstream.str().c_str());
  }
  return (void *)GetProcAddress(hVM, "JNI_CreateJavaVM");
}
