#ifndef _SETUP_H
#define _SETUP_H

#define STRICT

#ifndef _WIN32_WINNT
#define _WIN32_WINNT 0x0500
#endif

#ifndef WINVER
#define WINVER 0x0500
#endif

#ifndef CSIDL_PROGRAM_FILES
#define CSIDL_PROGRAM_FILES 0x0026
#endif

#include <windows.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <dlgs.h>
#include <commctrl.h>

#include <objbase.h>
#include <shlobj.h>
#include <shellapi.h>

#include <jni.h>
#include "resource.h"

#define IDC_MAIN_EDIT	101
#define REG_KEY	"Software\\Karatasi"

#define MAIN_CLASS  "com/karatasi/desktop/setup/Main"
#define APPNAME "Karatasi Spaces Setup"
#define SUPPORTED_JVM	0x00010006

#define DACHOSM_SETUP_JAR "dachosm-setup.jar"

#if defined(WIN32)
#define JVM_DLL "\\bin\\client\\jvm.dll"
#elif defined(UNIX)
#define JVM_DLL 
#endif

#endif
