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
 * This is a protected class meant only to encapsulate the returned values from 
 * <code>AsioDriver.ASIOInit()</code>.
 */
public class AsioDriverInfo {

	private final int asioVersion;
	private final int driverVersion;
	private final String driverName;
	private final String errorMessage;
	
	protected AsioDriverInfo(int asioVersion, int driverVersion, String driverName, String errorMessage) {
		this.asioVersion = asioVersion;
		this.driverVersion = driverVersion;
		this.driverName = driverName;
		this.errorMessage = errorMessage;
	}
	
	public int getAsioVersion() {
		return asioVersion;
	}
	
	public int getDriverVersion() {
		return driverVersion;
	}
	
	public String getDriverName() {
		return driverName;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("=== ASIO Driver Information ===\n");
		sb.append("ASIO Version: "); sb.append(Integer.toString(asioVersion)); sb.append("\n");
		sb.append("Driver Version: "); sb.append(Integer.toString(driverVersion)); sb.append("\n");
		sb.append("Driver Name: "); sb.append(driverName); sb.append("\n");
		sb.append("Error Message: "); sb.append(errorMessage);
		return sb.toString();
	}
	
}
