import processing.sound.*;

import com.jsyn.engine.SynthesisEngine;

void setup() {

  // print audio device information to the console
  Sound.list();

  // to improve support for USB audio interfaces on Windows, it is possible to 
  // use the PortAudio library, which is however not enabled by default. The 
  // listing above might therefore not have given accurate input/output channel 
  // numbers. The library automatically loads PortAudio drivers when 
  // Sound.outputDevice() is called on a device that it is unable to use 
  // correctly with the default drivers, OR you can always load them explicitly 
  // using MultiChannel.usePortAudio().
  if (MultiChannel.usePortAudio()) {
    // if PortAudio was loaded successfully, the ids and names of the sound 
    // devices (and possibly their number of input/output channels) will have 
    // changed!
    Sound.list();
  }

  // the Sound.status() method prints some general information about the current 
  // memory and CPU usage of the library to the console
  Sound.status();

  // to get programmatic access to the same information (and more), you can get 
  // and inspect the JSyn Synthesizer class yourself:
  SynthesisEngine s = Sound.getSynthesisEngine();
  println("Current CPU usage: " + s.getUsage());

}

