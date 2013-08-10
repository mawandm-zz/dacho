class Daemn{
DachoJVM dachoJVM;
Daemon(const Daemon &daemon);
Daemon & operator=(const Daemon &other);

public:
void Start();
void Stop();
~Daemon() throw();
Daemon(std::map<std::string, std::string> &properties);
};