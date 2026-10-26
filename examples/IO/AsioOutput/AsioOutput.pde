/**
 * Play a different note on every output channel of an ASIO audio interface,
 * bypassing the Windows audio stack for the lowest possible latency.
 *
 * ASIO support is powered by the JAsioHost bindings and has to be enabled with
 * MultiChannel.useJASIO() before any other Sound library call. After the
 * switch, all ASIO drivers registered in the system are listed in the console.
 * This is a Windows-only feature: on other operating systems (or if no ASIO
 * driver is installed) useJASIO() prints an error message and the sketch
 * continues with the default audio drivers, so the same sketch runs everywhere.
 *
 * Tip: if you don't have a driver from your audio interface manufacturer
 * installed, try the universal ASIO4ALL driver.
 */

import processing.sound.*;
import com.synthbot.jasiohost.AsioDriver;
import java.util.List;

SinOsc[] sines;
int[] notes;
float[] frequencies;
boolean usingASIO;

// Optional: select a specific ASIO driver by name, exactly as printed by the
// list below. Leave this empty to use the default ASIO output device.
// Example: String asioDriverName = "Focusrite USB ASIO";
String asioDriverName = "";

// Base note (MIDI note number) and scale used to spread different notes over
// the output channels. 48 = C3, 60 = C4.
int baseMidiNote = 48;
final int[] SCALE = {0, 2, 4, 7, 9}; // major pentatonic
final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

void setup() {
  size(640, 360);
  background(255);

  // has to be the very first Sound library call of the sketch, before any
  // other Sound library objects are created or devices are selected
  usingASIO = MultiChannel.useJASIO();

  if (usingASIO) {
    // First list all registered ASIO drivers. We only read the names from the
    // registry here: opening every driver one after another can crash some
    // manufacturer ASIO drivers.
    List<String> asioDrivers = AsioDriver.getDriverNames();
    println("Available ASIO drivers:");
    for (String name : asioDrivers) {
      println("  " + name);
    }

    // Then select the driver you want to use (see asioDriverName above).
    // If that driver is not installed/registered, fall back to the first one.
    if (!asioDrivers.isEmpty()) {
      try {
        if (asioDriverName.length() > 0) {
          if (!asioDrivers.contains(asioDriverName)) {
            asioDriverName = asioDrivers.get(0);
            println("ASIO driver not found, falling back to '" + asioDriverName + "'");
          }
          Sound.outputDevice(asioDriverName);
        } else {
          Sound.defaultOutputDevice();
        }
      } catch (RuntimeException e) {
        println("Could not open ASIO output device: " + e.getMessage());
      }
    }
  }

  int channels = MultiChannel.availableChannels();
  if (channels <= 0) {
    println("No output channels available.");
    return;
  }

  println("Playing a different note on each of the " + channels + " output channels"
      + (usingASIO ? " of '" + asioDriverName + "'" : " of the default output device"));

  sines = new SinOsc[channels];
  notes = new int[channels];
  frequencies = new float[channels];

  for (int i = 0; i < channels; i++) {
    // route the next oscillator to output channel i
    MultiChannel.activeChannel(i);

    sines[i] = new SinOsc(this);
    sines[i].amp(0.15f);
    sines[i].play();
  }

  // leave multi-channel routing mode so any sounds created later use the
  // normal stereo layout again; already-playing sines keep their channels
  MultiChannel.activeChannel(-1);

  updateFrequencies();
  printChannelNotes();

  textSize(12);
  textAlign(CENTER, BOTTOM);
}

void draw() {
  if (sines == null || sines.length == 0) {
    return;
  }

  // transpose all notes together with the mouse
  baseMidiNote = 36 + int(map(mouseX, 0, width, 0, 24));
  updateFrequencies();

  background(255);
  drawChannelBars();

  fill(0);
  textSize(16);
  textAlign(CENTER, TOP);
  text(sines.length + " ASIO output channels", width / 2, 10);
}

void updateFrequencies() {
  for (int i = 0; i < sines.length; i++) {
    int octave = i / SCALE.length;
    int note = baseMidiNote + SCALE[i % SCALE.length] + 12 * octave;
    notes[i] = note;
    frequencies[i] = midiToFrequency(note);
    sines[i].freq(frequencies[i]);
  }
}

void printChannelNotes() {
  for (int i = 0; i < notes.length; i++) {
    println("  channel " + i + ": " + noteName(notes[i]) + " (" + nf(frequencies[i], 1, 2) + " Hz)");
  }
}

void drawChannelBars() {
  float margin = 20;
  float barWidth = (width - 2 * margin) / sines.length;
  float minFrequency = midiToFrequency(24);
  float maxFrequency = midiToFrequency(96);

  for (int i = 0; i < sines.length; i++) {
    float x = margin + i * barWidth;
    float h = map(frequencies[i], minFrequency, maxFrequency, 20, height - 80);
    fill(0, 120, 255);
    rect(x + 2, height - 40 - h, barWidth - 4, h);

    fill(0);
    textSize(10);
    textAlign(CENTER, BOTTOM);
    text(i, x + barWidth / 2, height - 10);

    textSize(11);
    text(noteName(notes[i]), x + barWidth / 2, height - 45);
  }
}

float midiToFrequency(int note) {
  return (float) (440.0 * Math.pow(2.0, (note - 69) / 12.0));
}

String noteName(int note) {
  return NOTE_NAMES[note % 12] + (note / 12 - 1);
}
