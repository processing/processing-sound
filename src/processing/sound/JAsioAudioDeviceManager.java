package processing.sound;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.jsyn.devices.AudioDeviceInputStream;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.devices.AudioDeviceOutputStream;
import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioDriverListener;
import com.synthbot.jasiohost.AsioDriverState;
import com.synthbot.jasiohost.AsioException;

/**
 * JSyn {@link AudioDeviceManager} implementation that routes the synthesizer's
 * audio output (and optionally input) through native ASIO drivers on Windows,
 * using the JAsioHost library (https://github.com/mhroth/jasiohost, LGPL).
 *
 * <p>
 * Each ASIO driver registered in the system registry is exposed as one audio
 * device. Since the ASIO API only allows a single driver to be loaded at any
 * time, driver information that requires the driver to be loaded (such as the
 * number of channels) is probed by briefly loading and unloading the driver,
 * and then cached.
 *
 * <p>
 * ASIO pushes audio data to the host in <code>bufferSwitch()</code> callbacks,
 * while JSyn's synthesis thread blocks on {@link AudioDeviceOutputStream#write}
 * to pace itself. This class therefore bridges the two sides with ring buffers:
 * the synthesis thread writes interleaved samples into a ring buffer which is
 * drained by the ASIO callback thread.
 */
class JAsioAudioDeviceManager implements AudioDeviceManager {

	private final List<String> driverNames;

	// deviceID -> { maxInputChannels, maxOutputChannels }, lazily probed
	private final Map<Integer, int[]> channelCounts = new HashMap<Integer, int[]>();

	// the (single) stream pair that's currently attached to the ASIO driver
	private Session session;

	private double suggestedInputLatency = 0.010;
	private double suggestedOutputLatency = 0.010;

	JAsioAudioDeviceManager() {
		// this class' static initializer loads the jasiohost native library
		// (and throws UnsatisfiedLinkError if it's not available)
		this.driverNames = AsioDriver.getDriverNames();
		if (this.driverNames.isEmpty()) {
			throw new RuntimeException("did not find any ASIO drivers registered on this system, "
					+ "please install the driver that came with your audio interface (or the universal "
					+ "ASIO4ALL driver) and try again");
		}
	}

	@Override
	public synchronized int getDeviceCount() {
		return this.driverNames.size();
	}

	@Override
	public synchronized String getDeviceName(int deviceID) {
		return this.isValidDeviceId(deviceID) ? this.driverNames.get(deviceID) : "";
	}

	@Override
	public String getName() {
		return "JAsioHost ASIO";
	}

	private boolean isValidDeviceId(int deviceID) {
		return deviceID >= 0 && deviceID < this.driverNames.size();
	}

	@Override
	public synchronized int getDefaultInputDeviceID() {
		for (int i = 0; i < this.driverNames.size(); i++) {
			if (this.getChannelCounts(i)[0] > 0) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public synchronized int getDefaultOutputDeviceID() {
		for (int i = 0; i < this.driverNames.size(); i++) {
			if (this.getChannelCounts(i)[1] > 1) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public synchronized int getMaxInputChannels(int deviceID) {
		return this.isValidDeviceId(deviceID) ? this.getChannelCounts(deviceID)[0] : 0;
	}

	@Override
	public synchronized int getMaxOutputChannels(int deviceID) {
		return this.isValidDeviceId(deviceID) ? this.getChannelCounts(deviceID)[1] : 0;
	}

	@Override
	public double getDefaultLowInputLatency(int deviceID) {
		return this.suggestedInputLatency;
	}

	@Override
	public double getDefaultHighInputLatency(int deviceID) {
		return this.suggestedInputLatency;
	}

	@Override
	public double getDefaultLowOutputLatency(int deviceID) {
		return this.suggestedOutputLatency;
	}

	@Override
	public double getDefaultHighOutputLatency(int deviceID) {
		return this.suggestedOutputLatency;
	}

	@Override
	public int setSuggestedInputLatency(double latency) {
		this.suggestedInputLatency = latency;
		return 0;
	}

	@Override
	public int setSuggestedOutputLatency(double latency) {
		this.suggestedOutputLatency = latency;
		return 0;
	}

	@Override
	public synchronized AudioDeviceOutputStream createOutputStream(int deviceID, int frameRate,
			int numOutputChannels) {
		Session session = this.getOrCreateSession(deviceID, frameRate);
		if (session.output != null) {
			throw new RuntimeException("an ASIO output stream is already open on device '"
					+ this.getDeviceName(deviceID) + "'");
		}
		session.output = new JAsioOutputStream(session, numOutputChannels);
		return session.output;
	}

	@Override
	public synchronized AudioDeviceInputStream createInputStream(int deviceID, int frameRate,
			int numInputChannels) {
		Session session = this.getOrCreateSession(deviceID, frameRate);
		if (session.input != null) {
			throw new RuntimeException("an ASIO input stream is already open on device '"
					+ this.getDeviceName(deviceID) + "'");
		}
		session.input = new JAsioInputStream(session, numInputChannels);
		return session.input;
	}

	private Session getOrCreateSession(int deviceID, int frameRate) {
		if (!this.isValidDeviceId(deviceID)) {
			throw new RuntimeException("not a valid ASIO device id: " + deviceID);
		}
		if (this.session == null) {
			this.session = new Session(deviceID, frameRate);
		} else if (this.session.deviceID != deviceID) {
			throw new RuntimeException("the ASIO driver '" + this.session.driverName
					+ "' is still in use, cannot switch to '" + this.getDeviceName(deviceID) + "'");
		}
		return this.session;
	}

	/**
	 * Returns { inputChannels, outputChannels } for the given device. To query a
	 * driver's channel count, it has to be loaded into memory; since the ASIO API
	 * only permits one loaded driver at a time, we probe and unload right away,
	 * and cache the result for next time. If a different driver is currently in
	 * use by an audio session, its channel numbers are all we can report.
	 */
	private int[] getChannelCounts(int deviceID) {
		int[] counts = this.channelCounts.get(deviceID);
		if (counts == null) {
			counts = this.probeChannelCounts(deviceID);
			this.channelCounts.put(deviceID, counts);
		}
		return counts;
	}

	private int[] probeChannelCounts(int deviceID) {
		String driverName = this.driverNames.get(deviceID);
		boolean weLoadedIt = false;
		try {
			if (!AsioDriver.isDriverLoaded()) {
				AsioDriver.getDriver(driverName);
				weLoadedIt = true;
			} else if (!AsioDriver.getCurrentDriver().getName().equals(driverName)) {
				// another ASIO driver is currently in use by us (or, less likely, by
				// something else in this JVM): we can't load this one to probe it
				return new int[] { 0, 0 };
			}
			AsioDriver driver = AsioDriver.getCurrentDriver();
			return new int[] { driver.getNumChannelsInput(), driver.getNumChannelsOutput() };
		} catch (RuntimeException e) {
			if (Engine.verbose) {
				Engine.printWarning("failed to query the channels of ASIO driver '" + driverName + "': "
						+ e.getMessage());
			}
			return new int[] { 0, 0 };
		} finally {
			if (weLoadedIt && AsioDriver.isDriverLoaded()) {
				try {
					AsioDriver.getCurrentDriver().shutdownAndUnloadDriver();
				} catch (RuntimeException e) {
				}
			}
		}
	}

	/**
	 * Ties one ASIO driver to the input and/or output stream that JSyn's synthesis
	 * thread created for it, and moves the driver through its lifecycle: it is
	 * loaded when the stream is created, initialised and started when JSyn starts
	 * the stream, and fully unloaded again when the stream is stopped.
	 */
	private class Session implements AsioDriverListener {

		private final int deviceID;
		private final String driverName;
		private final int frameRate;

		private final Object lock = new Object();
		// guards the native ASIO buffers against a callback that is still in
		// flight when the driver is stopped and unloaded
		private final ReentrantReadWriteLock callbackLock = new ReentrantReadWriteLock();
		// guarded by lock: set to false when the streams should wind down
		private volatile boolean running = false;
		private boolean started = false;

		private JAsioOutputStream output;
		private JAsioInputStream input;

		private AsioDriver driver;
		private AsioChannel[] outputChannels;
		private AsioChannel[] inputChannels;
		// the number of samples per channel that ASIO hands us per callback
		private int bufferSize;

		Session(int deviceID, int frameRate) {
			this.deviceID = deviceID;
			this.driverName = driverNames.get(deviceID);
			this.frameRate = frameRate;
			// load the driver eagerly (while we're still on the sketch's thread) so
			// that "driver not available" errors bubble up to Engine.selectOutputDevice
			// which can then fall back to the next candidate device
			Engine.printMessage("loading ASIO driver '" + this.driverName + "'");
			this.driver = AsioDriver.getDriver(this.driverName);
			// cache what we can see of the driver while it's loaded
			channelCounts.put(deviceID, new int[] { this.driver.getNumChannelsInput(),
					this.driver.getNumChannelsOutput() });
		}

		/**
		 * Configure, prepare and start the driver; called by the first stream that
		 * JSyn starts. The driver must be in the INITIALIZED state.
		 */
		void start() {
			synchronized (this.lock) {
				if (this.started) {
					return;
				}
				this.started = true;
				try {
					this.doStart();
					this.running = true;
				} catch (RuntimeException e) {
					this.started = false;
					throw new RuntimeException("failed to start ASIO driver '" + this.driverName + "': "
							+ e.getMessage(), e);
				}
			}
		}

		private void doStart() {
			try {
				if (this.driver.canSampleRate(this.frameRate)) {
					this.driver.setSampleRate(this.frameRate);
				} else {
					Engine.printWarning("the ASIO driver '" + this.driverName + "' does not support a "
							+ "sample rate of " + this.frameRate + " Hz, keeping its current rate of "
							+ (int) this.driver.getSampleRate() + " Hz instead");
				}
			} catch (AsioException e) {
				Engine.printWarning("failed to set the sample rate of ASIO driver '" + this.driverName
						+ " to " + this.frameRate + " Hz, keeping its current rate of "
						+ (int) this.driver.getSampleRate() + " Hz instead");
			}

			Set<AsioChannel> channelsToInit = new HashSet<AsioChannel>();
			int numOutputChannels = this.output != null ? this.output.channels : 0;
			int numInputChannels = this.input != null ? this.input.channels : 0;
			if (numOutputChannels > this.driver.getNumChannelsOutput()) {
				throw new RuntimeException("the ASIO driver '" + this.driverName + "' only provides "
						+ this.driver.getNumChannelsOutput() + " output channels");
			}
			if (numInputChannels > this.driver.getNumChannelsInput()) {
				throw new RuntimeException("the ASIO driver '" + this.driverName + "' only provides "
						+ this.driver.getNumChannelsInput() + " input channels");
			}
			this.outputChannels = new AsioChannel[numOutputChannels];
			for (int i = 0; i < numOutputChannels; i++) {
				this.outputChannels[i] = this.driver.getChannelOutput(i);
				channelsToInit.add(this.outputChannels[i]);
			}
			this.inputChannels = new AsioChannel[numInputChannels];
			for (int i = 0; i < numInputChannels; i++) {
				this.inputChannels[i] = this.driver.getChannelInput(i);
				channelsToInit.add(this.inputChannels[i]);
			}
			if (channelsToInit.isEmpty()) {
				throw new RuntimeException("no channels to open on ASIO driver '" + this.driverName + "'");
			}

			this.driver.addAsioDriverListener(this);
			// createBuffers() fixes the block size for the session (there is no way
			// to pass a custom size through the AudioDeviceManager interface)
			this.driver.createBuffers(channelsToInit);
			this.bufferSize = this.driver.getBufferPreferredSize();

			// some drivers (e.g. Realtek's) claim everything is fine but then don't
			// actually allocate any buffers for the channels they report; check for
			// that before starting the driver, otherwise the callbacks will just
			// spam the console with NullPointerExceptions
			this.checkChannelBuffers(this.outputChannels);
			this.checkChannelBuffers(this.inputChannels);

			if (this.output != null) {
				this.output.allocateBuffers();
			}
			if (this.input != null) {
				this.input.allocateBuffers();
			}

			// the driver calls back into bufferSwitch() on its own native thread
			this.driver.start();
		}

		/**
		 * Stop the driver and unload it again; safe to call more than once.
		 */
		void shutdown() {
			synchronized (this.lock) {
				this.running = false;
				this.started = false;
				this.lock.notifyAll();
			}
			// let any thread that's currently blocking in write()/read() wake up and
			// wind down before we touch the driver
			Lock writeLock = this.callbackLock.writeLock();
			writeLock.lock();
			writeLock.unlock();
			synchronized (this) {
				if (this.driver != null) {
					try {
						this.driver.shutdownAndUnloadDriver();
					} catch (RuntimeException e) {
						if (Engine.verbose) {
							Engine.printWarning("error while shutting down ASIO driver '" + this.driverName
									+ "': " + e.getMessage());
						}
					}
					this.driver = null;
				}
			}
			synchronized (JAsioAudioDeviceManager.this) {
				if (JAsioAudioDeviceManager.this.session == this) {
					JAsioAudioDeviceManager.this.session = null;
				}
			}
		}

		private void checkChannelBuffers(AsioChannel[] channels) {
			for (int i = 0; channels != null && i < channels.length; i++) {
				if (!channels[i].isActive() || channels[i].getByteBuffer() == null) {
					throw new RuntimeException("the ASIO driver '" + this.driverName + "' did not provide an "
							+ "audio buffer for " + channels[i] + ", it is probably not fully compatible "
							+ "with it - please try a different driver (e.g. the one from your audio "
							+ "interface's manufacturer or the universal ASIO4ALL one)");
				}
			}
		}

		double getLatency() {
			int ringFrames = this.output != null ? this.output.ring.frames : (this.input != null
					? this.input.ring.frames : 0);
			return (2.0 * this.bufferSize + ringFrames / 2.0) / this.frameRate;
		}

		/*
		 * ASIO callbacks, called on the driver's native thread (except the requests
		 * which may come from a driver-internal thread as well).
		 */

		@Override
		public void bufferSwitch(long systemTime, long samplePosition, Set<AsioChannel> activeChannels) {
			// hold the read lock for the whole callback so shutdown() can wait for
			// a straggling callback before it disposes of the native buffers
			Lock readLock = this.callbackLock.readLock();
			readLock.lock();
			try {
				if (!this.running) {
					return;
				}
				try {
					if (this.output != null) {
						this.output.fillAsioBuffers();
					}
					if (this.input != null) {
						this.input.drainAsioBuffers();
					}
				} catch (RuntimeException e) {
					// the buffers were disposed of under us while the driver was stopping
				}
			} finally {
				readLock.unlock();
			}
		}

		@Override
		public void sampleRateDidChange(double sampleRate) {
			Engine.printWarning("the ASIO driver '" + this.driverName + "' changed its sample rate to "
					+ (int) sampleRate + " Hz");
		}

		@Override
		public void resetRequest() {
			// can't re-initialise the driver from within a driver callback
			Engine.printWarning("the ASIO driver '" + this.driverName + "' requested a reset, trying to restart it...");
			this.restart();
		}

		@Override
		public void resyncRequest() {
			// buffer underruns: the ring buffer is refilled with silence, nothing to do
		}

		@Override
		public void bufferSizeChanged(int bufferSize) {
			Engine.printMessage("the ASIO driver '" + this.driverName + "' changed its preferred buffer "
					+ "size to " + bufferSize + ", restarting it...");
			this.restart();
		}

		@Override
		public void latenciesChanged(int inputLatency, int outputLatency) {
		}

		private void restart() {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						if (!Session.this.running || Session.this.driver == null) {
							return;
						}
						// the driver callbacks stop as soon as it leaves the RUNNING state
						Session.this.driver.returnToState(AsioDriverState.INITIALIZED);
						Session.this.doStart();
					} catch (RuntimeException e) {
						Engine.printWarning("failed to restart the ASIO driver '" + Session.this.driverName
								+ "': " + e.getMessage());
					}
				}
			}).start();
		}
	}

	/**
	 * JSyn-facing interleaved ring buffer that the synthesis thread blocks on
	 * while it is full; drained by the ASIO callback thread, one ASIO block at a
	 * time.
	 */
	private class JAsioOutputStream implements AudioDeviceOutputStream {

		private final Session session;
		private final int channels;
		private RingBuffer ring;
		// scratch buffer to de-interleave one ASIO block into, only touched by the
		// ASIO callback thread
		private float[][] blocks;

		JAsioOutputStream(Session session, int channels) {
			this.session = session;
			this.channels = channels;
		}

		void allocateBuffers() {
			int ringFrames = Math.max(1024, 2 * this.session.bufferSize);
			this.ring = new RingBuffer(ringFrames, this.channels);
			this.blocks = new float[this.channels][this.session.bufferSize];
		}

		@Override
		public void start() {
			this.session.start();
		}

		@Override
		public void stop() {
			this.session.shutdown();
		}

		@Override
		public double getLatency() {
			return this.session.getLatency();
		}

		@Override
		public void write(double value) throws IOException {
			// extremely unlikely to be used on a multi-channel device, but keep the
			// interleaved stream aligned either way
			double[] single = new double[this.channels];
			single[0] = value;
			this.write(single, 0, this.channels);
		}

		@Override
		public void write(double[] buffer) throws IOException {
			this.write(buffer, 0, buffer.length);
		}

		@Override
		public void write(double[] buffer, int start, int count) throws IOException {
			int frames = count / this.channels;
			synchronized (this.session.lock) {
				while (frames > 0 && this.session.running) {
					try {
						while (this.ring.freeFrames() == 0 && this.session.running) {
							// this is what paces JSyn's synthesis thread to the ASIO clock
							this.session.lock.wait();
						}
					} catch (InterruptedException e) {
						// JSyn is shutting down the synthesis thread
						return;
					}
					if (!this.session.running) {
						// the driver stopped under us; drop the rest instead of blocking forever
						return;
					}
					int n = Math.min(frames, this.ring.freeFrames());
					this.ring.write(buffer, start, n);
					start += n * this.channels;
					frames -= n;
					this.session.lock.notifyAll();
				}
			}
		}

		@Override
		public void close() throws IOException {
		}

		/**
		 * Called by the ASIO driver callback: hand the oldest ring buffer block(s) to
		 * the driver's output buffers, filling any shortfall with silence.
		 */
		void fillAsioBuffers() {
			int blockSize = this.session.bufferSize;
			synchronized (this.session.lock) {
				int frames = Math.min(this.ring.usedFrames(), blockSize);
				for (int c = 0; c < this.channels; c++) {
					for (int i = 0; i < frames; i++) {
						this.blocks[c][i] = (float) this.ring.peek(i, c);
					}
					for (int i = frames; i < blockSize; i++) {
						this.blocks[c][i] = 0;
					}
				}
				this.ring.discard(frames);
				this.session.lock.notifyAll();
			}
			for (int c = 0; c < this.channels; c++) {
				this.session.outputChannels[c].write(this.blocks[c]);
			}
		}
	}

	/**
	 * Fills JSyn's input side: the ASIO callback thread reads the driver's input
	 * buffers into an interleaved ring buffer which the synthesis thread blocks
	 * on while it is empty.
	 */
	private class JAsioInputStream implements AudioDeviceInputStream {

		private final Session session;
		private final int channels;
		private RingBuffer ring;
		// scratch buffer to interleave one ASIO block from, only touched by the
		// ASIO callback thread
		private float[][] blocks;
		private boolean warnedAboutOverrun = false;

		JAsioInputStream(Session session, int channels) {
			this.session = session;
			this.channels = channels;
		}

		void allocateBuffers() {
			int ringFrames = Math.max(1024, 2 * this.session.bufferSize);
			this.ring = new RingBuffer(ringFrames, this.channels);
			this.blocks = new float[this.channels][this.session.bufferSize];
		}

		@Override
		public void start() {
			this.session.start();
		}

		@Override
		public void stop() {
			this.session.shutdown();
		}

		@Override
		public double getLatency() {
			return this.session.getLatency();
		}

		/**
		 * Called by the ASIO driver callback: grab the newest samples from the
		 * driver's input buffers and push them into the ring buffer, dropping them
		 * if the synthesis thread is not keeping up.
		 */
		void drainAsioBuffers() {
			for (int c = 0; c < this.channels; c++) {
				this.session.inputChannels[c].read(this.blocks[c]);
			}
			synchronized (this.session.lock) {
				if (this.ring.freeFrames() < this.session.bufferSize) {
					if (!this.warnedAboutOverrun) {
						Engine.printWarning("the ASIO input buffer is overrunning, audio input will "
								+ "contain drop-outs");
						this.warnedAboutOverrun = true;
					}
					return;
				}
				for (int i = 0; i < this.session.bufferSize; i++) {
					for (int c = 0; c < this.channels; c++) {
						this.ring.put(this.blocks[c][i], c);
					}
				}
				this.session.lock.notifyAll();
			}
		}

		@Override
		public double read() {
			double[] single = new double[this.channels];
			this.read(single, 0, this.channels);
			return single[0];
		}

		@Override
		public int read(double[] buffer) {
			return this.read(buffer, 0, buffer.length);
		}

		@Override
		public int read(double[] buffer, int start, int count) {
			int wantedFrames = count / this.channels;
			int frames = 0;
			synchronized (this.session.lock) {
				try {
					while (frames < wantedFrames && this.session.running) {
						while (this.ring.usedFrames() == 0 && this.session.running) {
							// this blocks until the ASIO callback delivers the next block
							this.session.lock.wait();
						}
						int n = Math.min(wantedFrames - frames, this.ring.usedFrames());
						for (int i = 0; i < n; i++) {
							for (int c = 0; c < this.channels; c++) {
								buffer[start++] = this.ring.peek(i, c);
							}
						}
						frames += n;
						if (n > 0) {
							this.ring.discard(n);
							this.session.lock.notifyAll();
						}
					}
				} catch (InterruptedException e) {
					// JSyn is shutting down the synthesis thread
				}
				// JSyn de-interleaves the full requested range regardless of the
				// return value, so pad whatever is left with silence
				for (int i = frames; i < wantedFrames; i++) {
					for (int c = 0; c < this.channels; c++) {
						buffer[start++] = 0;
					}
				}
			}
			return frames * this.channels;
		}

		@Override
		public int available() {
			synchronized (this.session.lock) {
				return this.ring.usedFrames() * this.channels;
			}
		}

		@Override
		public void close() {
		}
	}

	/**
	 * Minimal circular buffer of interleaved frames; not thread-safe on its own,
	 * all accesses are guarded by the session's lock.
	 */
	private static class RingBuffer {

		private final double[] data;
		final int frames;
		private final int channels;
		private int readPos;
		private int writePos;
		private int used;

		RingBuffer(int frames, int channels) {
			this.frames = frames;
			this.channels = channels;
			this.data = new double[frames * channels];
		}

		int usedFrames() {
			return this.used;
		}

		int freeFrames() {
			return this.frames - this.used;
		}

		/** Copy the frame at index i (from the read head) and channel c. */
		double peek(int i, int c) {
			return this.data[((this.readPos + i) % this.frames) * this.channels + c];
		}

		/** Append one sample of channel c to the write head. */
		void put(double value, int c) {
			this.data[this.writePos * this.channels + c] = value;
			if (c == this.channels - 1) {
				this.writePos = (this.writePos + 1) % this.frames;
				this.used++;
			}
		}

		/** Append count interleaved frames from the source array. */
		void write(double[] src, int srcStart, int count) {
			int srcSamples = srcStart;
			for (int i = 0; i < count; i++) {
				int base = this.writePos * this.channels;
				int firstSample = srcSamples;
				int n = Math.min(this.data.length - base, (count - i) * this.channels);
				System.arraycopy(src, firstSample, this.data, base, n);
				srcSamples += n;
				int framesWritten = n / this.channels;
				i += framesWritten - 1;
				this.writePos = (this.writePos + framesWritten) % this.frames;
				this.used += framesWritten;
			}
		}

		/** Drop count frames from the read head. */
		void discard(int count) {
			this.readPos = (this.readPos + count) % this.frames;
			this.used -= count;
		}
	}
}
