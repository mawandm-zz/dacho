#include "setup.h"

HWND hEdit;

FARPROC lpfnOldWndProc;
LONG editWndProc;

char szFolderName[MAX_PATH] = "";
char cmdLine[5];

char *ltrim(char *str){
	char *trim = str;

	while(*trim){
		if(*trim!=' ' && *trim!='"'){
			str = trim;
			break;
		}
		++trim;
	}

	return str;
}

char *rtrim(char *str){

	char *trim = _strrev(str);

	while(*trim){
		if(*trim!=' ' && *trim!='"'){
			str = trim;
			break;
		}
		++trim;
	}

	return _strrev(str);

}




UINT_PTR CALLBACK OFNHookProc(HWND hwnd, UINT uiMsg, WPARAM wParam, LPARAM lParam){

	int retval = 0;
	HWND hwDlg = GetParent(hwnd);

	HWND hok = GetDlgItem(hwDlg, IDOK);
	HWND hcancel = GetDlgItem(hwDlg, IDCANCEL);
	HWND list = GetDlgItem(hwDlg, lst1);

	

	if(uiMsg == WM_INITDIALOG){

		
		SendMessage(hwDlg, CDM_HIDECONTROL, (WPARAM)stc2, 0);
		SendMessage(hwDlg, CDM_HIDECONTROL, (WPARAM)cmb1, 0);
		SendMessage(hwDlg, CDM_HIDECONTROL, (WPARAM)edt1, 0);

		MoveWindow(GetDlgItem(hwDlg, stc2), 0, 0, 0, 0, TRUE);
		MoveWindow(GetDlgItem(hwDlg, cmb1), 0, 0, 0, 0, TRUE);
		
		SendMessage(hwDlg, CDM_SETCONTROLTEXT, stc3, (LPARAM)"Folder name");

        SendMessage(hwDlg, CDM_SETCONTROLTEXT, IDOK, (LPARAM)"Open");
		EnableWindow(hok, FALSE);
                
        SendMessage(hwDlg, CDM_SETCONTROLTEXT, IDCANCEL, (LPARAM)"Close");

		/*lpfnOldWndProc = (FARPROC)SetWindowLong(hwnd, GWL_WNDPROC, (DWORD) SubClassFunc);*/

		retval = 1;
	}

            

	
	if(uiMsg == WM_NOTIFY){
		OFNOTIFY *ofn = (OFNOTIFY *)lParam;

		if(ofn->hdr.hwndFrom == hok){
			MessageBox(hwnd, "OK Clicked", APPNAME, MB_OK | MB_ICONSTOP);
		}

		if(ofn->hdr.hwndFrom == hcancel){
			MessageBox(hwnd, "Cancel Clicked", APPNAME, MB_OK | MB_ICONSTOP);
		}


		switch(ofn->hdr.code){
			case CDN_INITDONE:

				{
					HFONT hfDefault;

					RECT rcClient, rcWindow, rcDlg;

					GetWindowRect(hwDlg, &rcDlg);
					GetWindowRect(GetDlgItem(hwDlg, edt1), &rcWindow);
					GetClientRect(GetDlgItem(hwDlg, edt1), &rcClient);

					hEdit = CreateWindowEx(WS_EX_CLIENTEDGE, "EDIT", "", WS_CHILD | WS_VISIBLE, 
						rcWindow.left - rcDlg.left, (rcWindow.top - rcDlg.top) - (rcWindow.bottom - rcWindow.top), 
						rcWindow.right - rcWindow.left, rcWindow.bottom - rcWindow.top, 
						hwDlg, (HMENU)IDC_MAIN_EDIT, GetModuleHandle(NULL), NULL);

					GetWindowRect(hEdit, &rcWindow);
					GetClientRect(hEdit, &rcClient);

/*					editWndProc = SetWindowLong(hEdit, GWL_WNDPROC, EditWinProc);*/

					

					hfDefault = GetStockObject(DEFAULT_GUI_FONT);
					SendMessage(hEdit, WM_SETFONT, (WPARAM)hfDefault, MAKELPARAM(FALSE, 0));

					EnableWindow(hEdit, FALSE);

					retval = 1;
				}
				break;
			case CDN_SELCHANGE:
				{
					/*
					SendMessage(hwDlg,CDM_GETFOLDERPATH,_MAX_PATH,(LPARAM)szFolderName);
					SendMessage(hwDlg, CDM_SETCONTROLTEXT, MAKEINTRESOURCE(edt1 ), szFolderName);
					SetDlgItemText(hwDlg, IDC_MAIN_EDIT, szFolderName);*/

				}
				break;
			case CDN_FOLDERCHANGE:
				{

					int result;
					struct _stat buf;
					char libpath[MAX_PATH];

					SendMessage(hwDlg,CDM_GETFOLDERPATH,_MAX_PATH,(LPARAM)szFolderName);
					

					sprintf(libpath, "%s\\bin\\client\\jvm.dll", szFolderName);
					result = _stat(libpath, &buf);

					if (result==-1 || (buf.st_mode & _S_IFREG) != _S_IFREG){
						EnableWindow(hok, FALSE);
					}else{
						EnableWindow(hok, TRUE);
					}

					SendMessage(hwDlg, CDM_SETCONTROLTEXT, (WPARAM)MAKEINTRESOURCE(edt1), (LPARAM)libpath);
					SetDlgItemText(hwDlg, IDC_MAIN_EDIT, szFolderName);

					return 1;
					
					
				}
				break;
			case CDN_FILEOK:
				{
					_strnset(szFolderName, '\0', MAX_PATH);

					GetDlgItemText(hwDlg, IDC_MAIN_EDIT, szFolderName, MAX_PATH);
					SetDlgItemText(ofn->lpOFN->hwndOwner, IDC_EDIT_JH, szFolderName);
					EndDialog(hwDlg, IDOK);

				}
				break;
			
			default:
				break;
		}
	}

	/*SetWindowLongPtrW(hwnd, DWLP_MSGRESULT, retval);*/
	return retval;
}




void DoFolderOpen(HWND hwnd){
	OPENFILENAME ofn;
	

	ZeroMemory(&ofn, sizeof(ofn));

	ofn.lStructSize = sizeof(OPENFILENAME);
	ofn.hwndOwner = hwnd;
	ofn.lpstrFilter = "Folders Only\0";
	ofn.lpstrFile = szFolderName;
	ofn.nMaxFile = MAX_PATH;
	ofn.Flags = OFN_EXPLORER | 
                OFN_ENABLEHOOK | 
                OFN_HIDEREADONLY;

	ofn.lpfnHook = (LPOFNHOOKPROC) OFNHookProc;


	GetOpenFileName(&ofn);
}


FARPROC JNU_FindCreateJavaVM(const char *libpath){ 

	HINSTANCE hVM = LoadLibrary(libpath);

	if (hVM == NULL){ 
		return NULL; 
	}


	return GetProcAddress(hVM, "JNI_CreateJavaVM");
} 



static BOOL ProgramFiles(char *home){
	/* Call CoInitialize() and create the link if OK. */
	HRESULT hRes = CoInitialize(NULL);
	BOOL result = FALSE;


	if (SUCCEEDED(hRes)){

		if(SHGetSpecialFolderPath(NULL, home, CSIDL_PROGRAM_FILES, TRUE)==TRUE)
			result = TRUE;
	}
	/* call CoUninitialize() and exit the program. */
	CoUninitialize();

	return result;
}

static void FindJVM(char *startsearch, char *jvmpath){
	//Read the environment for JAVA_HOME
	//Read the environment for PATH
	//Find Java folder in Program Files

	char filter[MAX_PATH];
	char libpath[MAX_PATH];
	char librelpath[MAX_PATH];

	int result;

	struct _stat buf;

	time_t ctime = 0;
	
	WIN32_FIND_DATA FindFileData;
	HANDLE hFind;

	sprintf(filter, "%s\\%s", startsearch, "*.*");


	hFind = FindFirstFile(filter, &FindFileData);

	while(hFind!=INVALID_HANDLE_VALUE && GetLastError() != ERROR_NO_MORE_FILES){

		if(strcmp(FindFileData.cFileName, ".") == 0 || strcmp(FindFileData.cFileName, "..") == 0){
			FindNextFile(hFind, &FindFileData);
			continue;
		}

		if((FindFileData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) == FILE_ATTRIBUTE_DIRECTORY){
			memset(librelpath, 0, sizeof(librelpath));
			memset(libpath, 0, sizeof(libpath));

			if (strstr(_strlwr(FindFileData.cFileName), "jdk") != NULL)
				sprintf(librelpath, "%s\\%s\\jre", startsearch, FindFileData.cFileName);
			else
				sprintf(librelpath, "%s\\%s", startsearch, FindFileData.cFileName);

			sprintf(libpath, "%s\\bin\\client\\jvm.dll", librelpath);


			result = _stat(libpath, &buf);

			if (result == 0 && (buf.st_mode & _S_IFREG) == _S_IFREG && ctime < buf.st_ctime){
				memset(jvmpath, 0, sizeof(libpath));
				ctime = buf.st_ctime;
				strcpy(jvmpath, librelpath);
			}
		}

		FindNextFile(hFind, &FindFileData);

	}

	if(hFind != INVALID_HANDLE_VALUE)
		FindClose(hFind);

}


/*
 * Install the application using the specified options to the JVM
 * -1 -> couldn't find jvm.dll
 * -2 -> couldn't load Java
 * -3 -> unsupported JVM
 */

static int InstallApplication(HWND hwnd, const char* java_home, const char *opt){

	JNIEnv          *env;
	JavaVM          *jvm;
	JavaVMInitArgs  vm_args;
	JavaVMOption    options[7];
	int             nbOptions;
	jint            res;
	jclass          cls;
	jmethodID       mainId;

	char libpath[MAX_PATH];

	FARPROC pfnCreateVM;	

	char dclasspath[MAX_PATH];
	char dlibraryhome[MAX_PATH];
	char dkaratasihome[MAX_PATH];
	char action[100];
	
	char dinstallfiles[MAX_PATH];
	char dsuggestedinstallhome[MAX_PATH];
	char suggestedinstallhome[MAX_PATH];
	char *c;

	struct _stat buf;
	int result;

	nbOptions=7;

	/*options = (JavaVMOption *)calloc(nbOptions, sizeof(struct JavaVMOption));*/

	GetModuleFileName(GetModuleHandle(NULL), dkaratasihome, MAX_PATH);
	c = strrchr(dkaratasihome, '\\');

	while(*c++ != '\0')
		*c = '\0';
	/*
	-Dkaratasi.install.files=M:\karatasi\setup\Debug\dist -Daction=uninstall -Dkaratasi.install.home="c:\program files\Karatasi"
	*/
	options[0].optionString = "-Djava.compiler=NONE";				/* disable JIT */
	
#if defined(WIN32)
#define CLASSPATH "-Djava.class.path=%s\\%s"
#elif defined(UNIX)
#define CLASSPATH "-Djava.class.path=%s/%s"
#endif
	sprintf(dclasspath, CLASSPATH, dkaratasihome, DACHOSM_SETUP_JAR);			/* user classes */
	options[1].optionString = dclasspath;
	
#if defined(WIN32)
#define JAVA_LIB_PATH "-Djava.library.path=%s\\lib"
#elif defined(UNIX)
#define JAVA_LIB_PATH "-Djava.library.path=%s/lib"
#endif

	sprintf(dlibraryhome, JAVA_LIB_PATH, dkaratasihome);	/* set native library path */
	options[2].optionString = dlibraryhome;
	options[3].optionString = "-verbose:jni";											/* print JNI-related messages */

	sprintf(dinstallfiles, "-Dkaratasi.install.files=%s", dkaratasihome);
	options[4].optionString = dinstallfiles;

	sprintf(action, "-Daction=%s", opt);
	options[5].optionString = action;

	memset(suggestedinstallhome, 0, sizeof(suggestedinstallhome));
	ProgramFiles(suggestedinstallhome);
	sprintf(dsuggestedinstallhome, "-Dsuggested.installation.home=%s\\Karatasi", suggestedinstallhome);
	options[6].optionString = dsuggestedinstallhome;


	vm_args.version  = JNI_VERSION_1_2;                   /* Specifies the JNI version used */
	vm_args.options  = options;
	vm_args.nOptions = nbOptions;
	vm_args.ignoreUnrecognized = JNI_TRUE;                 /* JNI won't complain about unrecognized options */

	sprintf(libpath, "%s%s", java_home, JVM_DLL);
	result = _stat(libpath, &buf);
	if (result==-1 || (buf.st_mode & _S_IFREG) != _S_IFREG)
		return -1;

	pfnCreateVM = JNU_FindCreateJavaVM(libpath);

	if(pfnCreateVM == NULL)	
		return -2;

	res = (*pfnCreateVM)(&jvm, (void **)&env, &vm_args);

	if( res < 0 )
		return -2;

	if((*env)->GetVersion(env) < SUPPORTED_JVM)
		return -3;

	cls   = (*env)->FindClass(env, MAIN_CLASS);

	/* find the main() method */
	mainId = (*env)->GetStaticMethodID(env, cls, "main", "([Ljava/lang/String;)V");
	
	if( mainId == 0 )
		return 1; /* error */
	
	(*env)->CallStaticVoidMethod(env, cls, mainId, 0); /* call main() */

	/*Registry Additions*/
	{
		HKEY hKey;
		RegCreateKeyEx(HKEY_LOCAL_MACHINE, REG_KEY, 0, NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, 0, &hKey, &result);
		RegSetValueEx(hKey, "JavaHome\0", 0, REG_SZ, java_home, strlen(java_home));
		RegCloseKey(hKey);
	}

	(*jvm)->DestroyJavaVM( jvm ); /* kill the JVM */

	return 0;

}

// Step 4: the Window Procedure
BOOL CALLBACK SetupDlgProc(HWND hwnd, UINT Message, WPARAM wParam, LPARAM lParam){

	char java_home[_MAX_PATH];


    switch(Message){
        case WM_INITDIALOG:
			{
				HKEY hKey;
				long result;  
				DWORD lpType;
				RECT dlgRect;
				POINT point;
				MONITORINFO moninfo;
				HICON hIcon;
				HMONITOR hmon = MonitorFromWindow(hwnd, MONITOR_DEFAULTTOPRIMARY);

				ShowWindow(GetDlgItem(hwnd, IDC_STATIC_JVMSR), SW_HIDE);

				_strset(java_home, '\0');

				hIcon = (HICON)LoadImage(GetModuleHandle(NULL), MAKEINTRESOURCE(IDI_ICON), IMAGE_ICON, 16, 16, 0);

				if(hIcon)
					SendMessage(hwnd, WM_SETICON, ICON_BIG, (LPARAM)hIcon);
				
				if(RegOpenKeyEx(HKEY_LOCAL_MACHINE, REG_KEY, 0, KEY_QUERY_VALUE, &hKey) == ERROR_SUCCESS){
					if(RegQueryValueEx(hKey, "JavaHome\0", NULL, &lpType, java_home, &result) == ERROR_SUCCESS)
						SetDlgItemText(hwnd, IDC_EDIT_JH, java_home);
				}

				RegCloseKey (hKey);

				moninfo.cbSize = sizeof(MONITORINFO);
				result = GetMonitorInfo(hmon, &moninfo);


				GetWindowRect(hwnd, &dlgRect);

				if(result != 0){
					point.x = (moninfo.rcMonitor.right - (dlgRect.right - dlgRect.left)) / 2;
					point.y = (moninfo.rcMonitor.bottom - (dlgRect.bottom - dlgRect.top)) / 2;

					SetWindowPos(hwnd, NULL, point.x, point.y, 0, 0, SWP_NOSIZE);
				}

				if(strlen(java_home)>0){
					PostMessage(hwnd, WM_COMMAND, MAKEWPARAM(IDNEXT, 0), 0);
					ShowWindow(hwnd, SW_HIDE);

				}else{
					char program_files[MAX_PATH];
					ProgramFiles(program_files);
					strcat(program_files, "\\");
					strcat(program_files, "Java");
					FindJVM(program_files, java_home);

					if(strlen(java_home)>0){
						SetDlgItemText(hwnd, IDC_EDIT_JH, java_home);
						ShowWindow(GetDlgItem(hwnd, IDC_STATIC_JVMSR), SW_SHOW);
					}
				}
				
			}
			break;

        case WM_COMMAND:

            switch(LOWORD(wParam)){

                case IDCANCEL:
                    EndDialog(hwnd, IDC_BUTTON_CANCEL);
					break;

				case IDC_BUTTON_JH:
					DoFolderOpen(hwnd);
					break;

				case IDNEXT:
					{
						struct _stat buf;
						int result;
						char jcmd[1024];

						GetDlgItemText(hwnd, IDC_EDIT_JH, java_home, _MAX_PATH);

						if(strlen(ltrim(rtrim(java_home)))==0){
							MessageBox(hwnd, "You must provide a Java installation to use", APPNAME, MB_OK | MB_ICONSTOP);
							return TRUE;
						}

						result = _stat(java_home, &buf);
						if (result==-1 || (buf.st_mode & _S_IFDIR) != _S_IFDIR){
							MessageBox(hwnd, "The Java installation must be a directory", APPNAME, MB_OK | MB_ICONSTOP);
							return TRUE;
						}

						if(strcmp(cmdLine, "/u")==0)
							strcpy(jcmd, "uninstall");
						else if(strcmp(cmdLine, "/i")==0)
							strcpy(jcmd, "install");


						result = InstallApplication(hwnd, java_home, jcmd);

						switch(result){
							case -1:
								{
									char errbuf[1024];
									sprintf(errbuf, "Either the file '%s\\bin\\client\\jvm.dll' does not exist or is an invalid file", java_home);
									MessageBox(hwnd, errbuf, APPNAME, MB_OK | MB_ICONSTOP);
									return TRUE;
								}
								break;

							case -2:
								MessageBox(NULL, "Failed to load Java VM Library", APPNAME, MB_ICONERROR | MB_OK);
								break;

							case -3:
								MessageBox(NULL, "Minimum Java Version is 1.6", APPNAME, MB_ICONERROR | MB_OK);
								break;

							default:
								return TRUE;
						}

					}
					break;
            }
			
			break;

        default:
            return FALSE;
    }
    return TRUE;
}


int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow){

	int ret;
	
	strcpy(cmdLine, lpCmdLine);

	if(lpCmdLine == NULL || strlen(lpCmdLine)==0)
		strcpy(cmdLine, "/i");


	if((strcmp(cmdLine, "/u") != 0) && (strcmp(cmdLine, "/i") != 0)){
		MessageBox(NULL, "Failed to parse command parameters", APPNAME, MB_ICONERROR | MB_OK);
		return -1;
	}

	ret = DialogBox(GetModuleHandle(NULL), MAKEINTRESOURCE(IDD_DIALOG), NULL, SetupDlgProc);

	return 0;

}
