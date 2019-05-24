/**
 * Inspect the frequency spectrum of different types of noise.
 */

import processing.sound.*;

// All noise generators are instances of the Noise superclass.
Noise noises[] = new Noise[3];

// Store information on which of the noises is currently playing.
int current = 0;

FFT fft;
int fftBands = 512;

void setup() {
  size(640, 360);
  background(255);

  // Turn the volume down globally.
  Sound s = new Sound(this);
  s.volume(0.2);

  // Create the noise generators and put them into an array.
  noises[0] = new WhiteNoise(this);
  noises[1] = new PinkNoise(this);
  noises[2] = new BrownNoise(this);

  // Initialise the FFT and start playing the (default) noise generator.
  fft = new FFT(this, 512);
  noises[current].play();
  fft.input(noises[current]);
}

void draw() {
  // Only play one of the four oscillators, based on mouseY
  int nextNoise = constrain(floor(map(mouseY, 0, height, 0, noises.length)), 0, noises.length - 1);

  if (nextNoise != current) {
    noises[current].stop();
    current = nextNoise;

    // Switch FFT analysis over to the newly selected noise generator.
    fft.input(noises[current]);
    // Start playing new noise
    noises[current].play();
  }

  // Draw frequency spectrum.
  background(125, 255, 125);
  fill(255, 0, 150);
  noStroke();

  fft.analyze();

  float r_width = width/float(fftBands);

  for (int i = 0; i < fftBands; i++) {
    rect( i*r_width, height, r_width, -fft.spectrum[i]*height);
  }

  // Display the name of the noise generator class.
  textSize(32);
  fill(0);
  float verticalPosition = map(current, -1, noises.length, 0, height);
  text(noises[current].getClass().getSimpleName(), 0, verticalPosition);
}
