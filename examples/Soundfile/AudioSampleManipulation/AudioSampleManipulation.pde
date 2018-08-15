/**
 * Allocate a new audio sample and manually fill it with a sine wave that gets
 * scrambled every time the mouse is pressed. As the order of data points is scrambled
 * more and more, the original sine wave signal becomes less and less audible until it
 * is completely washed out by noise artefacts.
 */

import processing.sound.*;

AudioSample sample;

void setup() {
  size(640, 360);
  background(255);

  // Manually write a sine wave oscillations into an array.
  int resolution = 1000;
  float[] sinewave = new float[resolution];
  for (int i = 0; i < resolution; i++) {
    sinewave[i] = sin(TWO_PI*i/resolution);
  }

  // Initialize the audiosample, set framerate to play 200 oscillations/second
  sample = new AudioSample(this, sinewave, 500 * resolution);

  // Play the sample in a loop (but don't make it too loud)
  sample.amp(0.2);
  sample.loop();
}      


void draw() {
}

void mousePressed() {
  // Every time the mouse is pressed, swap two of the sample frames around.
  int i = int(random(0, sample.frames()));
  int j = int(random(0, sample.frames()));

  // Read a frame each from their respective positions
  float onevalue = sample.read(i);
  float othervalue = sample.read(j);
  // and write them back the other way around
  sample.write(i, othervalue);
  sample.write(j, onevalue);
}
