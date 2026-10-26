# About JAsioHost
JAsioHost (JAH) is a Java interface to Steinberg's [Audio Stream Input/Output](http://en.wikipedia.org/wiki/Audio_Stream_Input/Output) (ASIO) API. It provides low-latecy (< 10ms) input and output access to the available audio hardware on **Windows**, replacing the need to use the outdated and slow Java Sound API.


## Getting Started
JAsioHost comes in two parts, `JAsioHost.jar` and `jasiohost.dll`. The former is the usual encapsulation of the classes comprising the JAH Java library, and the latter is the JAH interface to ASIO. The package of JAH is `com.synthbot.jasiohost`.

The library can be quickly tested from the root directory of the project with `java -jar JAsioHost.jar -Djava.library.path=./`. The `ExampleHost` will launch a small GUI allowing you to select an available ASIO driver and use it to play a 440Hz tone. If you're not sure if you have the correct ASIO driver installed (usually soundcard-specific), then try using the excellent [ASIO4ALL](http://www.asio4all.com/) Universal ASIO Driver For WDM Audio.

+ Include `JAsioHost.jar` in your Java project.
+ Make `jasiohost.dll` available to your project. This can be done in several ways:
  + Move or copy the library to `C:\WINDOWS\system32`. This is the default search location for JNI libraries.
  + Inform the JVM where the library is located. This can be done with, e.g. `java -Djava.library.path=C:\WINDOWS\system32`

If the JVM cannot find the dll, an [UnsatisfiedLinkError](http://docs.oracle.com/javase/1.4.2/docs/api/java/lang/UnsatisfiedLinkError.html) exception will be thrown.


## Example
The basic design pattern for using `JAsioHost` is as follows. `static` methods in `AsioDriver` are used to collect information about the available drivers. `getDriver` is called in order to load and instantiate a given driver. The `AsioDriver` can then be queried for channel state information. Audio buffers are created using `createBuffers`, before `start` is called. Callbacks are made from the driver to registered `AsioDriverListener` objects in order to submit input and retrieve output.

```Java
// get a list of available ASIO drivers
List<String> driverNameList = AsioDriver.getDriverNames();

// load the names ASIO driver
AsioDriver asioDriver = AsioDriver.getDriver(driverNameList.get(0));

// add an AsioDriverListener in order to receive callbacks from the driver
asioDriver.addAsioDriverListener(new AsioDriverListener() {
  @Override
  public void bufferSwitch(long systemTime, long samplePosition, Set<AsioChannel> channels) {
    // fill in audio buffers here
  }
  
  // implement remaining AudioDriverListener interface functions
}

// create a Set of AsioChannels, defining which input and output channels will be used
Set<AsioChannel> activeChannels = new HashSet<AsioChannel>();

// configure the ASIO driver to use the given channels
activeChannels.add(asioDriver.getChannelOutput(0));
activeChannels.add(asioDriver.getChannelOutput(1));

// create the audio buffers and prepare the driver to run
asioDriver.createBuffers();

// start the driver
asioDriver.start();
try {
  Thread.sleep(1000);
} catch (InterruptedException ie) {
  ie.printStackTrace(System.err);
}

// tear everything down
AsioDriver.shutdownAndUnloadDriver();
```

See the [ExampleHost](https://github.com/mhroth/jasiohost/blob/master/src/com/synthbot/jasiohost/ExampleHost.java) for more details. It also contains a `main` function with a minimum GUI for selecting available drivers.

Note that you can only load one ASIO driver at time. This is a limitation of the original API (AFAIK).


## Note on Compilation
If you are brave enough to try to compile the native component, please note the following helpful tips:

* I use [Cygwin](http://www.cygwin.com/) to compile the library, and not Visual Studio. Because I am more familiar with the Unix toolchain. This makes some things more difficult. Unfortunate, but so be it.
* You must [download](http://www.steinberg.net/en/company/developer.html) your own copy of the ASIO library. It cannot be distributed here due to licensing restrictions by Steinberg.

In the ASIO library,
* `./common/asiodrvr.cpp` is not necessary. Rename or remove it.
* `./common/dllentry.cpp` is not necessary. Rename or remove it.
* Line 219 of `./common/combase.h`, `#if WINVER < 0x0501`, should be replaced with `#if 0`. See [here](http://osdir.com/ml/audio.portaudio.devel/2006-09/msg00058.html) for more information.

The Windows DLL currently shipped with Processing Sound is built with MSVC,
not Cygwin, and uses a patched driver enumerator. See
[PATCHES.md](PATCHES.md) for the exact changes and build command.


## A Note about API Translation
The `JAsioHost` API does not strictly reflect the original C++ API written by Steinberg. There are two reasons for this. The first is that some elements of the original API do not translate well due to language semantics. The second is that I felt that some things could be improved or simplified.

An example of the former is that the native API refers to an audio buffer by using a `void` pointer. Java does not allow arrays to be referenced opaquely. `JAsioHost` therefore encapsulates an audio buffer by using a `ByteBuffer`, which exposes the raw bytes of the native buffer, but also allows other types to be read and written with relative ease.

An example of the latter is the absence of the `ASIOBufferInfo` structure in Java. I simply added a reference to the audio buffer to the active `AsioChannel` objects. As the audio buffer conceptually belongs to a channel, this seemed to make sense, and also remove a superfluous class.


## License
JAsioHost is released under the [Lesser Gnu Public License](http://www.gnu.org/licenses/lgpl.html) (LGPL). Basically, the library stays open source, but you can use if for whatever you want, including closed source applications. You must publicly credit the use of this library.


## Acknowledgements
Many thanks to the following people for making JAsioHost possible:
* [Steinberg](http://www.steinberg.net/en/home.html) for releasing ASIO and making good documentation! Of course the ASIO API belongs to them, and if you want to be able to compile the native source then you will need to [get the ASIO component](http://www.steinberg.net/en/company/3rd_party_developer.html) from them.
* This project depends on [IASIOThisCallResolver](http://www.audiomulch.com/~rossb/code/calliasio/). Without it, this project would not be possible. All hail IASIOThisCallResolver. P.S. The reason for this is that I use Cygwin, gcc, MinGW in order to build the native component, not Visual Studio.
* Steve Taylor of [http://toot.org.uk](http://toot.org.uk) for his help in testing out various ASIO drivers and assisting in the debugging process.
* Carl Janson of [HD Sound Lab](hdsoundlab.com) for his efforts in compiling a 64-bit version of the native library.
