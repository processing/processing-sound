#!/bin/bash 

gcc -mno-cygwin -D__int64="long long" -o jasiohost.exe -O3 -w -Wl,--add-stdcall-alias \
-I. \
-I/cygdrive/c/Java/jdk1.6.0_12/include \
-I/cygdrive/c/Java/jdk1.6.0_12/include/win32 \
-I. \
-I./ASIOSDK2/common \
-I./ASIOSDK2/host \
-I./ASIOSDK2/host/sample \
-I./ASIOSDK2/host/pc \
./iasiothiscallresolver.cpp \
./ASIOSDK2/common/*.cpp \
./ASIOSDK2/host/*.cpp \
./ASIOSDK2/host/sample/*.cpp \
./ASIOSDK2/host/pc/*.cpp \
-lstdc++ -lole32 -luuid -lwinmm
#-I/usr/include \

ls -l jasiohost.exe