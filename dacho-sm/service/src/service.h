#ifndef _SERVICE_H
#define _SERVICE_H

#include "daemon.h"

#if 0
#if defined(_WIN32)
#include <arch/win32/service.h>
#elif defined(LINUX)
#include <arch/linux/service.h>
#else
#error "Unsupported platform"
#endif
#endif

int RunService(Daemon *daemon);
void InstallService();
void UninstallService();

#endif