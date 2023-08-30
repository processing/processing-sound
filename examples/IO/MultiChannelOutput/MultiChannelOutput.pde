import processing.sound.*;

SinOsc sines[];

void setup() {
  size(640, 360);
  background(255);
  
  
  boolean foundMultiChannel = false;
  for (int i = 0; i < Sound.list(true).length; i++) {
    if (MultiChannel.availableChannels(i) > 2) {
      println("Found a multi-channel device: " + Sound.list(true)[i]);
      Sound.outputDevice(i);
      foundMultiChannel = true;
      break;
    }
  }
  if (!foundMultiChannel) {
    println("Did not find any output devices with more than 2 channels!");
  }
  
  println("Playing back different sine waves on the " + MultiChannel.availableChannels() + " different channels");
  float freq = 100;
  
  sines = new SinOsc[MultiChannel.availableChannels()];
  
  // loop through all channels and start one sine wave on each
  for (int i = 0; i < sines.length; i++) {
    MultiChannel.activeChannel(i);
    // create and start the sine oscillator.
    sines[i] = new SinOsc(this);
    sines[i].play();
  }
}

void draw() {
  // as long as the oscillators are not stopped they will 'stick'
  // to the channel that they were originally added to, and we can
  // change their parameters freely
  float frequency = map(mouseX, 0, width, 80.0, 1000.0);
  
  for (SinOsc sin : sines) {
    sin.freq(frequency);
    
    // increase frequency on the next channel by one semitone (a factor of 2^(1/12))
    frequency = frequency * 1.05946;
  }
}
