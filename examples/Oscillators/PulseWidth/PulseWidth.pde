/**
 * This is a pulse-wave oscillator. On top of the sound frequency, which you can also
 * set for all other oscillators, the pulse wave also allows you to adjust the pulse's
 * relative width, using the .width() method. If you want to set all oscillator
 * parameters at the same time you can use
 * .set(float freq, float width, float amp, float add, float pan)
 */

import processing.sound.*;

Pulse pulse;

void setup() {
  size(640, 360);
  background(255);

  // Create and start the pulse wave oscillator
  pulse = new Pulse(this);
  // pulse waves can appear very loud to the human ear, so make it a bit more quiet
  pulse.amp(0.3);
  pulse.play();
}

void draw() {
  // Map mouseX from 20Hz to 500Hz for frequency
  float frequency = map(mouseX, 0, width, 20.0, 500.0);
  pulse.freq(frequency);
  // Map mouseY from 0.0 to 1.0 for the relative width of the pulse.
  float pulseWidth = map(mouseY, 0, height, 0.0, 1.0);
  pulse.width(pulseWidth);
}
