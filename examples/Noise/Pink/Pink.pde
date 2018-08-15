/**
 * This is a simple pink noise generator. The energy of pink noise falls off at 3 dB
 * per octave, which puts it somewhere between White and Brownian noise.
 */

import processing.sound.*;

PinkNoise noise;

void setup() {
  size(640, 360);
  background(255);

  // Create and start noise generator
  noise = new PinkNoise(this);
  noise.play();
}      

void draw() {
  // Map mouseX from -1.0 to 1.0 for left to right
  noise.pan(map(mouseX, 0, width, -1.0, 1.0));

  // Map mouseY from 0.0 to 0.5 for amplitude
  // (the higher the mouse position, the louder the sound)
  noise.amp(map(mouseY, 0, height, 0.5, 0.0));
}
