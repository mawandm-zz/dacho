#include <windows.h>
#include <iostream>
#include <fstream>
#include <clocale>
#include <Shlwapi.h>
#include <sstream>
#include <sys/types.h>
#include <sys/stat.h>

#include "../../module.h"

extern "C" IMAGE_DOS_HEADER __ImageBase;

static void GetModuleLocation(char *path, int len){
	WCHAR DllPath[MAX_PATH] = {0};
	GetModuleFileNameW((HINSTANCE)&__ImageBase, DllPath, _countof(DllPath));
	WideCharToMultiByte(CP_ACP, 0, DllPath, -1, path, len > MAX_PATH ? MAX_PATH : len, NULL, NULL);
	path[len-1]='\0';
}

std::string GetDachoHome(){
	char path[MAX_PATH] = {0};
	
#if _DEBUG
	std::string location = "C:\\Users\\mawandm\\Documents\\projects\\kakooge\\dacho\\dacho-sm\\dist\\config\\djvm.properties";
	unsigned len = location.length();
	strncpy(path, location.c_str(), len > MAX_PATH ? MAX_PATH : len);
#else
	GetModuleLocation(path, MAX_PATH);
#endif

	PathRemoveFileSpec(path);
	PathRemoveFileSpec(path);
	return path;
}


static void FindJVM(std::string &JVMPath){
	//Read the environment for JAVA_HOME
	//Read the environment for PATH
	//Find Java folder in Program Files

	char LIBPath[MAX_PATH];
	int result;
	struct _stat buf;
	
	char* pPath = getenv("JAVA_HOME");
	if(pPath==NULL){
		return;
	}

	memset(LIBPath, 0, sizeof LIBPath);
	const char * dllPathEnd = "bin\\server\\jvm.dll";

	sprintf(LIBPath, "%s\\jre\\%s", pPath, dllPathEnd);
	result = _stat(LIBPath, &buf);
	JVMPath = LIBPath;

	if (result != 0 || (buf.st_mode & _S_IFREG) != _S_IFREG){
		sprintf(LIBPath, "%s\\%s", pPath, dllPathEnd);
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
