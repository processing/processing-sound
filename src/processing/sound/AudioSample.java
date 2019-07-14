package processing.sound;

import com.jsyn.data.FloatSample;
import com.jsyn.unitgen.VariableRateDataReader;
import com.jsyn.unitgen.VariableRateMonoReader;
import com.jsyn.unitgen.VariableRateStereoReader;
import com.softsynth.shared.time.TimeStamp;

import processing.core.PApplet;

/**
 * This class allows you low-level access to an audio buffer to create, access,
 * manipulate and play back sound samples.
 * 
 * If you want to pre-load your audio sample with an audio file from disk you
 * can do so using the SoundFile subclass.
 * 
 * @see SoundFile
 * @webref sound
 */
public class AudioSample extends SoundObject {

	protected FloatSample sample;
	protected VariableRateDataReader player;

	// cued frame index of this sample
	protected int startFrame = 0;
	// DataReader's queue status, required for accurate computation of playback
	// position within the sample
	protected long startFrameCountOffset = 0;

	public AudioSample(PApplet parent, int frames) {
		this(parent, frames, false);
	}

	public AudioSample(PApplet parent, int frames, boolean stereo) {
		this(parent, frames, stereo, 44100); // read current framerate from Engine instead?
	}

	/**
	 * Allocate a new audiosample buffer with the given number of frames.
	 *
	 * @param parent
	 *            typically use "this"
	 * @param frames
	 *            the desired number of frames for this audiosample
	 * @param frameRate
	 *            the underlying frame rate of the sample (default: 44100)
	 * @param stereo
	 *            whether to treat the audiosample as 2-channel (stereo) or not
	 *            (default: false)
	 * @webref sound
	 */
	public AudioSample(PApplet parent, int frames, boolean stereo, int frameRate) {
		super(parent);
		this.sample = new FloatSample(frames, stereo ? 2 : 1);
		this.sample.setFrameRate(frameRate);
		this.initiatePlayer();
	}

	// TODO add another set of constructors: AudioSample(PApplet parent, float
	// duration)?
	// risk of accidental overloading through int/float, but could be interesting..

	/**
	 * @param data
	 *            an array of float values to be used as this audiosample's sound
	 *            data. The audiosample will consequently have as many frames as the
	 *            length of the given array.
	 * @webref sound
	 */
	public AudioSample(PApplet parent, float[] data) {
		this(parent, data, false);
	}

	public AudioSample(PApplet parent, float[] data, boolean stereo) {
		this(parent, data, stereo, 44100); // read current framerate from Engine instead?
	}

	public AudioSample(PApplet parent, float[] data, int frameRate) {
		this(parent, data, false, frameRate);
	}

	public AudioSample(PApplet parent, float[] data, boolean stereo, int frameRate) {
		super(parent);
		this.sample = new FloatSample(data, stereo ? 2 : 1);
		this.sample.setFrameRate(frameRate);
		this.initiatePlayer();
	}

	// called by subclasses who initialise their own sample object
	protected AudioSample(PApplet parent) {
		super(parent);
	}

	// private constructor for cloning (see getUnusedPlayer() method below)
	protected AudioSample(AudioSample original) {
		super(null);
		this.sample = original.sample;
		this.initiatePlayer();
		this.player.amplitude.set(original.player.amplitude.get());
		this.player.rate.set(original.player.rate.get());
		this.startFrame = original.startFrame;
	}

	// should be called by the constructor after the sample object has been set
	protected void initiatePlayer() {
		if (this.channels() == 2) {
			this.player = new VariableRateStereoReader();
		} else {
			this.player = new VariableRateMonoReader();
		}

		// needs to be set explicitly
		this.player.rate.set(this.sampleRate());
		this.circuit = new JSynCircuit(this.player.output);
		this.amplitude = this.player.amplitude;

		// unlike the Oscillator and Noise classes, the sample player units can
		// always stay connected to the JSyn synths, since they make no noise
		// as long as their dataQueue is empty
		super.play(); // doesn't actually start playback, just adds the (silent) units

		// as a consequence, the AudioSample class manages its isPlaying status
		// explicitly (see the overridden public boolean isPlaying() method below)
		this.isPlaying = false;
	}

	/**
	 * Check whether this audiosample is currently playing.
	 * @Override
	 **/
	public boolean isPlaying() {
		// this used to just return dataQueue.hasMore(), but this raises false
		// positives as .hasMore() still returns true for a split second after
		// dataQueue.clear() has been called. the AudioSample class therefore
		// manually sets the isPlaying boolean to true when playback commences,
		// but every time the user queries the playing status we check if the
		// queue has actually run out in the meantime.
		this.isPlaying = this.isPlaying & this.player.dataQueue.hasMore();
		return this.isPlaying;
	}

	/**
	 * Change the amplitude/volume of this audiosample.
	 *
	 * @param amp
	 *            A float value between 0.0 (complete silence) and 1.0 (full volume)
	 *            controlling the amplitude/volume of this sound.
	 * @webref sound
	 **/
	public void amp(float amp) {
		if (Engine.checkAmp(amp)) {
			this.player.amplitude.set(amp);
		}
	}

	/**
	 * Returns the number of channels in the audiosample.
	 * 
	 * @return the number of channels in the audiosample (1 for mono, 2 for stereo)
	 * @webref sound
	 **/
	public int channels() {
		return this.sample.getChannelsPerFrame();
	}

	/**
	 * Cues the playhead to a fixed position in the audiosample.
	 * 
	 * @param time
	 *            position in the audiosample that the next playback should start
	 *            from, in seconds.
	 * @webref sound
	 **/
	public void cue(float time) {
		if (this.setStartTime(time)) {
			this.stop();
			this.setStartFrameCountOffset();
		}
	}

	/**
	 * Cues the playhead to a fixed position in the audiosample.
	 * 
	 * @webref sound
	 * @param frameNumber
	 *            frame number to start playback from.
	 **/
	public void cueFrame(int frameNumber) {
		if (this.setStartFrame(frameNumber)) {
			this.stop();
			this.setStartFrameCountOffset();
		}
	}

	/**
	 * Returns the duration of the audiosample in seconds.
	 * 
	 * @webref sound
	 * @return The duration of the audiosample in seconds.
	 **/
	public float duration() {
		return (float) (this.frames() / this.sample.getFrameRate());
	}

	/**
	 * Returns the number of frames of the audiosample.
	 * 
	 * @webref sound
	 * @return The number of frames of the audiosample.
	 * @see duration()
	 **/
	public int frames() {
		return this.sample.getNumFrames();
	}

	public void resize(int frames) {
		this.resize(frames, false);
	}

	private boolean setStartFrame(int frameNumber) {
		if (this.checkStartFrame(frameNumber)) {
			this.startFrame = frameNumber;
			return true;
		} else {
			return false;
		}
	}

	private boolean setStartTime(float time) {
		if (time < 0) {
			Engine.printError("absolute position can't be < 0");
			return false;
		}
		int startFrame = Math.round(this.sampleRate() * time);
		if (startFrame >= this.frames()) {
			// TODO implement wrapping over to beginning of file and printing warning instead of error
			Engine.printError("can't cue past end of sample (total duration is " + this.duration() + "s)");
			return false;
		}
		this.startFrame = startFrame;
		return true;
	}

	/**
	 * Jump to a specific position in the audiosample without interrupting playback.
	 * 
	 * @param time
	 *            position to jump to, in seconds.
	 * @see cue
	 * @see play
	 * @webref sound
	 **/
	public void jump(float time) {
		// FIXME this currently only works for simply *playing* files, if the
		// current playback was a loop, all of the looping information is lost
		if (this.setStartTime(time)) {
			this.stop();
			this.play(); // if the file wasn't playing when jump() was called, just start playing it
		}
	}

	/**
	 * Jump to a specific position in the audiosample without interrupting playback.
	 * 
	 * @param time
	 *            frame number to jump to.
	 * @see cue
	 * @see play
	 * @webref sound
	 **/
	public void jumpFrame(int frameNumber) {
		if (this.setStartFrame(frameNumber)) {
			this.stop();
			// if the file wasn't already playing when jumpFrame() was called, just start playing it
			this.play();
		}
	}

	// helper function: when called on a soundfile already running, the original
	// library triggered a second (concurrent) playback. with JSyn, every data
	// reader can only do one playback at a time, so if the present player
	// is busy we need to create a new one with the exact same settings and
	// trigger it instead (see JSyn's VoiceAllocator class)
	protected AudioSample getUnusedPlayer() {
		// TODO could implement a more intelligent player allocation pool method here to
		// limit the total number of playback voices
		if (this.isPlaying()) {
			// use private constructor which copies the sample as well as all playback
			// settings over
			return new AudioSample(this);
		} else {
			return this;
		}
	}

	private void setStartFrameCountOffset() {
		this.startFrameCountOffset = this.player.dataQueue.getFrameCount();
	}

	// FIXME cueing subsections of a file for looping is not supported at the
	// moment because looping information would need to be stored to correctly
	// compute the current position
	private void loopInternal(int startFrame, int numFrames, int numLoops) {
		// always use current sample player
		this.stop();
		this.setStartFrameCountOffset();
		this.startFrame = startFrame;
		if (numLoops > 1) {
			this.player.dataQueue.queueLoop(this.sample, startFrame, numFrames, numLoops);
		} else {
			this.player.dataQueue.queueLoop(this.sample, startFrame, numFrames);
		}
		this.isPlaying = true;
	}

	private void loopInternal(int startFrame, int numFrames) {
		this.loopInternal(startFrame, numFrames, 0);
	}


	public void loop() {
		this.loopInternal(0, this.frames());
	}

	public void loop(float rate) {
		this.rate(rate);
		this.loop();
	}

	public void loop(float rate, float amp) {
		this.rate(rate);
		this.amp(amp);
		this.loop();
	}

	public void loop(float rate, float pos, float amp) {
		this.pan(pos);
		this.loop(rate, amp);
	}

	/**
	 * Starts playback which loops from the beginning to the end of the sample.
	 * 
	 * @param rate
	 *            relative playback rate to use. 1 is the original speed. 0.5 is
	 *            half speed and one octave down. 2 is double the speed and one
	 *            octave up.
	 * @param pos
	 *            the panoramic position of this sound unit from -1.0 (left) to 1.0
	 *            (right). Only works for mono audiosamples!
	 * @param amp
	 *            the desired playback amplitude of the audiosample as a value from
	 *            0.0 (complete silence) to 1.0 (full volume)
	 * @param add
	 *            offset the output of the generator by the given value
	 * @webref sound
	 */
	public void loop(float rate, float pos, float amp, float add) {
		this.add(add);
		this.loop(rate, pos, amp);
	}

//	public void loopFrames(int startFrame, int numFrames) {
		// TODO check startFrame, numFrames
//		this.loopInternal(startFrame, numFrames, 0);
//	}

//	public void loopFrames(int startFrame, int numFrames, int loops) {
		// TODO check startFrame, numFrames, loop > 1
//		this.loopInternal(startFrame, numFrames, loops);
//	}

	// TODO add same functions but specifying loop section in seconds
//	public void loopSection(float start, float duration) {
//	}
//	public void loopSection(float start, float duration, int loops) {
//	}


	/*
	 * FIXME cueing a position for loops has to be handled differently than for
	 * simple playback, because passing a startFrame to dataQueue.queueLoop() causes
	 * repetitions of the loop to only ever be played from that position, instead of
	 * jumping back to the very beginning of the sample after reaching the end
	 * 
	 * @param cue position in the audiosample that the loop should start from, in
	 * seconds. public void loop(float rate, float pos, float amp, float add, float
	 * cue) { this.cue(cue); this.loop(rate, pos, amp, add); }
	 */

	private void playInternal() {
		this.playInternal(this.startFrame, this.frames() - this.startFrame);
	}

	private void playInternal(int startFrame, int numFrames) {
		this.setStartFrameCountOffset();
		// only queueImmediate() guarantees that a directly subsequent call to .hasMore() returns true
		this.player.dataQueue.queueImmediate(this.sample, startFrame, numFrames, new TimeStamp(0.0));
		this.isPlaying = true;
	}

	public void play() {
		AudioSample source = this.getUnusedPlayer();
		source.playInternal();
		// for improved handling by the user, could return a reference to
		// whichever audiosample object is the actual source (i.e. JSyn
		// container) of the newly triggered playback
		// return source;
	}

	public void play(float rate) {
		this.rate(rate);
		this.play();
	}

	public void play(float rate, float amp) {
		this.amp(amp);
		this.play(rate);
	}

	public void play(float rate, float pos, float amp) {
		this.pan(pos);
		this.play(rate, amp);
	}

	public void play(float rate, float pos, float amp, float add) {
		this.add(add);
		this.play(rate, pos, amp);
	}

	/**
	 * Starts the playback of the audiosample. Only plays to the end of the
	 * audiosample once.
	 * 
	 * @param rate
	 *            relative playback rate to use. 1 is the original speed. 0.5 is
	 *            half speed and one octave down. 2 is double the speed and one
	 *            octave up.
	 * @param amp
	 *            the desired playback amplitude of the audiosample as a value from
	 *            0.0 (complete silence) to 1.0 (full volume)
	 * @param pos
	 *            the panoramic position of this sound unit from -1.0 (left) to 1.0
	 *            (right). Only works for mono audiosamples!
	 * @param cue
	 *            position in the audiosample that playback should start from, in
	 *            seconds.
	 * @param add
	 *            offset the output of the generator by the given value
	 * @webref sound
	 **/
	public void play(float rate, float pos, float amp, float add, float cue) {
		this.cue(cue);
		this.play(rate, pos, amp, add);
	}

	public void playFor(float duration) {
		AudioSample source = this.getUnusedPlayer();
		source.playInternal(this.startFrame, Math.min((int) Math.round(duration * this.sampleRate()), this.frames() - this.startFrame));
		// FIXME at the end of playback the startFrame is still the initially cued one,
		// even though position() reports that the file is at the end of the playback bit.
		// should we add an event listener that cues the file to the end of the playback
		// when it finished (for what that might look like see pause()), or otherwise
		// reset the position()?
	}

	/**
	 * Starts the playback of the audiosample for the specified duration or to the
	 * end of the audiosample, whichever comes first.
	 * 
	 * @param duration
	 *            duration that this audiosample should be played back for, in seconds.
	 *            If the start position plus duration exceeds the total duration of the
	 *            sample, playback will stop at the end of the sample.
	 * @param cue
	 *            position in the audiosample that playback should start from, in
	 *            seconds.
	 * @webref sound
	 **/
	public void playFor(float duration, float cue) {
		this.cue(cue);
		this.playFor(duration);
	}

	/**
	 * Set the playback rate of the audiosample.
	 * 
	 * @param rate
	 *            Relative playback rate to use. 1 is the original speed. 0.5 is
	 *            half speed and one octave down. 2 is double the speed and one
	 *            octave up.
	 * @webref sound
	 **/
	public void rate(float rate) {
		if (rate <= 0) {
			Engine.printError("rate needs to be positive");
		} else {
			// 1.0 = original
			this.player.rate.set(this.sampleRate() * rate);
		}
	}

	/**
	 * Resizes the underlying buffer of the audiosample to the given number of
	 * frames.
	 * 
	 * @param frames
	 *            the desired number of frames for this audiosample
	 * @param stereo
	 *            whether to treat the audiosample as 2-channel (stereo) or not
	 *            (default: false)
	 * @webref sound
	 */
	public void resize(int frames, boolean stereo) {
		this.stop();
		this.sample.allocate(frames, stereo ? 2 : 1);
		this.initiatePlayer(); // TODO re-initiation might be redundant
	}

	/**
	 * Returns the underlying sample rate of the audiosample.
	 * 
	 * @webref sound
	 * @return Returns the underlying sample rate of the audiosample as an int.
	 **/
	public int sampleRate() {
		return (int) Math.round(this.sample.getFrameRate());
	}

	/**
	 * Move the sound in a stereo panorama. Only works for mono audiosamples!
	 *
	 * @webref sound
	 * @param pos
	 *            the panoramic position of this sound unit from -1.0 (left) to 1.0
	 *            (right).
	 **/
	public void pan(float pos) {
		super.pan(pos);
	}

	/**
	 * Set multiple parameters at once
	 * 
	 * @webref sound
	 * @param rate
	 *            Relative playback rate to use. 1 is the original speed. 0.5 is
	 *            half speed and one octave down. 2 is double the speed and one
	 *            octave up.
	 * @param pos
	 *            the panoramic position of this sound unit from -1.0 (left) to 1.0
	 *            (right).
	 * @param amp
	 *            the desired playback amplitude of the audiosample as a value from
	 *            0.0 (complete silence) to 1.0 (full volume)
	 * @param add
	 *            offset the output of the generator by the given value
	 **/
	public void set(float rate, float pos, float amp, float add) {
		this.rate(rate);
		this.pan(pos);
		this.amp(amp);
		this.add(add);
	}

	/**
	 * Stops the playback.
	 * 
	 * @webref sound
	 **/
	public void stop() {
		this.player.dataQueue.clear();
		this.isPlaying = false;
	}

	// new methods go here

	/**
	 * Get current sound file playback position in seconds.
	 * 
	 * Note that, if this audio sample was at some point played back in parallel
	 * (triggered by another call to play() before the original playback had finished),
	 * the position returned by this function can be of any of the concurrent playbacks,
	 * not necessarily the last one that was triggered.
	 * 
	 * @return The current position of the audio sample playback in seconds
	 * @webref sound
	 */
	public float position() {
		return this.positionFrame() / (float) this.sampleRate();
	}

	/**
	 * Get frame index of current sound file playback position.
	 * 
	 * Note that, if this audio sample was at some point played back in parallel
	 * (triggered by another call to play() before the original playback had finished),
	 * the position returned by this function can be of any of the concurrent playbacks,
	 * not necessarily the last one that was triggered.
	 * 
	 * @return The current frame index position of the audio sample playback
	 * @webref sound
	 */
	public int positionFrame() {
		return (int) (this.startFrame + this.player.dataQueue.getFrameCount() - this.startFrameCountOffset) % this.frames();
	}
	/**
	 * Get current sound file playback position in percent.
	 * 
	 * Note that, if this audio sample was at some point played back in parallel
	 * (triggered by another call to play() before the original playback had finished),
	 * the position returned by this function can be of any of the concurrent playbacks,
	 * not necessarily the last one that was triggered.
	 * 
	 * @return The current position of the audio sample playback in percent (a value
	 *         between 0 and 100).
	 * @webref sound
	 */
	public float percent() {
		return 100f * this.positionFrame() / (float) this.frames();
	}

	/**
	 * Stop the playback of the sample, but cue it to the current position.
	 * 
	 * @see cue
	 * @webref sound
	 */
	public void pause() {
		if (this.isPlaying()) {
			this.stop();
			this.startFrame = this.positionFrame();
			this.setStartFrameCountOffset();
		} else {
			Engine.printWarning("trying to pause an audio sample that is not playing");
		}
	}

	protected boolean checkStartFrame(int startFrame) {
		return this.checkStartFrame(startFrame, true);
	}

	protected boolean checkStartFrame(int startFrame, boolean verbose) {
		if (startFrame < 0 || startFrame >= this.frames()) {
			if (verbose) {
				Engine.printError(Integer.toString(startFrame) + " is not a valid start frame number (has to be in [0,"
						+ Integer.toString(this.frames() - 1) + "]");
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Get the current sample data and write it into the given array.
	 *
	 * Get the current sample data and write it into the given array. The array
	 * has to be able to store as many floats as there are frames in this sample,
	 * or twice the number of frames for a stereo sample.
	 *
	 * @param data
	 *            the target array that the read data is written to
	 */
	public void read(float[] data) {
		if (this.channels() == 2 && data.length != 2 * this.frames()) {
			Engine.printWarning(
					"the length of the array passed to read(float[]) does not match the size of the data of this stereo audio sample (note that stereo samples contain two values per frame!)");
		} else if (this.channels() == 1 && data.length != this.frames()) {
			Engine.printWarning(
					"the length of the array passed to read(float[]) does not match the number of frames of this audio sample");
		}
		// TODO catch exception and print understandable error message
		this.sample.read(data);
	}

	/**
	 * Read some frames of this audio sample into an array. Stereo samples
	 * contain two data points per frame.
	 *
	 * @param startFrame
	 *            the index of the first frame of the audiosample that should be
	 *            read
	 * @param startIndex
	 *            the position in the array where the first read frame should be
	 *            written to (typically 0)
	 * @param numFrames
	 *            the number of frames that should be read (can't be greater than
	 *            audiosample.channels() * data.length - startIndex)
	 * @webref sound
	 */
	public void read(int startFrame, float[] data, int startIndex, int numFrames) {
		if (this.checkStartFrame(startFrame)) {
			// TODO check and print informative warning about stereo case
			if (startFrame + numFrames < this.frames()) {
				this.sample.read(startFrame, data, startIndex, numFrames);
			} else {
				// overflow at end of sample, need to do two partial copies
				int firstReadLength = this.frames() - startFrame;
				this.sample.read(startFrame, data, startIndex, firstReadLength);
				this.sample.read(0, data, startIndex + firstReadLength, numFrames - firstReadLength);
			}
		}
	}

	/**
	 * @param index
	 *            the index of the single frame of the audiosample that should be
	 *            read and returned as a float
	 * @return the value of the audio sample at the given frame
	 */
	public float read(int index) {
		if (this.channels() == 2) {
			Engine.printWarning(
					"read(int) only returns data for the left channel of a stereo file, please use one of the other read() methods to access data for all channels");
		}
		// TODO catch exception and print understandable error message
		return (float) this.sample.readDouble(index);
	}

	/**
	 * Overwrite the sample with the data from the given array. The array should
	 * contain as many floats as there are frames in this sample, or twice as
	 * many as the number of frames for a stereo sample.
	 * 
	 * @param data
	 *            the array from which the sample data, up to
	 *            sample.frames() * sample.channels() floats, should be copied
	 */
	public void write(float[] data) {
		if (this.channels() == 2 && data.length != 2 * this.frames()) {
			Engine.printWarning(
					"the length of the array passed to write(float[]) does not match the size of the data of this stereo audio sample (note that stereo samples contain two values per frame!)");
		} else if (this.channels() == 1 && data.length != this.frames()) {
			Engine.printWarning(
					"the length of the array passed to write(float[]) does not match the number of frames of this audio sample");
		}
		this.sample.write(data);
	}

	/**
	 * Write some frames of this audio sample. Stereo samples require two data
	 *.points per frame.
	 *
	 * @param startFrame
	 *            the index of the first frame of the audiosample that should be
	 *            written to
	 * @param startIndex
	 *            the position in the array that the first value to write should be
	 *            taken from (typically 0)
	 * @param numFrames
	 *            the number of frames that should be written (can't be greater than
	 *            audiosample.channels() * data.length - startIndex)
	 * @webref sound
	 */
	public void write(int startFrame, float[] data, int startIndex, int numFrames) {
		// FIXME check stereo case
		if (this.checkStartFrame(startFrame)) {
			if (startFrame + numFrames < this.frames()) {
				this.sample.write(startFrame, data, startIndex, numFrames);
			} else {
				// overflow at end of sample, need to do two partial copies
				int firstReadLength = this.frames() - startFrame;
				this.sample.write(startFrame, data, startIndex, firstReadLength);
				this.sample.write(0, data, startIndex + firstReadLength, numFrames - firstReadLength);
			}

		}
	}

	/**
	 * @param index
	 *            the index of the single frame of the audiosample that should be
	 *            set to the given value
	 * @param value
	 *            the float value that the given audio frame should be set to
	 */
	public void write(int index, float value) {
		if (this.channels() == 2) {
			Engine.printWarning(
					"write(int, float) only writes data to the left channel of a stereo file, please use one of the other write() methods to write data to all channels");
		}
		if (this.checkStartFrame(startFrame)) {
			this.sample.writeDouble(index, value);
		}
	}
}
