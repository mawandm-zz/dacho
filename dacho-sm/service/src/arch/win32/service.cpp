/*
Reference:
http://msdn.microsoft.com/en-us/library/bb540476%28v=VS.85%29.aspx
http://www.codeproject.com/Articles/499465/Simple-Windows-Service-in-Cplusplus
*/

#include "service.h"
#include <windows.h>

namespace{
	SERVICE_STATUS        g_ServiceStatus = {0};
	SERVICE_STATUS_HANDLE g_StatusHandle = NULL;
	HANDLE                g_ServiceStopEvent = INVALID_HANDLE_VALUE;
	HANDLE  hEventSource;
	Daemon *serviceDaemon;

	VOID WINAPI ServiceCtrlHandler (DWORD CtrlCode)
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
	 
			if (!SetServiceStatus (g_StatusHandle, &g_ServiceStatus))
				OutputDebugString("My Sample Service: ServiceCtrlHandler: SetServiceStatus returned error");
	 
			// This will signal the worker thread to start shutting down
			SetEvent (g_ServiceStopEvent);
			break;
		 default:
			 break;
		}
	}

	VOID WINAPI ServiceMain (DWORD argc, LPTSTR *argv)
	{
		DWORD Status = E_FAIL;
		const char * msg = "Starting service";
	 
		// Register our service control handler with the SCM
		g_StatusHandle = RegisterServiceCtrlHandler (SERVICE_NAME, ServiceCtrlHandler);

		if (!g_StatusHandle)
			return;

		// Tell the service controller we are starting
		g_ServiceStatus.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
		g_ServiceStatus.dwCurrentState = SERVICE_START_PENDING;

		if (!SetServiceStatus (g_StatusHandle , &g_ServiceStatus)){
			OutputDebugString("My Sample Service: ServiceMain: SetServiceStatus returned error");
		}
	 
		// Create a service stop event to wait on later
		g_ServiceStopEvent = CreateEvent (NULL, TRUE, FALSE, NULL);
		
		if (g_ServiceStopEvent == NULL){
			g_ServiceStatus.dwControlsAccepted = 0;
			g_ServiceStatus.dwCurrentState = SERVICE_STOPPED;
			g_ServiceStatus.dwWin32ExitCode = GetLastError();
			g_ServiceStatus.dwCheckPoint = 1;
	 
			if (!SetServiceStatus (g_StatusHandle, &g_ServiceStatus)){
				OutputDebugString("My Sample Service: ServiceMain: SetServiceStatus returned error");
			}
			return; 
		}    
	    
		// Tell the service controller we are started
		g_ServiceStatus.dwControlsAccepted = SERVICE_ACCEPT_STOP;
		g_ServiceStatus.dwCurrentState = SERVICE_RUNNING;
		g_ServiceStatus.dwWin32ExitCode = 0;
		g_ServiceStatus.dwCheckPoint = 0;
	 
		if (!SetServiceStatus (g_StatusHandle, &g_ServiceStatus)){
			msg = "My Sample Service: ServiceMain: SetServiceStatus returned error";
			ReportEvent(hEventSource, EVENTLOG_ERROR_TYPE, 0, 0, NULL, 2, 0, &msg, NULL);                // no raw data
			return;
		}
	 
		
		ReportEvent(hEventSource, EVENTLOG_SUCCESS, 0, 0, NULL, 2, 0, &msg, NULL);                // no raw data

		try{
			serviceDaemon->Start();
		}catch(std::exception &e){
			msg = "My Sample Service: ServiceMain: SetServiceStatus returned error when starting";
			ReportEvent(hEventSource, EVENTLOG_ERROR_TYPE, 0, 0, NULL, 2, 0, &msg, NULL);                // no raw data
			goto EXIT;
		}
		//  Periodically check if the service has been requested to stop
		for (;WaitForSingleObject(g_ServiceStopEvent, 0) != WAIT_OBJECT_0; Sleep(5000)); 

		try{
			serviceDaemon->Stop();
		}catch(std::exception &e){
			OutputDebugString("My Sample Service: ServiceMain: SetServiceStatus returned error when stopping");
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
	 
		if (!SetServiceStatus (g_StatusHandle, &g_ServiceStatus)){
			OutputDebugString("My Sample Service: ServiceMain: SetServiceStatus returned error");
		}
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