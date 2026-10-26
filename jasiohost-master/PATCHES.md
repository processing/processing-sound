# Local JAsioHost patches

The prebuilt `jasiohost64.dll` in this directory (and in
`library/windows-amd64/`) is built from the sources in this repository plus the
Steinberg ASIO SDK. The SDK sources are **not** committed because of the
Steinberg license.

## ASIO SDK version

The DLL is built against **ASIO SDK 2.3**. The code in SDK 2.3.4 is identical
to 2.3; 2.3.4 only changed the license headers (and introduced a GPLv3
alternative), so 2.3 is used to keep binary distribution under the classic
Steinberg ASIO license.

Get the SDK from <https://www.steinberg.net/asiosdk> and extract it somewhere
outside this repository.

## Patches against the vendored JAsioHost sources

### `src/com/synthbot/jasiohost/JAsioHost.cpp`

`ASIOCreateBuffers` used a variable-length array
(`ASIOSampleType sampleTypes[bufferVars.numInitedChannels]`), which is a GCC
extension and does not compile with MSVC. It is replaced with
`std::vector<ASIOSampleType>`. No behaviour change.

## Patches against the ASIO SDK

### `host/pc/asiolist.cpp`

The stock Windows enumeration only reads
`HKLM\SOFTWARE\ASIO` in the native registry view. Drivers that are registered
only in the 32-bit view (`HKLM\SOFTWARE\WOW6432Node\ASIO`, written by 32-bit
installers) or per-user (`HKCU\Software\ASIO`) are invisible to a 64-bit host,
even when they ship a 64-bit driver DLL. The rewritten enumerator:

* scans `HKLM\SOFTWARE\ASIO` in both the 64-bit and 32-bit views (64-bit host)
  and `HKCU\Software\ASIO`;
* expands environment variables in `InprocServer32` paths;
* accepts CLSID values with or without braces;
* verifies that the `InprocServer32` DLL exists, is a PE image for the host
  architecture and exports `DllGetClassObject` (it is an in-process COM
  server) before advertising the driver;
* deduplicates entries by CLSID and by driver name;
* never aborts the scan because of a broken entry.

The rewritten `asiolist.cpp` is a derivative of the Steinberg SDK, so it is
kept in the local build directory (`.build-deps/build/asiolist_win.cpp`, which
is git-ignored) and not committed. Set `JASIOHOST_DEBUG=1` before launching
the JVM to print the scan report and the rejection reason for each entry to
`stderr`.

## Rebuilding `jasiohost64.dll`

Prerequisites:

* Visual Studio 2019 Build Tools (or newer) with the x64 C++ toolset
* A 64-bit JDK (for `jni.h`)
* ASIO SDK 2.3 extracted somewhere

Open a `x64 Native Tools Command Prompt` and run, from
`jasiohost-master/src/com/synthbot/jasiohost`:

```bat
cl /nologo /LD /MT /O2 /EHsc /D_CRT_SECURE_NO_WARNINGS /D_CRT_NONSTDC_NO_DEPRECATE ^
  /I. /I"%ASIOSDK%\common" /I"%ASIOSDK%\host" /I"%ASIOSDK%\host\pc" ^
  /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32" ^
  JAsioHost.cpp iasiothiscallresolver.cpp ^
  "%ASIOSDK%\common\asio.cpp" "%ASIOSDK%\host\asiodrivers.cpp" ^
  patched-asiolist.cpp ^
  /Fe:jasiohost64.dll /link ole32.lib uuid.lib advapi32.lib user32.lib
```

`patched-asiolist.cpp` is the patched copy of `%ASIOSDK%\host\pc\asiolist.cpp`
described above. Do not copy the ASIO SDK into the repository.

Verify the result with `dumpbin /exports jasiohost64.dll`: the 20
`Java_com_synthbot_jasiohost_AsioDriver_*` exports must be present and the DLL
must report `8664 machine (x64)`.

Only `jasiohost64.dll` was rebuilt. `jasiohost32.dll` is still the original
2012 build; Processing 4 runs on a 64-bit JVM, so the 32-bit DLL is not used.
