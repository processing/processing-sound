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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The <code>AsioChannel</code> class represents an input or output channel available form the
 * ASIO driver. It provides information such as current state and sample type. This class also
 * encapsulates and makes available native audio buffers if it is active. Convenience methods
 * are also available to facilitate consuming audio.
 */
public class AsioChannel {

  private final int index;
  private final boolean isInput;
  private volatile boolean isActive;
  private final int channelGroup;
  private final AsioSampleType sampleType;
  private final String name;
  private final ByteBuffer[] nativeBuffers;
  private volatile int bufferIndex;
  
  private static final float MAX_INT16 = (float) 0x00007FFF;
  private static final float MAX_INT18 = (float) 0x0001FFFF;
  private static final float MAX_INT20 = (float) 0x0007FFFF;
  private static final float MAX_INT24 = (float) 0x007FFFFF;
  private static final float MAX_INT32 = (float) 0x7FFFFFFF; // Integer.MAX_VALUE
  
  private AsioChannel(int index, boolean isInput, boolean isActive, int channelGroup, AsioSampleType sampleType, String name) {
    this.index = index;
    this.isInput = isInput;
    this.isActive = isActive;
    this.channelGroup = channelGroup;
    this.sampleType = sampleType;
    this.name = name;
    nativeBuffers = new ByteBuffer[2];
  }
  
  public int getChannelIndex() {
    return index;
  }
  
  public boolean isInput() {
    return isInput;
  }
  
  public boolean isActive() {
    return isActive;
  }
  
  public int getChannelGroup() {
    return channelGroup;
  }
  
  public AsioSampleType getSampleType() {
    return sampleType;
  }
  
  public String getChannelName() {
    return name;
  }
  
  /**
   * Returns the current buffer to read or write from, with the position reset to zero. The endian-ness
   * of the buffer and of the underlying system has been accounted for. Note that input buffers 
   * <code>isInput()</code> are read-only.
   */
  public ByteBuffer getByteBuffer() {
    return nativeBuffers[bufferIndex];
  }

  protected void setBufferIndex(int bufferIndex) {
    this.bufferIndex = bufferIndex;
    // guard against drivers which fire a straggling bufferSwitch callback while
    // or after their buffers have been disposed of (the buffers are cleared to
    // null by setByteBuffers() in that case)
    if (nativeBuffers[bufferIndex] != null) {
      nativeBuffers[bufferIndex].rewind(); // reset position to start of buffer
    }
  }
  
  protected void setByteBuffers(ByteBuffer buffer0, ByteBuffer buffer1) {
    if (buffer0 == null || buffer1 == null) {
      // the ByteBuffer references are cleared
      isActive = false;
      nativeBuffers[0] = null;
      nativeBuffers[1] = null;
    } else {
      nativeBuffers[0] = isInput ? buffer0.asReadOnlyBuffer() : buffer0;
      nativeBuffers[1] = isInput ? buffer1.asReadOnlyBuffer() : buffer1;
      if (sampleType.name().contains("MSB")) {
        nativeBuffers[0].order(ByteOrder.BIG_ENDIAN); // set the endian-ness of the buffers
        nativeBuffers[1].order(ByteOrder.BIG_ENDIAN); // according to the sample type      
      } else {
        nativeBuffers[0].order(ByteOrder.LITTLE_ENDIAN);
        nativeBuffers[1].order(ByteOrder.LITTLE_ENDIAN);
      }
      isActive = true;
    }
  }
  
  /**
   * A convenience method to write a <code>float</code> array of samples to the output. The array 
   * values are expected to be bounded to the range of [-1,1]. The need to convert to the correct 
   * sample type is abstracted. The <code>output</code> array should be same size as the buffer.
   * If it is larger, then a <code>BufferOverflowException</code> will be thrown. If it is smaller,
   * the buffer will be incompletely filled.
   * 
   * If the ASIO host does not use <code>float</code>s to represent samples, then the <code>AsioChannel</code>'s
   * <code>ByteBuffer</code> should be directly manipulated. Use <code>getByteBuffer</code> to access the buffer.
   * @param output  A <code>float</code> array to write to the output.
   */
  public void write(float[] output) {
    if (isInput) {
      throw new IllegalStateException("Only output channels can be written to.");
    }
    if (!isActive) {
      throw new IllegalStateException("This channel is not active: " + toString());
    }
    ByteBuffer outputBuffer = getByteBuffer();
    switch (sampleType) {
      case ASIOSTFloat64MSB:
      case ASIOSTFloat64LSB: {
        for (float sampleValue : output) {
          outputBuffer.putDouble((double) sampleValue);          
        }
        break;
      }
      case ASIOSTFloat32MSB:
      case ASIOSTFloat32LSB: {
        for (float sampleValue : output) {
          outputBuffer.putFloat(sampleValue);          
        }
        break;
      }
      case ASIOSTInt32MSB:
      case ASIOSTInt32LSB: {
        for (float sampleValue : output) {
          outputBuffer.putInt((int) (sampleValue * MAX_INT32));          
        }
        break;
      }
      case ASIOSTInt32MSB16:
      case ASIOSTInt32LSB16: {
        for (float sampleValue : output) {
          outputBuffer.putInt((int) (sampleValue * MAX_INT16));          
        }
        break;
      }
      case ASIOSTInt32MSB18:
      case ASIOSTInt32LSB18: {
        for (float sampleValue : output) {
          outputBuffer.putInt((int) (sampleValue * MAX_INT18));          
        }
        break;
      }
      case ASIOSTInt32MSB20:
      case ASIOSTInt32LSB20: {
        for (float sampleValue : output) {
          outputBuffer.putInt((int) (sampleValue * MAX_INT20));          
        }
        break;
      }
      case ASIOSTInt32MSB24:
      case ASIOSTInt32LSB24: {
        for (float sampleValue : output) {
          outputBuffer.putInt((int) (sampleValue * MAX_INT24));          
        }
        break;
      }
      case ASIOSTInt16MSB:
      case ASIOSTInt16LSB: {
        for (float sampleValue : output) {
          outputBuffer.putShort((short) (sampleValue * MAX_INT16));          
        }
        break;
      }
      case ASIOSTInt24MSB: {
        for (float sampleValue : output) {
          int sampleValueInt = (int) (sampleValue * MAX_INT24);
          outputBuffer.put((byte) ((sampleValueInt >> 16) & 0xFF));
          outputBuffer.put((byte) ((sampleValueInt >> 8) & 0xFF));
          outputBuffer.put((byte) (sampleValueInt & 0xFF));          
        }
        break;
      }
      case ASIOSTInt24LSB: {
        for (float sampleValue : output) {
          int sampleValueInt = (int) (sampleValue * MAX_INT24);
          outputBuffer.put((byte) (sampleValueInt & 0xFF));
          outputBuffer.put((byte) ((sampleValueInt >> 8) & 0xFF));
          outputBuffer.put((byte) ((sampleValueInt >> 16) & 0xFF));
        }
        break;
      }
      case ASIOSTDSDInt8MSB1:
      case ASIOSTDSDInt8LSB1:
      case ASIOSTDSDInt8NER8: {
        throw new IllegalStateException(
            "The sample types ASIOSTDSDInt8MSB1, ASIOSTDSDInt8LSB1, and ASIOSTDSDInt8NER8 are not supported.");
      }
    }
  }
  
  /**
   * A convenience method to read samples from the input buffer to a <code>float</code> array. 
   * The argument array must have the same length as the configured buffer size. The returned samples
   * are bounded to within [-1,1]. The <code>input</code> array should be the same size as the input
   * array. If it larger, then a <code>BufferUnderflowException</code> will be thrown. If it is smaller,
   * then the buffer will be incompletely read.
   * @param input  A <code>float</code> array to read into.
   */
  public void read(float[] input) {
    if (!isInput) {
      throw new IllegalStateException("Only input channels can be read from.");
    }
    if (!isActive) {
      throw new IllegalStateException("This channel is not active: " + toString());
    }
    ByteBuffer inputBuffer = getByteBuffer();
    switch (sampleType) {
      case ASIOSTFloat64MSB:
      case ASIOSTFloat64LSB: {
        for (int i = 0; i < input.length; i++) {
          input[i] = (float) inputBuffer.getDouble();          
        }
        break;
      }
      case ASIOSTFloat32MSB:
      case ASIOSTFloat32LSB: {
        for (int i = 0; i < input.length; i++) {
          input[i] = inputBuffer.getFloat();          
        }
        break;
      }
      case ASIOSTInt32MSB:
      case ASIOSTInt32LSB: {
        for (int i = 0; i < input.length; i++) {
          input[i] = ((float) inputBuffer.getInt()) / MAX_INT32;          
        }
        break;
      }
      case ASIOSTInt32MSB16:
      case ASIOSTInt32LSB16: {
        for (int i = 0; i < input.length; i++) {
          input[i] = ((float) inputBuffer.getInt()) / MAX_INT16;          
        }
        break;
      }
      case ASIOSTInt32MSB18:
      case ASIOSTInt32LSB18: {
        for (int i = 0; i < input.length; i++) {
          input[i] = ((float) inputBuffer.getInt()) / MAX_INT18;          
        }
        break;
      }
      case ASIOSTInt32MSB20:
      case ASIOSTInt32LSB20: {
        for (int i = 0; i < input.length; i++) {
          input[i] = ((float) inputBuffer.getInt()) / MAX_INT20;          
        }
        break;
      }
      case ASIOSTInt32MSB24:
      case ASIOSTInt32LSB24: {
        for (int i = 0; i < input.length; i++) {
          input[i] = ((float) inputBuffer.getInt()) / MAX_INT24;          
        }
        break;
      }
      case ASIOSTInt16MSB:
      case ASIOSTInt16LSB: {
        for (int i = 0; i < input.length; i++) {
          input[i] = ((float) inputBuffer.getShort()) / MAX_INT16;          
        }
        break;
      }
      case ASIOSTInt24MSB: {
        for (int i = 0; i < input.length; i++) {
          int sampleValueInt = ((int) inputBuffer.get()) & 0xFFFF; sampleValueInt <<= 8;
          sampleValueInt |= ((int) inputBuffer.get()) & 0xFF; sampleValueInt <<= 8;
          sampleValueInt |= ((int) inputBuffer.get()) & 0xFF;
          input[i] = ((float) sampleValueInt) / MAX_INT24;          
        }
        break;
      }
      case ASIOSTInt24LSB: {
        for (int i = 0; i < input.length; i++) {
          int sampleValueInt = ((int) inputBuffer.get()) & 0xFF;
          sampleValueInt |= (((int) inputBuffer.get()) & 0xFF) << 8;
          sampleValueInt |= (((int) inputBuffer.get()) & 0xFFFF) << 16;
          input[i] = ((float) sampleValueInt) / MAX_INT24;          
        }
        break;
      }
      case ASIOSTDSDInt8MSB1:
      case ASIOSTDSDInt8LSB1:
      case ASIOSTDSDInt8NER8: {
        throw new IllegalStateException(
            "The sample types ASIOSTDSDInt8MSB1, ASIOSTDSDInt8LSB1, and ASIOSTDSDInt8NER8 are not supported.");
      }
    }
  }

  /*
   * equals() is overridden such that it may be used in a Set
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AsioChannel)) {
      return false;
    } else {
      AsioChannel channelInfo = (AsioChannel) o;
      return (channelInfo.getChannelIndex() == index && channelInfo.isInput() == isInput);
    }
  }
  
  /*
   * hashCode() overridden in order to accompany equals()
   */
  @Override
  public int hashCode() {
    return isInput ? index : ~index + 1; // : 2's complement
  }
  
  /**
   * Returns a string description of the channel in the format, 
   * "Output Channel 0: Analog Out 1/2 Delta-AP [1], ASIOSTInt32LSB, group 0, inactive"
   */
  @Override
  public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(isInput ? "Input" : "Output");
	sb.append(" Channel "); sb.append(Integer.toString(index));
	sb.append(": "); sb.append(name);
	sb.append(", "); sb.append(sampleType.toString());
	sb.append(", group "); sb.append(Integer.toString(channelGroup));
	sb.append(", "); sb.append(isActive ? "active" : "inactive");
	return sb.toString();
  }  
}
