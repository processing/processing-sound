package processing.sound;

import com.jsyn.engine.SynthesisEngine;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.ports.UnitVariablePort;
import com.jsyn.unitgen.UnitGenerator;
import processing.core.PApplet;

public class BeatDetector extends Analyzer {
  private final BeatDetectorUGen detector;

  public BeatDetector(PApplet parent) {
    super(parent);
    this.detector = new BeatDetectorUGen();
  }

  protected void removeInput() {
    this.input = null;
  }

  protected void setInput(UnitOutputPort input) {
    Engine.getEngine().add(this.detector);
    this.detector.start();
    this.detector.input.connect(input);
  }

  public boolean analyze() {
    // TODO check if input exists, print warning if not
    return this.detector.current.getValue() == 1;
  }

  public void sensitivity(double sensitivity) {
    this.detector.sensitivity.set(sensitivity);
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

    // time incremented after every call to detect, to know how many milliseconds of audio we have processed so far.
    // this value is used as part of the the sensitivity implementation
    private long detectTimeMillis;
    // for circular buffer support
    private int insertAt;
    // vars for sEnergy
    private boolean isOnset;
    private final double[] buffer;
    private double[] energyBuffer;
    private double[] deltaBuffer;
    private boolean[] beatBuffer;
    // a millisecond timer used to prevent reporting onsets until the sensitivity threshold has been reached
    // see the sEnergy method
    private long sensitivityTimer;
    private int cursor;

    public BeatDetectorUGen() {
      this.addPort(this.input = new UnitInputPort("Input"));
      this.addPort(this.current = new UnitVariablePort("Current"));
      this.addPort(this.output = new UnitOutputPort("Output"));
      this.addPort(this.sensitivity = new UnitInputPort("Sensitivity"));

      buffer = new double[CHUNK_SIZE];
      sensitivity.set(20);
      detectTimeMillis = 0;
      isOnset = false;
      sensitivityTimer = 0;
      insertAt = 0;
      detectTimeMillis = 0;
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

      // System.out.println(start + " " + limit);
      for (int i = start; i < limit; i++) {
        double inputValue = inputs[i];

        buffer[cursor] = inputs[i];
        ++cursor;
        // When it is full, do the FFT.
        if (cursor == buffer.length) {
          sEnergy(buffer);
          current.set(isOnset ? 1 : 0);
          cursor = 0;
        }

        outputs[i] = inputValue;
      }
    }

    private void sEnergy(double[] samples) {
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
      if (detectTimeMillis - sensitivityTimer < sensitivity.get()) {
        isOnset = false;
      }
      // if we've made it this far then we're allowed to set a new
      // value, so set it true if it deserves to be, restart the timer
      else if (diff2 > 0 && instant > 2) {
        isOnset = true;
        sensitivityTimer = detectTimeMillis;
      }
      // OMG it wasn't true!
      else {
        isOnset = false;
      }
      energyBuffer[insertAt] = instant;
      deltaBuffer[insertAt] = diff;
      beatBuffer[insertAt] = isOnset;
      insertAt++;
      if (insertAt == energyBuffer.length) {
        insertAt = 0;
      }
      // advance the current time by the number of milliseconds this buffer represents
      detectTimeMillis += (long) (((float) samples.length / getFrameRate()) * 1000);
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
      return insertAt;
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
      float V = 0;
      for (int i = 0; i < arr.length; i++) {
        V += (float) Math.pow(arr[i] - val, 2);
      }
      V /= arr.length;
      return V;
    }
  }

}
