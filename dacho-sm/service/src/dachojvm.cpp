#include <string.h>
#include <stdexcept>
#include <sstream>
#include "dachojvm.h"
#include "module.h"


static void someSimpleUntrustedTrim(std::string &str){
  std::stringstream temp;
  temp << str;
  str.clear();
  temp >> str;
}

void DachoJVM::init(){

  // Make options
  std::vector<std::string>::iterator v_it = v_optionstr.begin();
  std::vector<JavaVMOption>::iterator vo_it = v_options.begin();
  for (; v_it!=v_optionstr.end(); ++v_it, ++vo_it){
    someSimpleUntrustedTrim(*v_it);
    vo_it->optionString = const_cast<char *>(v_it->c_str());
#ifdef DEBUG
    std::cout << (*v_it) << "\n";
#endif
  }

  vm_args.version  = JNI_VERSION_1_6;
  vm_args.options  = &v_options[0];
  vm_args.nOptions = v_options.size();
  vm_args.ignoreUnrecognized = JNI_TRUE;                 /* JNI won't complain about unrecognized options */

  //- Start machine
  //if (JNI_CreateJavaVM(&jvm, (void **)&env, &vm_args)) 
  typedef jint (JNICALL*LPTRCreateJavaVM)(JavaVM **, void **, void *);

  LPTRCreateJavaVM ptrCreateJavaVM = reinterpret_cast<LPTRCreateJavaVM>(CreateJavaVM());
  if(!ptrCreateJavaVM)
    throw std::runtime_error("Failed to create the JVM. Did you set the JAVA_HOME variable");

  if(ptrCreateJavaVM(&jvm, (void **)&env, &vm_args))
    throw std::runtime_error("Failed to create the JVM");

  if(env->GetVersion() < SUPPORTED_JVM)
    throw std::runtime_error("Unsupported JVM version, please upgrade");
}

void DachoJVM::destroy(){
  if(jvm)
    jvm->DestroyJavaVM(); /* kill the JVM */

  DestroyJavaVM();
}

void DachoJVM::executeMethod(const std::string &className, const std::string &methodName, const std::string &signature){

  if(!env)
	  throw std::runtime_error("JVM Environemnt has not been initialized"); /* error */

  jclass cls = env->FindClass(className.c_str());

  /* find the main() method */
  jmethodID methodId = env->GetStaticMethodID(cls, methodName.c_str(), signature.c_str());
	
  if( methodId == 0 )
    throw std::runtime_error("Could not find method. Failed to execute method"); /* error */
	
  env->CallStaticVoidMethod(cls, methodId, 0); /* call method() */
}

DachoJVM::DachoJVM(std::vector<std::string> &v_config)
  : v_options(v_config.size()), v_optionstr(v_config){}
DachoJVM::~DachoJVM() throw(){}
/*
DachoJVM::DachoJVM(const DachoJVM &other) : properties(other.properties){}
DachoJVM & DachoJVM::operator=(const DachoJVM &other){}
*/
