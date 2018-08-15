/**
 * This is a square wave oscillator. The method .play() starts the oscillator. There
 * are several setter functions for configuring the oscillator, such as .amp(),
 * .freq(), .pan() and .add(). If you want to set all of them at the same time you can
 * use .set(float freq, float amp, float add, float pan)
 */

import processing.sound.*;

SqrOsc sqr;

void setup() {
  size(640, 360);
  background(255);

  // create and start the oscillator.
  sqr = new SqrOsc(this);
  sqr.play();
}

void draw() {
  // Map mouseY from 1.0 to 0.0 for amplitude (mouseY is 0 at the
  // top of the sketch, so the higher the mouse position, the louder)
  float amplitude = map(mouseY, 0, height, 1.0, 0.0);
  sqr.amp(amplitude);

  // Map mouseX from 20Hz to 1000Hz for frequency
  float frequency = map(mouseX, 0, width, 20.0, 1000.0);
  sqr.freq(frequency);

  // Map mouseX from -1.0 to 1.0 for panning the audio to the left or right
  float panning = map(mouseX, 0, width, -1.0, 1.0);
  sqr.pan(panning);
}
