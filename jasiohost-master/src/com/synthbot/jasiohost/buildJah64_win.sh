#!/bin/bash 

x86_64-w64-mingw32-g++ -mno-cygwin -D__int64="long long" -o jasiohost64.dll -static -shared -O3 -W -Wl,--add-stdcall-alias \
-I. \
-I/cygdrive/c/Program\ Files/Java/jdk1.6.0_31/include \
-I/cygdrive/c/Program\ Files/Java/jdk1.6.0_31/include/win32 \
-I./ASIOSDK2/common \
-I./ASIOSDK2/host \
-I./ASIOSDK2/host/pc \
./JAsioHost.cpp \
./ASIOSDK2/common/*.cpp \
./ASIOSDK2/host/*.cpp \
./ASIOSDK2/host/pc/*.cpp \
-lstdc++ -lole32 -luuid

ls -l jasiohost.dll