/**
 * This sketch shows how to use the PitchDetector class to detect the pitch 
 * (also known as the 'fundamental frequency') of a sound signal. For complex 
 * signals this is not a trivial task, so the analyzer only returns a frequency 
 * (measured in Hertz, or 'Hz') when its measurement exceeds a 'confidence level'
 * that can be specified by the user.
 */

import processing.sound.*;

PitchDetector pitchDetector;
int i;

void setup() {
  size(640, 320);
  background(0);

  pitchDetector = new PitchDetector(this);
  // capture audio input
  pitchDetector.input(new AudioIn(this, 0));
}

void draw() {
  // clear column
  noStroke();
  fill(color(0));
  rect(i, 0, 1, height);

  // require a minimum confidence level based on current mouse position
  float confidence = map(mouseY, 0, height, 1.0, 0.0);
  float frequency = pitchDetector.analyze(confidence);

  // did we get a reading that had the required confidence level?
  if (frequency != 0.0) {
    // map the range of the human voice (40 - 1000Hz) to the height of the 
    // sketch, and color the dot according to confidence that was required
    // for this measurement
    fill(lerpColor(color(255, 0, 0), color(0, 255, 0), confidence));
    circle(i, int(map(frequency, 1000, 40, 0, height)), 4);
  }

  i = (i+1) % width;
}
