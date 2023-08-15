package processing.sound;

import com.jsyn.engine.SynthesisEngine;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.ports.UnitVariablePort;
import com.jsyn.unitgen.UnitGenerator;
import processing.core.PApplet;

/**
 * The BeatDetector analyzer looks for spikes in the energy of an audio signal
 * which are often associated with rhythmic musical beats and can be used to trigger a
 * response whenever the incoming audio signal pulses. Note that this
 * implementation does not return a tempo or BPM (beats per minute) value — it
 * can only tell you whether the current moment of audio contains a beat or not.
 *
 * @webref Analysis:BeatDetector
 * @webBrief Looks for spikes in the energy of an audio signal
 * which are often associated with rhythmic musical beats and can be used to trigger a
 * response whenever the incoming audio signal pulses.
 **/
public class BeatDetector extends Analyzer {
  private final BeatDetectorUGen detector;

  /**
   * @param parent Typically "this"
   */
  public BeatDetector(PApplet parent) {
    super(parent);
    this.detector = new BeatDetectorUGen();
  }

  @Override
  protected void removeInput() {
    this.input = null;
  }

  @Override
  protected void setInput(UnitOutputPort input) {
    Engine.getEngine().add(this.detector);
    this.detector.start();
    this.detector.input.connect(input);
  }

  /**
   * Returns whether or not the current moment of audio contains a beat or not.
   * A "beat" is defined as a spike in the energy of the audio signal — it may
   * or may not coincide exactly with a musical beat.
   *
   * @webref Analysis:BeatDetector
   * @webBrief Returns whether or not the current moment of audio contains a beat or not.
   *
   * @return True if the audio signal currently contains a beat, false otherwise.
   */
  public boolean isBeat() {
    return this.detector.current.getValue() == 1;
  }

  /**
   * Sets the sensitivity, in milliseconds, of the beat detection algorithm.
   * The sensitivity determines how long the detector will wait after detecting
   * a beat to detect the next one. For example, a sensitivity of 10 will cause the
   * detector to wait 10ms before returning any new beats.
   *
   * A higher sensitivity value means the algorithm will be less sensitive. You
   * can tune this appropriately if you notice the detector returning too many
   * false positive beats.
   *
   * @webref Analysis:BeatDetector
   * @webBrief Sets the sensitivity, in milliseconds, of the beat detection algorithm.
   *
   * @param sensitivity Sensitivity in milliseconds. Must be a positive number.
   */
  public void sensitivity(int sensitivity) {
    this.detector.sensitivity.set(sensitivity);
  }

  /**
   * Sets the sensitivity of the beat detector.
   * @webref Analysis:BeatDetector
   * @webBrief Sets the sensitivity of the beat detector.
   *
   * @return The sensitivity in milliseconds.
   */
  public int sensitivity() {
    return (int) this.detector.sensitivity.get();
  }

  public double[] getEnergyBuffer() {
    return detector.getEnergyBuffer();
  }

  public int getEnergyCursor() {
    return detector.getEnergyCursor();
  }

  public boolean[] getBeatBuffer() {
    return detector.getBeatBuffer();
  }

  public class BeatDetectorUGen extends UnitGenerator {
    private static final int CHUNK_SIZE = 1024;
    public UnitInputPort input;
    public UnitVariablePort current;
    public UnitInputPort sensitivity;
    public UnitOutputPort output;

    private final double[] audioBuffer;
    private double[] energyBuffer;
    private double[] deltaBuffer;
    private boolean[] beatBuffer;

    private int audioBufferCursor;
    private int energyBufferCursor;

    private long detectTimeMillis;
    private long sensitivityTimer;

    public BeatDetectorUGen() {
      this.addPort(this.input = new UnitInputPort("Input"));
      this.addPort(this.current = new UnitVariablePort("Current"));
      this.addPort(this.output = new UnitOutputPort("Output"));
      this.addPort(this.sensitivity = new UnitInputPort("Sensitivity"));
      sensitivity.set(10);

      audioBuffer = new double[CHUNK_SIZE];
    }

    @Override
    public void setSynthesisEngine(SynthesisEngine synthesisEngine) {
      super.setSynthesisEngine(synthesisEngine);
      int frameRate = synthesisEngine.getFrameRate();
      int bufferSize = frameRate / CHUNK_SIZE;
      energyBuffer = new double[bufferSize];
      deltaBuffer = new double[bufferSize];
      beatBuffer = new boolean[bufferSize];
    }

    public void generate(int start, int limit) {
      double[] inputs = input.getValues();
      double[] outputs = output.getValues();

      for (int i = start; i < limit; i++) {
        double inputValue = inputs[i];

        audioBuffer[audioBufferCursor] = inputs[i];
        ++audioBufferCursor;
        // When it is full, do the FFT.
        if (audioBufferCursor == audioBuffer.length) {
          boolean beatDetected = detect(audioBuffer);
          current.set(beatDetected ? 1 : 0);
          audioBufferCursor = 0;
        }

        outputs[i] = inputValue;
      }
    }

    // This algorithm is adapted from Damien Quartz's Minim audio library
    // http://code.compartmental.net/tools/minim/
    private boolean detect(double[] samples) {
      // compute the energy level
      float level = 0;
      for (int i = 0; i < samples.length; i++) {
        level += (samples[i] * samples[i]);
      }
      level /= samples.length;
      level = (float) Math.sqrt(level);
      float instant = level * 100;
      // compute the average local energy
      float E = average(energyBuffer);
      // compute the variance of the energies in eBuffer
      float V = variance(energyBuffer, E);
      // compute C using a linear digression of C with V
      float C = (-0.0025714f * V) + 1.5142857f;
      // filter negative values
      float diff = Math.max(instant - C * E, 0);
      // find the average of only the positive values in dBuffer
      float dAvg = specAverage(deltaBuffer);
      // filter negative values
      float diff2 = Math.max(diff - dAvg, 0);
      // report false if it's been less than 'sensitivity'
      // milliseconds since the last true value

      boolean beatDetected = false;

      if (detectTimeMillis - sensitivityTimer < sensitivity.get()) {
        beatDetected = false;
      }
      // if we've made it this far then we're allowed to set a new
      // value, so set it true if it deserves to be, restart the timer
      else if (diff2 > 0 && instant > 2) {
        beatDetected = true;
        sensitivityTimer = detectTimeMillis;
      }
      // OMG it wasn't true!
      else {
        beatDetected = false;
      }
      energyBuffer[energyBufferCursor] = instant;
      deltaBuffer[energyBufferCursor] = diff;
      beatBuffer[energyBufferCursor] = beatDetected;
      energyBufferCursor++;
      if (energyBufferCursor == energyBuffer.length) {
        energyBufferCursor = 0;
      }
      // advance the current time by the number of milliseconds this buffer represents
      detectTimeMillis += (long) (((float) samples.length / getFrameRate()) * 1000);

      return beatDetected;
    }

    private float average(double[] arr) {
      float avg = 0;
      for (int i = 0; i < arr.length; i++) {
        avg += arr[i];
      }
      avg /= arr.length;
      return avg;
    }

    private float specAverage(double[] arr) {
      float avg = 0;
      float num = 0;
      for (int i = 0; i < arr.length; i++) {
        if (arr[i] > 0) {
          avg += arr[i];
          num++;
        }
      }
      if (num > 0) {
        avg /= num;
      }
      return avg;
    }

    private float variance(double[] arr, float val) {
      float v = 0;
      for (int i = 0; i < arr.length; i++) {
        v += (float) Math.pow(arr[i] - val, 2);
      }
      v /= arr.length;
      return v;
    }

    public double[] getEnergyBuffer() {
      return energyBuffer;
    }

    public double[] getDeltaBuffer() {
      return deltaBuffer;
    }

    public boolean[] getBeatBuffer() {
      return beatBuffer;
    }

    public int getEnergyCursor() {
      return energyBufferCursor;
    }
  }
}
