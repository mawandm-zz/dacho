#ifndef _DAEMON_H
#define _DAEMON_H

#include <vector>
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
	Daemon(std::vector<std::string> &v_config);
};

#endif
