/**
 * In this example, a WhiteNoise generator (equal amount of noise at all frequencies) is
 * passed through a BandPass filter. You can control both the central frequency
 * (left/right) as well as the bandwidth of the filter (up/down) with the mouse. The
 * position and size of the circle indicates how much of the noise's spectrum passes
 * through the filter, and at what frequency range.
 */

import processing.sound.*;

WhiteNoise noise;
BandPass filter;

void setup() {
  size(640, 360);

  // Create the noise generator + Filter
  noise = new WhiteNoise(this);
  filter = new BandPass(this);

  noise.play(0.5);
  filter.process(noise);
}      

void draw() {
  // Map the left/right mouse position to a cutoff frequency between 20 and 10000 Hz
  float frequency = map(mouseX, 0, width, 20, 10000);
  // And the vertical mouse position to the width of the band to be passed through
  float bandwidth = map(mouseY, 0, height, 1000, 100);

  filter.freq(frequency);
  filter.bw(bandwidth);

  // Draw a circle indicating the position + width of the frequency window
  // that is allowed to pass through
  background(125, 255, 125);
  noStroke();
  fill(255, 0, 150);
  ellipse(mouseX, height, 2*(height - mouseY), 2*(height - mouseY));
}
