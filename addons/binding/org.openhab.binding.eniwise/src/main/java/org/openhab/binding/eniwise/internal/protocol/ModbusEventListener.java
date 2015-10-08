/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.eniwise.internal.protocol;

/**
 * The ModbusEventListener interface defines common interfaces for Modbus
 * event listeners.
 * 
 * @author Ying Shaodong - Initial contribution
 */
public interface ModbusEventListener {

	/**
	 * Notify all listeners on byte message receive. 
	 * 
	 * @param msg received byte format message
	 */
	public void onReceive(byte[] msg);
	
	/**
	 * Notify all listeners on connection close;
	 */
	public void onConnectionClose();
	
}
