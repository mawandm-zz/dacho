CFLAGS = -I/usr/lib/jvm/java-7-openjdk-amd64/include/
LDFLAGS = -L/usr/lib/jvm/java-7-openjdk-amd64/jre/lib/amd64/server/

build_dir=../build/unix/obj
objects = \
$(build_dir)/dachojvm.o \
$(build_dir)/service.o \
$(build_dir)/daemon.o \
$(build_dir)/config.o \
$(build_dir)/module.o \
$(build_dir)/main.o

output = $(build_dir)/../dacho

CC=g++ -Wextra -Wall -g

all: build_dir_rule dachojvm.o service.o daemon.o module.o config.o main.o
	$(CC) $(LDFLAGS) -o $(output) $(objects) -ljvm
dachojvm.o: ../src/dachojvm.h
	$(CC) -c $(CFLAGS) -o $(build_dir)/dachojvm.o ../src/dachojvm.cpp
service.o: ../src/service.h
	$(CC) -c $(CFLAGS) -o $(build_dir)/service.o ../src/arch/linux/service.cpp
module.o: ../src/module.h
	$(CC) -c $(CFLAGS) -o $(build_dir)/module.o ../src/arch/linux/module.cpp
daemon.o: ../src/daemon.h
	$(CC) -c $(CFLAGS) -o $(build_dir)/daemon.o ../src/daemon.cpp
config.o: ../src/config.h
	$(CC) -c $(CFLAGS) -o $(build_dir)/config.o ../src/config.cpp
main.o:
	$(CC) -c $(CFLAGS) -o $(build_dir)/main.o ../src/main.cpp

build_dir_rule:
	if [ ! -d $(build_dir) ] ; then \
		mkdir $(build_dir) ; \
	fi

.PHONY: clean
clean:
	rm $(objects) $(output)