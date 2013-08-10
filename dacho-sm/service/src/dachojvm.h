#ifndef _DACHOJVM_H
#define _DACHOJVM_H

#include <jni.h>
#include <map>
#include <string>
#include <vector>

#define SUPPORTED_JVM 0x00010006

class DachoJVM{
  JNIEnv *env;
  JavaVM *jvm;

  JavaVMInitArgs vm_args;
  //JavaVMOption options[7]; // Max options???
  std::vector<JavaVMOption> v_options;
  std::vector<std::string> v_optionstr;
  //int nbOptions;

  std::map<std::string, std::string> &m_properties;
  DachoJVM(const DachoJVM &other);
  DachoJVM & operator=(const DachoJVM &other);

 public:

  //- Initializes the JVM
  void init();

  //- Destroys the JVM
  void destroy();

  //- Execute a static method
  void executeMethod(const std::string &className, const std::string &methodName, const std::string &signature);

  DachoJVM(std::map<std::string, std::string> &properties);
  ~DachoJVM() throw();
};

#endif
