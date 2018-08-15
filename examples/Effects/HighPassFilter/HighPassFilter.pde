/**
 * This is a simple WhiteNoise generator, run through a HighPass filter which only lets
 * the higher frequency components of the noise through. The cutoff frequency of the
 * filter can be controlled through the left/right position of the mouse.
 */

import processing.sound.*;

WhiteNoise noise;
HighPass highPass;

void setup() {
  size(640, 360);

  // Create the noise generator + filter
  noise = new WhiteNoise(this);
  highPass = new HighPass(this);

  noise.play(0.5);
  highPass.process(noise);
}      

void draw() {
  // Map the left/right mouse position to a cutoff frequency between 10 and 15000 Hz
  float cutoff = map(mouseX, 0, width, 10, 15000);
  highPass.freq(cutoff);

  // Draw a circle indicating the position + width of the frequencies passed through
  background(125, 255, 125);
  noStroke();
  fill(255, 0, 150);
  ellipse(width, height, 2*(width - mouseX), 2*(width - mouseX));
}
