#!/bin/bash 

gcc -mno-cygwin -D__int64="long long" -o jasiohost.dll -shared -O3 -w -Wl,--add-stdcall-alias \
-I. \
-I/cygdrive/c/Java/jdk1.6.0_12/include \
-I/cygdrive/c/Java/jdk1.6.0_12/include/win32 \
-I./ASIOSDK2/common \
-I./ASIOSDK2/host \
-I./ASIOSDK2/host/pc \
./*.cpp \
./ASIOSDK2/common/*.cpp \
./ASIOSDK2/host/*.cpp \
./ASIOSDK2/host/pc/*.cpp \
-lstdc++ -lole32 -luuid

ls -l jasiohost.dll
echo copying jasiohost.dll to C:/WINDOWS/system32
cp jasiohost.dll /cygdrive/c/WINDOWS/system32
mv jasiohost.dll ../../../../