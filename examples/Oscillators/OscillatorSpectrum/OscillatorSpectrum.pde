/**
 * Inspect the frequency spectrum of different simple oscillators.
 */

import processing.sound.*;

// All oscillators are instances of the Oscillator superclass.
Oscillator oscs[] = new Oscillator[5];

// Store information on which of the oscillators is currently playing.
int current = 0;

FFT fft;
int fftBands = 512;

void setup() {
  size(640, 360);
  background(255);

  // Turn the volume down globally.
  Sound s = new Sound(this);
  s.volume(0.2);

  // Create the oscillators and put them into an array.
  oscs[0] = new SinOsc(this);
  oscs[1] = new TriOsc(this);
  oscs[2] = new SawOsc(this);
  oscs[3] = new SqrOsc(this);

  // Special treatment for the Pulse oscillator to set its pulse width.
  Pulse pulse = new Pulse(this);
  pulse.width(0.05);
  oscs[4] = pulse;

  // Initialise the FFT and start playing the (default) oscillator.
  fft = new FFT(this, 512);
  oscs[current].play();
  fft.input(oscs[current]);
}

void draw() {
  // Only play one of the four oscillators, based on mouseY
  int nextOscillator = floor(map(mouseY, 0, height, 0, oscs.length));

  if (nextOscillator != current) {
    oscs[current].stop();
    current = nextOscillator;

    // Switch FFT analysis over to the newly selected oscillator.
    fft.input(oscs[current]);
    // Play
    oscs[current].play();
  }

  // Map mouseX from 20Hz to 22000Hz for frequency.
  float frequency = map(mouseX, 0, width, 20.0, 22000.0);
  // Update oscillator frequency.
  oscs[current].freq(frequency);


  // Draw frequency spectrum.
  background(125, 255, 125);
  fill(255, 0, 150);
  noStroke();

  fft.analyze();

  float r_width = width/float(fftBands);

  for (int i = 0; i < fftBands; i++) {
    rect( i*r_width, height, r_width, -fft.spectrum[i]*height);
  }

  // Display the name of the oscillator class.
  textSize(32);
  fill(0);
  float verticalPosition = map(current, -1, oscs.length, 0, height);
  text(oscs[current].getClass().getSimpleName(), 0, verticalPosition);
}
