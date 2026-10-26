/*
 *  Copyright 2009,2010,2012 Martin Roth (mhroth@gmail.com)
 * 
 *  This file is part of JAsioHost.
 *
 *  JAsioHost is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JAsioHost is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JAsioHost.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.synthbot.jasiohost;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The <code>AsioDriver</code> class represents an ASIO driver. Only one may be loaded at any
 * time. A new driver is instantiated with <code>getDriver()</code>, supplied with the name of the
 * driver to load, as derived from <code>getDriverNames()</code>.<br>
 * <br>
 * All methods may throw an <code>AsioException</code>, which is a <code>RuntimeException</code> 
 * (i.e., it is not required to be caught). The error message will have some information about
 * what has gone wrong. Generally, it is not possible to recover from such errors, except for
 * shutting down the driver.<br>
 * <br>
 * Note that most methods also throw an <code>IllegalStateException</code> if the driver is not
 * in the appropriate state for that method to be called. For instance, if <code>start()</code>
 * is called before <code>createBuffers()</code>, an <code>IllegalStateException</code> will be
 * thrown.<br>
 * <br>
 * After the driver has been instantiated with <code>getDriver()</code>, the sequence of setup calls
 * are: <code>createBuffers()</code> and then <code>start()</code>. Once the driver has been started,
 * <code>bufferSwitch()</code> will be called on any <code>AsioDriverListener</code>s and audio can be
 * sent or received from the driver. In order to shut down the driver from the <code>RUNNING</code>
 * state, call: <code>stop()</code>, <code>disposeBuffers()</code>, <code>exit()</code>, and 
 * finally <code>unloadDriver()</code> (if the driver should be fully unloaded from memory). Alternatively
 * <code>returnToState()</code> can also be used in order to return the <code>AsioDriver</code> to
 * a particular <code>AsioDriverState</code>.
 */
public class AsioDriver {
  
  private AsioDriverState currentState;
  private final List<AsioDriverListener> listeners;
  private final Set<AsioChannel> activeChannels;
  private final AsioChannel[] inputChannels;
  private final AsioChannel[] outputChannels;
  private final AsioDriverInfo driverInfo;
  
  private static AsioDriver asioDriver; // The currently loaded ASIO driver.
  private static final Set<Thread> registeredThreads; // threads registered to access the native driver
  
  private AsioDriver(String driverName) {
    registerThreadIfNecessary();
    
    boolean driverLoaded = loadDriver(driverName); // load the driver into memory
    if (!driverLoaded) {
      throw new AsioException("The driver was not successfully loaded into memory. " +
          "The Steinberg ASIO API does not indicate why.");
    }
    driverInfo = ASIOInit(); // initialise the driver
    currentState = AsioDriverState.INITIALIZED;
    activeChannels = new HashSet<AsioChannel>();
    asioDriver = this;
    
    listeners = new ArrayList<AsioDriverListener>();
    
    inputChannels = new AsioChannel[ASIOGetChannels(true)];
    for (int i = 0; i < inputChannels.length; i++) {
      inputChannels[i] = ASIOGetChannelInfo(i, true);
    }
    outputChannels = new AsioChannel[ASIOGetChannels(false)];
    for (int i = 0; i < outputChannels.length; i++) {
      outputChannels[i] = ASIOGetChannelInfo(i, false);
    }
  }
  
  /**
   * The designated ASIO driver is loaded and returned in the <code>INITIALIZED</code> state. If a driver is
   * already loaded and it is the named driver, then that driver object is returned. If the named
   * driver is different from the one currently loaded, then the currently loaded driver is shut
   * down and a new one is instantiated. There is only ever one loaded driver.
   * 
   * @param driverName  The name of the driver to load, as returned by <code>getDriverNames()</code>.
   * @return The named AsioDriver object.
   * @throws AsioException  Thrown if the driver could not be loaded into memory.
   * @throws NullPointerException  Thrown if the input is <code>null</code>.
   */
  public static AsioDriver getDriver(String driverName) {
    if (driverName == null) {
      throw new NullPointerException("The driver name cannot be null.");
    } 
    if (isDriverLoaded()) {
      if (driverName.equals(asioDriver.getName())) {
        return asioDriver;
      } else {
        asioDriver.shutdownAndUnloadDriver();
      }
    }
    if (getDriverNames().contains(driverName)) {
      asioDriver = new AsioDriver(driverName);
      return asioDriver;
    } else {
      throw new IllegalArgumentException(
          "The given driver name does not exist in the system registry. " +
          "Check AsioDriver.getDriverNames().");
    }
  }
  
  private static void registerThreadIfNecessary() {
    if (registeredThreads.isEmpty()) {
      // the first thread to access native code does not have to be registered
      // ...it does so implicitly by accessing the ASIO code for the first time
      registeredThreads.add(Thread.currentThread());
    } else if (!registeredThreads.contains(Thread.currentThread())) {
      registeredThreads.add(Thread.currentThread());
      registerThread();
    }
  }
  
  /**
   * Due to the way in which ASIO drivers are integrated into Windows (as COM objects),
   * it is necessary to register each thread which accesses any ASIO methods. Such
   * registration is done automatically for the thread which loads the <code>AsioDriver</code>
   * class (usually the first thread to access any <code>AsioDriver</code> method). Failure to register 
   * additional threads may cause exceptions, non-function of methods, or other arbitrary behaviour. 
   * A thread may register itself more than once, but this is not necessary. Registration is lightweight.
   */
  private static native void registerThread();
  
  /**
   * Returns the currently loaded <code>AsioDriver</code>. If no driver is loaded, <code>null</code>
   * is returned. Check to see if a driver is currently loaded with <code>isDriverLoaded()</code>.
   */
  public static AsioDriver getCurrentDriver() {
    return asioDriver;
  }
  
  static {
    // load jasiohost32.dll or jasiohost64.dll depending on the the bit-depth of the JVM
    System.loadLibrary("jasiohost" + System.getProperty("sun.arch.data.model"));  
    registeredThreads = new HashSet<Thread>();
  }
  
  /**
   * Returns <code>true</code> if a driver is currently loaded, <code>false</code> otherwise.
   */
  public static boolean isDriverLoaded() {
    return asioDriver != null;
  }
  
  /**
   * A list of all (maximum 32) ASIO drivers registered with the system is returned.
   */
  public static List<String> getDriverNames() {
    registerThreadIfNecessary();
    String[] driverNames = new String[32];
    int numNames = getDriverNames(driverNames);
    List<String> nameList = new ArrayList<String>(numNames);
    for (int i = 0; i < numNames; i++) {
      nameList.add(driverNames[i]);
    }
    return nameList;
  }
  private static native int getDriverNames(String[] driverNames);
  
  /*
   * Normally the driver is shut down manually. But it is put into finalize() in order to properly
   * shut down the driver in case the JVM is otherwise stopped.
   * (non-Javadoc)
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable {
    try {
      returnToState(AsioDriverState.UNLOADED);
    } finally {
      super.finalize();
    }
  }
  
  /**
   * Returns the name of the driver.
   */
  public String getName() {
    return driverInfo.getDriverName();
  }
  
  /**
   * Returns the version of the driver.
   */
  public int getVersion() {
    return driverInfo.getDriverVersion();
  }
  
  /**
   * Returns the version of ASIO which this driver uses (currently 1 or 2).
   */
  public int getAsioVersion() {
    return driverInfo.getAsioVersion();
  }
  
  /**
   * Returns the current state of the ASIO driver.
   */
  public synchronized AsioDriverState getCurrentState() {
    return currentState;
  }
  
  private native AsioDriverInfo ASIOInit();
  
  /**
   * Closes all data structures relating to the operation of the ASIO driver, though the driver
   * remains loaded in memory. Returns the driver from the <code>INITIALIZED</code> state to the 
   * <code>LOADED</code> state.
   */
  public synchronized void exit() {
    if (!AsioDriverState.INITIALIZED.equals(currentState)) {
      throw new IllegalStateException("AsioDriver must be in AsioDriverState.INITIALIZED state " +
          "in order to be initialised. The current state is: " + currentState.toString());
    }
    registerThreadIfNecessary();
    ASIOExit();
    currentState = AsioDriverState.LOADED;
  }
  private native void ASIOExit();
  
  /**
   * Open the native control panel, allowing the user to adjust the ASIO settings. A control panel
   * may not be provided by all drivers on all platforms.
   */
  public synchronized void openControlPanel() {
    if (currentState.ordinal() < AsioDriverState.INITIALIZED.ordinal()) {
      throw new IllegalStateException("The AsioDriver must be at least in the INITIALIZED state: " + 
          currentState.toString());
    }
    registerThreadIfNecessary();
    ASIOControlPanel();
  }
  private static native void ASIOControlPanel();
  
  /**
   * Returns the number of available input channels. -1 is returned if there is an error.
   * @return  The number of available input channels.
   */
  public int getNumChannelsInput() {
    if (currentState.ordinal() < AsioDriverState.INITIALIZED.ordinal()) {
      throw new IllegalStateException("The AsioDriver must be at least in the INITIALIZED state: " + 
          currentState.toString());
    }
    return inputChannels.length;
  }
  
  /**
   * Returns the number of available output channels. -1 is return if there is an error.
   * @return  The number of available output channels.
   */
  public int getNumChannelsOutput() {
    if (currentState.ordinal() < AsioDriverState.INITIALIZED.ordinal()) {
      throw new IllegalStateException("The AsioDriver must be at least in the INITIALIZED state: " + 
          currentState.toString());
    }
    return outputChannels.length;
  }
  private static native int ASIOGetChannels(boolean isInput);

  /**
   * Returns the current sample rate to which the host is set.
   * @return  The current sample rate in Hz.
   */
  public synchronized double getSampleRate() {
    if (currentState.ordinal() < AsioDriverState.INITIALIZED.ordinal()) {
      throw new IllegalStateException("The AsioDriver must be at least in the INITIALIZED state: " + 
          currentState.toString());
    }
    registerThreadIfNecessary();
    return ASIOGetSampleRate();
  }
  private static native double ASIOGetSampleRate();
  
  /**
   * Inquires of the hardware if a specific available sample rate is available.
   * @param sampleRate  The sample rate in Hz.
   * @return  True if the sample rate is supported. False otherwise.
   */
  public synchronized boolean canSampleRate(double sampleRate) {
    if (currentState.ordinal() < AsioDriverState.INITIALIZED.ordinal()) {
      throw new IllegalStateException("The AsioDriver must be at least in the INITIALIZED state: " +
          currentState.toString());
    }
    registerThreadIfNecessary();
    return ASIOCanSampleRate(sampleRate);
  }
  private static native boolean ASIOCanSampleRate(double sampleRate);
  
  /**
   * Set the hardware to the requested sample Rate. If sampleRate == 0, enable external sync.
   * The driver must be in the <code>INITIALIZED</code> state.
   * @param sampleRate  The sample rate in Hz.
   */
  public synchronized void setSampleRate(double sampleRate) {
    if (currentState.ordinal() != AsioDriverState.INITIALIZED.ordinal()) {
      throw new IllegalStateException("The AsioDriver must be in the INITIALIZED state: " + 
          currentState.toString());
    }
    registerThreadIfNecessary();
    ASIOSetSampleRate(sampleRate);
  }
  private static native void ASIOSetSampleRate(double sampleRate);
  
  /**
   * Returns the minimum supported buffer size.
   */
  public synchronized int getBufferMinSize() {
    if (currentState.ordinal() < AsioDriverState.INITIALIZED.ordinal()) {
      throw new IllegalStateException("The AsioDriver must be at least in the INITIALIZED state: " + 
          currentState.toString());
    }
    registerThreadIfNecessary();
    return ASIOGetBufferSize(0);
  }
  
  /**
   * Returns the maximum supported buffer size.
   */
  public synchronized int getBufferMaxSize() {
    if (currentState.ordinal() < AsioDriverState.INITIALIZED.ordinal()) {
      throw new IllegalStateException("The AsioDriver must be at least in the INITIALIZED state: " + currentState.toString());
    }
    registerThreadIfNecessary();
    return ASIOGetBufferSize(1);
  }
  
  /**
   * Returns the preferred buffer size. The host should attempt to use this buffer size.
   */
  public synchronized int getBufferPreferredSize() {
    if (currentState.ordinal() < AsioDriverState.INITIALIZED.ordinal()) {
      throw new IllegalStateException("The AsioDriver must be at least in the INITIALIZED state: " + currentState.toString());
    }
    registerThreadIfNecessary();
    return ASIOGetBufferSize(2);
  }
  
  /**
   * Returns the granularity at which buffer sizes may differ. Usually, the buffer size will be 
   * a power of 2; in this case, granularity will be reported as -1, signaling possible 
   * buffer sizes starting from the minimum size, increased in powers of 2 up to maximum size.
   */
  public synchronized int getBufferGranularity() {
    if (currentState.ordinal() < AsioDriverState.INITIALIZED.ordinal()) {
      throw new IllegalStateException("The AsioDriver must be at least in the INITIALIZED state: " + 
          currentState.toString());
    }
    registerThreadIfNecessary();
    return ASIOGetBufferSize(3);
  }
  
  private static native int ASIOGetBufferSize(int index);
  
  /**
   * Note: As <code>getLatencyInput()</code> will also have to include the audio buffer size of the 
   * <code>createBuffers()</code> call, the application should call this function after the buffer creation. 
   * In the case that the call occurs beforehand the driver should assume preferred buffer size. 
   * @return  The input latency in samples.
   */
  public synchronized int getLatencyInput() {
    if (currentState.ordinal() < AsioDriverState.INITIALIZED.ordinal()) {
      throw new IllegalStateException("The AsioDriver must be at least in the INITIALIZED state: " + 
          currentState.toString());
    }
    registerThreadIfNecessary();
    return ASIOGetLatencies(true);
  }
  
  /**
   * Note: As <code>getLatencyOutput()</code> will also have to include the audio buffer size of the 
   * <code>createBuffers()</code> call, the application should call this function after the buffer creation. 
   * In the case that the call occurs beforehand the driver should assume preferred buffer size.
   * @return  The output latency in samples.
   */
  public synchronized int getLatencyOutput() {
    if (currentState.ordinal() < AsioDriverState.INITIALIZED.ordinal()) {
      throw new IllegalStateException("The AsioDriver must be at least in the INITIALIZED state: " + 
        currentState.toString());
    }
    registerThreadIfNecessary();
    return ASIOGetLatencies(false);
  }
  private static native int ASIOGetLatencies(boolean isInput);
  
  /**
   * Get information about an input channel. The returned <code>AsioChannel</code> object
   * is persistent for as long as the driver is <code>INITIALIZED</code>, and can be retrieved
   * in any context.
   * @param index  The input channel index to get information about.
   * @return An <code>AsioChannel</code> object representing the requested input channel.
   * @throws IndexOutOfBoundsException  Thrown if the requested channel index is out of bounds. The
   * channel does not exist.
   */
  public synchronized AsioChannel getChannelInput(int index) {
    if (currentState.ordinal() < AsioDriverState.INITIALIZED.ordinal()) {
      throw new IllegalStateException("The AsioDriver must be at least in the INITIALIZED state: " + 
          currentState.toString());
    }
    if (index < 0 || index >= inputChannels.length) {
      throw new IndexOutOfBoundsException("The input index must be in [0," + 
          Integer.toString(inputChannels.length) + "): " + Integer.toString(index));
    }
    return inputChannels[index];
  }
  
  /**
   * Get information about an output channel. The returned <code>AsioChannel</code> object
   * is persistent for as long as the driver is <code>INITIALIZED</code>, and can be retrieved
   * in any context.
   * @param index  The output channel index to get information about.
   * @return An <code>AsioChannel</code> object representing the requested output channel.
   * @throws IndexOutOfBoundsException  Thrown if the requested channel index is out of bounds. The
   * channel does not exist.
   */
  public synchronized AsioChannel getChannelOutput(int index) {
    if (currentState.ordinal() < AsioDriverState.INITIALIZED.ordinal()) {
      throw new IllegalStateException("The AsioDriver must be at least in the INITIALIZED state: " + 
          currentState.toString());
    }
    if (index < 0 || index >= outputChannels.length) {
      throw new IndexOutOfBoundsException("The output index must be in [0," + 
          Integer.toString(outputChannels.length) + "): " + Integer.toString(index));
    }
    return outputChannels[index];
  }
  private static native AsioChannel ASIOGetChannelInfo(int index, boolean isInput);
  
  /**
   * Creates audio buffers for the set of designated channels. The buffer size is that as returned by
   * <code>getBufferPreferredSize()</code>.
   * @param channelsToInit  A <code>Set</code> of <code>AsioChannelInfo</code> objects designating the
   * input and output channels to initialise and create audio buffers for.
   */
  public synchronized void createBuffers(Set<AsioChannel> channelsToInit) {
    if (!AsioDriverState.INITIALIZED.equals(currentState)) {
      throw new IllegalStateException("The ASIO driver must be in the INITIALIZED state in order to createBuffers().");
    }
    if (channelsToInit == null) {
      throw new NullPointerException("The set of channels to initialise may not be null.");
    }
    if (channelsToInit.contains(null)) {
      throw new IllegalArgumentException("The set of channels to initialise may not contain a null value.");
    }
    if (channelsToInit.size() == 0) {
      throw new IllegalArgumentException("The set of channels to initialise may not be empty.");
    }
    
    // make a defensive copy of the the channel initialisation set
    activeChannels.addAll(channelsToInit);
    
    registerThreadIfNecessary();
    ASIOCreateBuffers(activeChannels.toArray(new AsioChannel[0]), getBufferPreferredSize());
    
    currentState = AsioDriverState.PREPARED;
  }
  private static native void ASIOCreateBuffers(AsioChannel[] channelsToInit, int bufferSize);
  
  /**
   * Remove the previously created audio buffers (with <code>createBuffers()</code>). The active
   * channels are reset; all channels become inactive.
   */
  public synchronized void disposeBuffers() {
    if (!AsioDriverState.PREPARED.equals(currentState)) {
      throw new IllegalStateException("The ASIO driver must be in the PREPARED state " +
          "in order to dispose of the audio buffers.");
    }
    for (AsioChannel channelInfo : activeChannels) {
      channelInfo.setByteBuffers(null, null); // clear the ByteBuffer references
    }
    activeChannels.clear();
    registerThreadIfNecessary();
    ASIODisposeBuffers();
    currentState = AsioDriverState.INITIALIZED;
  }
  private static native void ASIODisposeBuffers();
  
  /**
   * Start the driver. Input buffers are presented and output buffers consumed via calls to 
   * <code>bufferSwitch()</code>. Puts the <code>AsioDriver</code> into the <code>RUNNING</code> state.
   */
  public synchronized void start() {
    if (!AsioDriverState.PREPARED.equals(currentState)) {
      throw new IllegalStateException();
    }
    registerThreadIfNecessary();
    ASIOStart();
    currentState = AsioDriverState.RUNNING;
  }
  private static native void ASIOStart();
  
  /**
   * Stop the driver. Calls to <code>bufferSwitch()</code> will cease. The <code>AsioDriver</code> 
   * returns to the <code>PREPARED</code> state.
   */
  public synchronized void stop() {
    if (!AsioDriverState.RUNNING.equals(currentState)) {
      throw new IllegalStateException();
    }
    registerThreadIfNecessary();
    ASIOStop();
    currentState = AsioDriverState.PREPARED;
  }
  private static native void ASIOStop();
  
  /**
   * Loads the named driver into memory.
   * @param driverName  The ASIO driver to load.
   * @return  True if the driver was successfully loaded. False otherwise.
   */
  private static native boolean loadDriver(String driverName);
  
  private synchronized void unloadDriver() {
    if (!AsioDriverState.LOADED.equals(currentState)) {
      throw new IllegalStateException();
    }
    registerThreadIfNecessary();
    removeCurrentDriver();
    currentState = AsioDriverState.UNLOADED;
    asioDriver = null;
  }
  private static native void removeCurrentDriver();
  
  /**
   * Shutdown the ASIO driver, regardless of what state it is in. Unload it from memory. This is a 
   * convenience method for <code>returnToState(AsioDriverState.UNLOADED)</code>.
   */
  public synchronized void shutdownAndUnloadDriver() {
    returnToState(AsioDriverState.UNLOADED);
  }
  
  /**
   * Return the driver to a given state. If the target state is ahead or equal to the current state 
   * then this method has no effect.
   * @param targetState  The state to which the driver should return.
   */
  public synchronized void returnToState(AsioDriverState targetState) {
    if (targetState == null) {
      throw new NullPointerException("Target state may not be null.");
    }
    if (targetState.ordinal() < currentState.ordinal()) {
      switch (currentState) {
        case RUNNING: {
          stop();
          if (currentState.equals(targetState)) {
            break;
          }
          // allow fall-throughs
        }
        case PREPARED: {
          disposeBuffers();
          if (currentState.equals(targetState)) {
            break;
          }
        }
        case INITIALIZED: {
          exit();
          if (currentState.equals(targetState)) {
            break;
          }
        }
        case LOADED: {
          unloadDriver();
          break;
        }
        default: {
          // do nothing
        }
      }
    }
  }
  
  /**
   * Add a new <code>AsioDriverListener</code>. Listeners can only be updated while the driver
   * is in the LOADED or INITIALIZED state.
   * @param listener  A new <code>AsioDriverListener</code> which should be sent callbacks.
   */
  public synchronized void addAsioDriverListener(AsioDriverListener listener) {
    if (currentState.ordinal() < AsioDriverState.PREPARED.ordinal()) {
      if (!listeners.contains(listener)) {
        listeners.add(listener);
      }
    } else {
      throw new IllegalStateException("AsioDriverListeners can only be updated while the " +
          "AsioDriver is in the LOADED or INITIALIZED state.");
    }
  }
  
  /**
   * Unregister an <code>AsioDriverListener</code>. Listeners can only be updated while the driver
   * is in the LOADED or INITIALIZED state.
   * @param listener  A previously registered <code>AsioDriverListener</code>.
   */
  public synchronized void removeAsioDriverListener(AsioDriverListener listener) {
    if (currentState.ordinal() < AsioDriverState.PREPARED.ordinal()) {
      listeners.remove(listener);
    } else {
      throw new IllegalStateException("AsioDriverListeners can only be updated while the " +
          "AsioDriver is in the LOADED or INITIALIZED state.");
    }
  }
  
  
  /*
   * Callbacks
   */
  
  private void fireSampleRateDidChange(double sampleRate) {
    for (AsioDriverListener listener : listeners) {
      listener.sampleRateDidChange(sampleRate);
    }
  }
  
  private void fireResetRequest() {
    for (AsioDriverListener listener : listeners) {
      listener.resetRequest();
    }
  }
  
  private void fireResyncRequest() {
    for (AsioDriverListener listener : listeners) {
      listener.resyncRequest();
    }
  }
  
  private void fireBufferSizeChanged(int bufferSize) {
    for (AsioDriverListener listener : listeners) {
      listener.bufferSizeChanged(bufferSize);
    }
  }
  
  private void fireLatenciesChanged(int inputLatency, int outputLatency) {
    for (AsioDriverListener listener : listeners) {
      listener.latenciesChanged(inputLatency, outputLatency);
    }
  }
  
  private void fireBufferSwitch(long systemTime, long samplePosition, int bufferIndex) {
    for (AsioChannel channel : inputChannels) {
      if (channel.isActive()) channel.setBufferIndex(bufferIndex);
    }
    for (AsioChannel channel : outputChannels) {
      if (channel.isActive()) channel.setBufferIndex(bufferIndex);
    }
    // NOTE(mhroth): use a standard for loop in order to avoid implicitly creating iterator objects
    // as this function is called very often
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).bufferSwitch(systemTime, samplePosition, activeChannels);
    }
  }
}
