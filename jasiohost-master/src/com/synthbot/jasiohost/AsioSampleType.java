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

/**
 * This class enumerates the different types of sample formats supported by ASIO.
 * The API makes a distinction between Big-Endian (denoted by <code>MSB</code>) and 
 * Little-Endian (<code>LSB</code>) encodings, which are meaningless in Java, as all 
 * numbers are Big-Endian encoded. <code>AsioSampleType</code>s are exposed for informative 
 * purposes, however they are most useful for determining the Java native type to use. 
 * The endianness of the sample is resolved in the native library.
 */
public enum AsioSampleType {
  
  /**
   * 16-bit data word
   */
  ASIOSTInt16MSB(0),
  
  /**
   * This is the packed 24 bit format. 2 data words will spawn consecutive 6 bytes in memory.
   * (Used for 18 and 20 bits as well, if they use this packed format). In Java it is represented
   * the same as any of the <code>ASIOSTInt32MSB16</code> formats.
   */
  ASIOSTInt24MSB(1),
  
  /**
   * 32-bit data word.
   */
  ASIOSTInt32MSB(2),
  
  /**
   * IEEE 754 32 bit float, as found on Intel x86 architecture.
   */
  ASIOSTFloat32MSB(3),
  
  /**
   * IEEE 754 64 bit double float, as found on Intel x86 architecture
   */
  ASIOSTFloat64MSB(4),
  
  /**
   * Sample data fills the least significant 16 bits, the other bits are sign extended.
   */
  ASIOSTInt32MSB16(8),
  
  /**
   * Sample data fills the least significant 18 bits, the other bits are sign extended.
   */
  ASIOSTInt32MSB18(9),
  
  /**
   * Sample data fills the least significant 20 bits, the other bits are sign extended.
   */
  ASIOSTInt32MSB20(10),
  
  /**
   * Sample data fills the least significant 24 bits, the other bits are sign extended.
   */
  ASIOSTInt32MSB24(11),
  ASIOSTInt16LSB(16),
  ASIOSTInt24LSB(17),
  ASIOSTInt32LSB(18),
  ASIOSTFloat32LSB(19),
  ASIOSTFloat64LSB(20),
  ASIOSTInt32LSB16(24),
  ASIOSTInt32LSB18(25),
  ASIOSTInt32LSB20(26),
  ASIOSTInt32LSB24(27),
  ASIOSTDSDInt8LSB1(32),
  ASIOSTDSDInt8MSB1(33),
  ASIOSTDSDInt8NER8(40);
  
  private int nativeEnum; // the native enum representing this type
  
  private AsioSampleType(int nativeEnum) {
    this.nativeEnum = nativeEnum;
  }
  
  public static AsioSampleType getSampleType(int nativeEnum) {
    for (AsioSampleType sampleType : values()) {
      if (sampleType.nativeEnum == nativeEnum) {
        return sampleType;
      }
    }
    return null;
  }
}
