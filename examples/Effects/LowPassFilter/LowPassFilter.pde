/**
 * This is a simple WhiteNoise generator, run through a LowPass filter which only lets
 * the lower frequency components of the noise through. The cutoff frequency of the
 * filter can be controlled through the left/right position of the mouse.
 */

import processing.sound.*;

WhiteNoise noise;
LowPass lowPass;

void setup() {
  size(640, 360);

  // Create the noise generator + filter
  noise = new WhiteNoise(this);
  lowPass = new LowPass(this);

  noise.play(0.5);
  lowPass.process(noise);
}      

void draw() {
  // Map the left/right mouse position to a cutoff frequency between 20 and 10000 Hz
  float cutoff = map(mouseX, 0, width, 20, 10000);
  lowPass.freq(cutoff);

  // Draw a circle indicating the position + width of the frequencies passed through
  background(125, 255, 125);
  noStroke();
  fill(255, 0, 150);
  ellipse(0, height, 2*mouseX, 2*mouseX);
}
