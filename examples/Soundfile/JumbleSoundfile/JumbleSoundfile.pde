/**
 * First load a sample sound file from disk, then start manipulating it using the
 * low-level data access functions provided by AudioSample.
 * With every mouseclick, two random 1 second chunks of the sample are swapped around
 * in their position. The sample always stays the same length and it keeps on looping,
 * but the more often you do random swaps, the more the original soundfile gets cut up
 * into smaller and smaller portions that seem to be resampled at random.
 */

import processing.sound.*;

SoundFile file;

void setup() {
  size(640, 360);
  background(255);

  // Load a soundfile and start playing it on loop
  file = new SoundFile(this, "beat.aiff");
  file.loop();
}      


void draw() {
}

void mousePressed() {
  // Every time the mouse is pressed, take two random 1 second chunks of the sample
  // and swap them around.

  int partOneStart = int(random(file.frames()));
  int partTwoOffset = int(random(file.frames() - file.sampleRate()));
  // Offset part two by at least one second
  int partTwoStart = partOneStart + file.sampleRate() + partTwoOffset;
  // Make sure the start of the second sample part is not past the end of the file.
  partTwoStart = partTwoStart % file.frames();

  // Read one second worth of frames from each position
  float[] partOne = new float[file.sampleRate()];
  float[] partTwo = new float[file.sampleRate()];
  file.read(partOneStart, partOne, 0, partOne.length);
  file.read(partTwoStart, partTwo, 0, partTwo.length);
  // And write them back the other way around
  file.write(partOneStart, partTwo, 0, partTwo.length);
  file.write(partTwoStart, partOne, 0, partOne.length);
}
