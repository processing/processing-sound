/**
 * Using AudioSample's low-level .read() functions to access the individual
 * samples of a multi-channel (stereo) sound file
 */

import processing.sound.*;

SoundFile soundfile;

void setup() {
  // load a stereo soundfile with out of phase sine waves in the left and right channel
  soundfile = new SoundFile(this, "stereo441hzoutofphase.wav");

  println("This sound file of duration " + soundfile.duration() + " seconds" +
    " contains " + soundfile.frames() + " frames, but because it has " + 
    soundfile.channels() + " channels there are actually " + 
    soundfile.channels() * soundfile.frames() +
    " samples that can be accessed using the read() and write() functions");

  size(662, 300);
  background(0);
  for (int i = 0; i < width; i++) {

    // trying to read() from a channel past the number of frames would
    // throw an exception
    if (i < soundfile.frames()) {
      // read from left channel
      set(i, round(map(soundfile.read(i, 0), -.3, .3, 0, 100)), 255);
      // read from right channel
      set(i, round(map(soundfile.read(i, 1), -.3, .3, 100, 200)), 255);
    }
    // read left-right interleaved data
    set(i, round(map(soundfile.read(i), -.3, .3, 200, 300)), 255);
 }
  stroke(255);
  textSize(14);
  text("the " + soundfile.frames() + " samples of this sound file's LEFT channel, accessed using read(i, 0)", 10, 15);
  text("the " + soundfile.frames() + " samples of this sound file's RIGHT channel, accessed using read(i, 1)", 10, 115);
  text("for stereo files, read(i) returns the left and right channel data together in interleaved order", 10, 215);
}


void draw() {
}
