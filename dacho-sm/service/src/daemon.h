#ifndef _DAEMON_H
#define _DAEMON_H

#include <map>
#include <string>
#include "dachojvm.h"

class Daemon{
	DachoJVM dachoJVM;
	Daemon(const Daemon &daemon);
	Daemon & operator=(const Daemon &other);
public:
	void Start();
	void Stop();
	~Daemon() throw();
	Daemon(std::map<std::string, std::string> &properties);
};

#endif
