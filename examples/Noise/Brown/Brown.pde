/**
 * This is a simple "Brown" (also known as "Brownian" or "red") noise generator.
 * Its power decreases by 6dB per octave, giving it a much "softer" quality than
 * white or pink noise.
 */

import processing.sound.*;

BrownNoise noise;

void setup() {
  size(640, 360);
  background(255);

  // Create and start the noise generator
  noise = new BrownNoise(this);
  noise.play();
}      

void draw() {
  // Map mouseX from -1.0 to 1.0 for left to right
  noise.pan(map(mouseX, 0, width, -1.0, 1.0));

  // Map mouseY from 0.0 to 0.3 for amplitude
  // (the higher the mouse position, the louder the sound)
  noise.amp(map(mouseY, 0, height, 0.3, 0.0));
}
