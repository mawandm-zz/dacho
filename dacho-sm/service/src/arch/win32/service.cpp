/*
Reference:
http://msdn.microsoft.com/en-us/library/bb540476%28v=VS.85%29.aspx
*/

#include <sstream>
#include <windows.h>
#include "service.h"

namespace{
	SERVICE_STATUS        g_ServiceStatus = {0};
	SERVICE_STATUS_HANDLE g_StatusHandle = NULL;
	HANDLE                g_ServiceStopEvent = INVALID_HANDLE_VALUE;
	HANDLE  hEventSource;
	Daemon *serviceDaemon;

	char messageBuffer[516];

	void LOG(const char *msg, int msglen, int type){
		memset(messageBuffer, 0, sizeof messageBuffer);
		strncpy(messageBuffer, msg, sizeof(messageBuffer)-1);
		ReportEvent(hEventSource, type, 0, 0, NULL, 2, 0, (LPCSTR*)&messageBuffer, NULL);
	}

	void WINAPI ServiceCtrlHandler (DWORD CtrlCode)
	{
		switch (CtrlCode) {
		 case SERVICE_CONTROL_STOP :
	 
			if (g_ServiceStatus.dwCurrentState != SERVICE_RUNNING)
			   break;
			
			// Perform tasks necessary to stop the service here 
			g_ServiceStatus.dwControlsAccepted = 0;
			g_ServiceStatus.dwCurrentState = SERVICE_STOP_PENDING;
			g_ServiceStatus.dwWin32ExitCode = 0;
			g_ServiceStatus.dwCheckPoint = 4;
	 
			SetServiceStatus (g_StatusHandle, &g_ServiceStatus);
	 
			// This will signal the worker thread to start shutting down
			SetEvent (g_ServiceStopEvent);
			break;
		 default:
			 break;
		}
	}

	VOID WINAPI ServiceMain (DWORD argc, LPTSTR *argv)
	{
		// Register our service control handler with the SCM
		g_StatusHandle = RegisterServiceCtrlHandler (SERVICE_NAME, ServiceCtrlHandler);

		if (!g_StatusHandle)
			return;

		// Tell the service controller we are starting
		g_ServiceStatus.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
		g_ServiceStatus.dwCurrentState = SERVICE_START_PENDING;

		if (!SetServiceStatus (g_StatusHandle , &g_ServiceStatus)){
			const char * msg = "Error setting starting status";
			LOG(msg, strlen(msg), EVENTLOG_ERROR_TYPE);
			goto EXIT;
		}
	 
		// Create a service stop event to wait on later
		g_ServiceStopEvent = CreateEvent (NULL, TRUE, FALSE, NULL);
		
		if (g_ServiceStopEvent == NULL){
			g_ServiceStatus.dwControlsAccepted = 0;
			g_ServiceStatus.dwCurrentState = SERVICE_STOPPED;
			g_ServiceStatus.dwWin32ExitCode = GetLastError();
			g_ServiceStatus.dwCheckPoint = 1;
	 
			SetServiceStatus (g_StatusHandle, &g_ServiceStatus);
			goto EXIT;
		}    
	    
		// Tell the service controller we are started
		g_ServiceStatus.dwControlsAccepted = SERVICE_ACCEPT_STOP;
		g_ServiceStatus.dwCurrentState = SERVICE_RUNNING;
		g_ServiceStatus.dwWin32ExitCode = 0;
		g_ServiceStatus.dwCheckPoint = 0;
	 
		if (!SetServiceStatus (g_StatusHandle, &g_ServiceStatus)){
			//ReportEvent(hEventSource, EVENTLOG_ERROR_TYPE, 0, 0, NULL, 2, 0, &ERROR_STARTING, NULL);
			const char * msg = "Error setting starting status";
			LOG(msg, strlen(msg), EVENTLOG_ERROR_TYPE);
			goto EXIT;
		}

		try{
			//ReportEvent(hEventSource, EVENTLOG_SUCCESS, 0, 0, NULL, 2, 0, &STARTING_MESSAGE, NULL);
			const char * msg = "Stopping service";
			LOG(msg, strlen(msg), EVENTLOG_SUCCESS);
			serviceDaemon->Start();
		}catch(std::exception &e){
			//ReportEvent(hEventSource, EVENTLOG_ERROR_TYPE, 0, 0, NULL, 2, 0, &ERROR_STARTING, NULL);
			LOG(e.what(), strlen(e.what()), EVENTLOG_ERROR_TYPE);
			goto EXIT;
		}
		//  Periodically check if the service has been requested to stop
		for (;WaitForSingleObject(g_ServiceStopEvent, 0) != WAIT_OBJECT_0; Sleep(5000)); 

		try{
			//ReportEvent(hEventSource, EVENTLOG_SUCCESS, 0, 0, NULL, 2, 0, &STOPPING_MESSAGE, NULL);
			const char * msg = "Starting service";
			LOG(msg, strlen(msg), EVENTLOG_SUCCESS);
			serviceDaemon->Stop();
		}catch(std::exception &e){
			LOG(e.what(), strlen(e.what()), EVENTLOG_ERROR_TYPE);
			//ReportEvent(hEventSource, EVENTLOG_ERROR_TYPE, 0, 0, NULL, 2, 0, &ERROR_STARTING, NULL);
		}
	    
EXIT:
		if (hEventSource) 
			DeregisterEventSource(hEventSource);

		// Perform any cleanup tasks 
		CloseHandle (g_ServiceStopEvent);
	 
		// Tell the service controller we are stopped
		g_ServiceStatus.dwControlsAccepted = 0;
		g_ServiceStatus.dwCurrentState = SERVICE_STOPPED;
		g_ServiceStatus.dwWin32ExitCode = 0;
		g_ServiceStatus.dwCheckPoint = 3;
	 
		SetServiceStatus (g_StatusHandle, &g_ServiceStatus);
	} 
}

int RunService(Daemon *daemon){

	SERVICE_TABLE_ENTRY ServiceTable[] = 
	{
		{SERVICE_NAME, (LPSERVICE_MAIN_FUNCTION) ServiceMain},
		{NULL, NULL}
	};

	serviceDaemon = daemon;

	hEventSource = RegisterEventSource(NULL, TEXT(SERVICE_NAME));

	if (!StartServiceCtrlDispatcher (ServiceTable)){
		return GetLastError ();
	}
	return 0;
}

//
// Purpose: 
//   Installs a service in the SCM database
//
// Parameters:
//   None
// 
// Return value:
//   None
//
void InstallService(){

	SC_HANDLE schSCManager;
    SC_HANDLE schService;
    TCHAR szPath[MAX_PATH];

    if( !GetModuleFileName( NULL, szPath, MAX_PATH ) ){
        printf("Cannot install service (%d)\n", GetLastError());
        return;
    }

    // Get a handle to the SCM database. 
    schSCManager = OpenSCManager(NULL, NULL, SC_MANAGER_ALL_ACCESS);  // full access rights 
 
    if (!schSCManager) {
        printf("OpenSCManager failed (%d)\n", GetLastError());
        return;
    }

    // Create the service
    schService = CreateService( 
        schSCManager,              // SCM database 
        SERVICE_NAME,              // name of service 
        SERVICE_NAME,              // service name to display 
        SERVICE_ALL_ACCESS,        // desired access 
        SERVICE_WIN32_OWN_PROCESS, // service type 
        SERVICE_DEMAND_START,      // start type 
        SERVICE_ERROR_NORMAL,      // error control type 
        szPath,                    // path to service's binary 
        NULL,                      // no load ordering group 
        NULL,                      // no tag identifier 
        NULL,                      // no dependencies 
        NULL,                      // LocalSystem account 
        NULL);                     // no password 
 
    if (!schService) {
        printf("CreateService failed (%d)\n", GetLastError()); 
        CloseServiceHandle(schSCManager);
        return;
    }else printf("Service installed successfully\n"); 

    CloseServiceHandle(schService); 
    CloseServiceHandle(schSCManager);
}

void UninstallService(){
}