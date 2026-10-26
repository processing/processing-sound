/*
 *  Copyright 2009 Martin Roth (mhroth@gmail.com)
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

import java.util.Set;

/**
 * This interface defines the callbacks that an ASIO driver may make to the host.
 */
public interface AsioDriverListener {

  /**
   * The sample rate has changed. This may be due to a user initiated change, or a change in input/output
   * source.
   * @param sampleRate  The new sample rate.
   */
  public void sampleRateDidChange(double sampleRate);

  /**
   * The driver requests a reset in the case of an unexpected failure or a device
   * reconfiguration. As this request is being made in a callback, the driver should
   * only be reset after this callback method has returned. The recommended way to reset
   * the driver is:
   * <pre><code>
   * public void resetRequest() {
   *   new Thread() {
   *     @Override
   *     public void run() {
   * 	     AsioDriver.getDriver().returnToState(AsioDriverState.INITIALIZED);
   *     }
   *   }.start();
   * }
   * </code></pre>
   * Because all methods are synchronized, this approach will safely return the driver
   * to the <code>INITIALIZED</code> state as soon as possible. The buffers must then be recreated
   * and the driver restarted.
   */
  public void resetRequest();
  
  /**
   * The driver detected audio buffer underruns and requires a resynchronization.
   */
  public void resyncRequest();
  
  /**
   * The driver has a new preferred buffer size. The host should make an effort to
   * accommodate the driver by returning to the <code>INITIALIZED</code> state and calling 
   * <code>AsioDriver.createBuffers()</code>.
   * @param bufferSize  The new preferred buffer size.
   */
  public void bufferSizeChanged(int bufferSize);
  
  /**
   * The input or output latencies have changed. The host is updated with the new values.
   * @param inputLatency  The new input latency in milliseconds.
   * @param outputLatency  The new output latency in milliseconds.
   */
  public void latenciesChanged(int inputLatency, int outputLatency);

  /**
   * The next block of samples is ready. Input buffers are filled with new input,
   * and output buffers should be filled at the end of this method.
   * @param sampleTime  System time related to sample position, in nanoseconds.
   * @param samplePosition  Sample position since <code>start()</code> was called.
   * @param activeChannels  The set of channels which are active and have allocated buffers. Retrieve
   * the buffers with <code>AsioChannel.getBuffer()</code>, or use <code>AsioChannel.read()</code>
   * and <code>AsioDriver.write()</code> in order to easily work with <code>float</code> arrays.
   */
  public void bufferSwitch(long sampleTime, long samplePosition, Set<AsioChannel> activeChannels);
}
