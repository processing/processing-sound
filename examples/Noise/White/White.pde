/**
 * This is a simple white noise generator. White noise has equal power at all
 * frequencies. The high frequencies can make it very grating to the ear.
 */

import processing.sound.*;

WhiteNoise noise;

void setup() {
  size(640, 360);
  background(255);

  // Create and start the noise generator
  noise = new WhiteNoise(this);
  noise.play();
}      

void draw() {
  // Map mouseX from -1.0 to 1.0 for left to right
  noise.pan(map(mouseX, 0, width, -1.0, 1.0));

  // Map mouseY from 0.0 to 0.3 for amplitude
  // (the higher the mouse position, the louder the sound)
  noise.amp(map(mouseY, 0, height, 0.3, 0.0));
}
