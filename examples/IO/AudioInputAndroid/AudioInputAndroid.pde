/**
 * Grab audio from the microphone input and draw a circle whose size
 * is determined by how loud the audio input is.
 * 
 * The only difference between this example and AudioInput is that
 * Android requires requesting the permission RECORD_AUDIO.
 */

import processing.sound.*;

AudioIn input;
Amplitude loudness;

void setup() {
  size(640, 360);
  background(255);

  if (hasPermission("android.permission.RECORD_AUDIO")) {
    initialize(true);
  } else {
    requestPermission("android.permission.RECORD_AUDIO", "initialize");
  }
}

void initialize(boolean granted) {
  if (!granted) {
    return;
  }

  // Create an Audio input and grab the 1st channel
  input = new AudioIn(this, 0);

  // Begin capturing the audio input
  input.start();
  // start() activates audio capture so that you can use it as
  // the input to live sound analysis, but it does NOT cause the
  // captured audio to be played back to you. if you also want the
  // microphone input to be played back to you, call
  //    input.play();
  // instead (be careful with your speaker volume, you might produce
  // painful audio feedback. best to first try it out wearing headphones!)

  // Create a new Amplitude analyzer
  loudness = new Amplitude(this);

  // Patch the input to the volume analyzer
  loudness.input(input);
}

void draw() {
  if (input == null) {
    // Wait for user to give permission
    return;
  }

  // Adjust the volume of the audio input based on mouse position
  float inputLevel = map(mouseY, 0, height, 1.0, 0.0);
  input.amp(inputLevel);

  // loudness.analyze() return a value between 0 and 1. To adjust
  // the scaling and mapping of an ellipse we scale from 0 to 0.5
  float volume = loudness.analyze();
  int size = int(map(volume, 0, 0.5, 1, 350));

  background(125, 255, 125);
  noStroke();
  fill(255, 0, 150);
  // We draw a circle whose size is coupled to the audio analysis
  ellipse(width/2, height/2, size, size);
}
