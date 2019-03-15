package processing.sound;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.jsyn.data.FloatSample;
import com.jsyn.util.SampleLoader;

import fr.delthas.javamp3.Sound;
import processing.core.PApplet;

// calls to amp(), pan() etc affect both the LAST initiated and still running sample, AND all subsequently started ones
/**
 * This is a Soundfile player which allows to play back and manipulate sound
 * files. Supported formats are: WAV, AIF/AIFF, and MP3.
 * 
 * @webref sound
 **/
public class SoundFile extends AudioSample {

	private static Map<String, FloatSample> SAMPLECACHE = new HashMap<String, FloatSample>();

	// the original library only printed an error if the file wasn't found,
	// but then later threw a NullPointerException when trying to play() the file.
	// it might be a better idea to throw an exception to the user in some cases,
	// e.g. when the file can't be found?
	/**
	 * @param parent
	 *            typically use "this"
	 * @param path
	 *            filename of the sound file to be loaded
	 * @webref sound
	 */
	public SoundFile(PApplet parent, String path) {
		super(parent);

		this.sample = SoundFile.SAMPLECACHE.get(path);

		if (this.sample == null) {
			InputStream fin = parent.createInput(path);

			// if PApplet.createInput() can't find the file or URL, it prints
			// an error message and fin returns null. In this case we can just
			// return this dysfunctional SoundFile object without initialising further
			if (fin == null) {
				Engine.printError("unable to find file " + path);
				return;
			}

			try {
				// load WAV or AIF using JSyn
				this.sample = SampleLoader.loadFloatSample(fin);
			} catch (IOException e) {
				// try parsing as mp3
				try {
					// stream as to be re-created, since it was modified in SampleLoader.loadFloatSample()
					fin = parent.createInput(path);
					Sound mp3 = new Sound(fin);
					try {
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						// TODO make decoding asynchronous with a FutureTask<FloatSample>
						// this call is expensive
						mp3.decodeFullyInto(os);
						float data[] = new float[os.size() / 2];
						SampleLoader.decodeLittleI16ToF32(os.toByteArray(), 0, os.size(), data, 0);
						this.sample = new FloatSample(data, mp3.isStereo() ? 2 : 1);
					} catch (IOException ee) {
						throw ee;
					} finally {
						mp3.close();
					}
				} catch (IOException ee) {
					Engine.printError("unable to decode sound file " + path);
					// return dysfunctional SoundFile object
					return;
				}
			}
			SoundFile.SAMPLECACHE.put(path, this.sample);
		}
		this.initiatePlayer();
	}

	// Below are just duplicated methods from the AudioSample superclass which
	// are required for the reference to build the corresponding pages.

	/**
	 * Returns the number of channels of the soundfile.
	 * 
	 * @return Returns the number of channels of the soundfile (1 for mono, 2 for
	 *         stereo)
	 * @webref sound
	 **/
	public int channels() {
		return super.channels();
	}

	/**
	 * Cues the playhead to a fixed position in the soundfile.
	 * 
	 * @param time
	 *            position in the soundfile that the next playback should start
	 *            from, in seconds.
	 * @webref sound
	 **/
	public void cue(float time) {
		super.cue(time);
	}

	/**
	 * Returns the duration of the soundfile in seconds.
	 * 
	 * @webref sound
	 * @return The duration of the soundfile in seconds.
	 **/
	public float duration() {
		return super.duration();
	}

	/**
	 * Returns the number of frames of this soundfile.
	 * 
	 * @webref sound
	 * @return The number of frames of this soundfile.
	 * @see duration()
	 **/
	public int frames() {
		return super.frames();
	}

	public void play() {
		super.play();
	}

	public void play(float rate) {
		super.play(rate);
	}

	public void play(float rate, float amp) {
		super.play(rate, amp);
	}

	public void play(float rate, float pos, float amp) {
		super.play(rate, pos, amp);
	}

	public void play(float rate, float pos, float amp, float add) {
		super.play(rate, pos, amp, add);
	}

	/**
	 * Starts the playback of the soundfile. Only plays to the end of the
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
	 *            (right). Only works for mono soundfiles!
	 * @param cue
	 *            position in the audiosample that playback should start from, in
	 *            seconds.
	 * @param add
	 *            offset the output of the generator by the given value
	 * @webref sound
	 **/
	public void play(float rate, float pos, float amp, float add, float cue) {
		super.play(rate, pos, amp, add, cue);
	}


	/**
	 * Jump to a specific position in the soundfile while continuing to play.
	 * 
	 * @webref sound
	 * @param time
	 *            position to jump to, in seconds.
	 **/
	public void jump(float time) {
		super.jump(time);
	}

	/**
	 * Stop the playback of the file, but cue it to the current position so that the
	 * next call to play() will continue playing where it left off.
	 * 
	 * @see stop
	 * @webref sound
	 */
	public void pause() {
		super.pause();
	}

	/**
	 * Check whether this soundfile is currently playing.
	 * 
	 * @return `true` if the soundfile is currently playing, `false` if it is not.
	 * @webref sound
	 */
	public boolean isPlaying() {
		return super.isPlaying();
	}

	public void loop() {
		super.loop();
	}

	public void loop(float rate) {
		super.loop(rate);
	}

	public void loop(float rate, float amp) {
		super.loop(rate, amp);
	}

	public void loop(float rate, float pos, float amp) {
		super.loop(rate, pos, amp);
	}

	/**
	 * Starts playback which will loop at the end of the soundfile.
	 * 
	 * @param rate
	 *            relative playback rate to use. 1 is the original speed. 0.5 is
	 *            half speed and one octave down. 2 is double the speed and one
	 *            octave up.
	 * @param pos
	 *            the panoramic position of this sound unit from -1.0 (left) to 1.0
	 *            (right). Only works for mono soundfiles!
	 * @param amp
	 *            the desired playback amplitude of the audiosample as a value from
	 *            0.0 (complete silence) to 1.0 (full volume)
	 * @param add
	 *            offset the output of the generator by the given value
	 * @webref sound
	 */
	public void loop(float rate, float pos, float amp, float add) {
		super.loop(rate, pos, amp, add);
	}

	/**
	 * FIXME see comment in AudioSample class
	 * 
	 * @param cue
	 *            position in the audiosample that the next playback or loop should
	 *            start from, in seconds. public void loop(float rate, float pos,
	 *            float amp, float add, float cue) { super.loop(rate, pos, amp, add,
	 *            cue); }
	 */

	/**
	 * Change the amplitude/volume of this audiosample.
	 *
	 * @param amp
	 *            A float value between 0.0 (complete silence) and 1.0 (full volume)
	 *            controlling the amplitude/volume of this sound.
	 * @webref sound
	 **/
	public void amp(float amp) {
		super.amp(amp);
	}

	/**
	 * Move the sound in a stereo panorama. Only works for mono soundfiles!
	 * 
	 * @param pos
	 *            the panoramic position of this sound unit from -1.0 (left) to 1.0
	 *            (right).
	 * @webref sound
	 **/
	public void pan(float pos) {
		super.pan(pos);
	}

	/**
	 * Set the playback rate of the soundfile.
	 * 
	 * @param rate
	 *            Relative playback rate to use. 1 is the original speed. 0.5 is
	 *            half speed and one octave down. 2 is double the speed and one
	 *            octave up.
	 * @webref sound
	 **/
	public void rate(float rate) {
		super.rate(rate);
	}

	/**
	 * Stops the playback.
	 * 
	 * @see pause
	 * @webref sound
	 **/
	public void stop() {
		super.stop();
	}

}
