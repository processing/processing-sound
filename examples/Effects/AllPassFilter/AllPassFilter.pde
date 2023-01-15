/**
 * This is a simple WhiteNoise generator, run through a LowPass filter which only lets
 * the lower frequency components of the noise through. The cutoff frequency of the
 * filter can be controlled through the left/right position of the mouse.
 */

import processing.sound.*;

PinkNoise noise;
AllPass allPass;

void setup() {
  size(640, 360);

  // Create the noise generator + filter
  noise = new PinkNoise(this);
  allPass = new AllPass(this);

  noise.play(0.5);
  allPass.process(noise);
}      

void draw() {
  // Map the left/right mouse position to a cutoff frequency between 20 and 10000 Hz
  allPass.gain(map(sin(mouseX), -PI, PI, -1., 1.));
}
