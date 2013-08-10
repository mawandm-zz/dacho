#include "../../module.h"
#include <windows.h>
#include <iostream>
#include <fstream>

extern "C" IMAGE_DOS_HEADER __ImageBase;

std::wstring GetModuleLocation(){
	WCHAR DllPath[MAX_PATH] = {0};
	GetModuleFileNameW((HINSTANCE)&__ImageBase, DllPath, _countof(DllPath));
	return std::wstring(DllPath);
}