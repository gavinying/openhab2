/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.eniwise.internal.protocol;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * The ModbusRtuConnection class defines a general Modbus RTU serial port connection 
 * 
 * @author Ying Shaodong - Initial contribution
 */
public class ModbusRtuConnection {
	
	private static final Logger logger = LoggerFactory.getLogger(ModbusRtuConnection.class);
	private static final int BUFFER_MAX_SIZE = 1024;
	private static final int READ_TIMEOUT = 1800;
	
	private byte[] buffer = new byte[BUFFER_MAX_SIZE];
	private int pt_buf = 0;
	private String serialport;
	private int baudrate;
	private CommPort commPort = null;
	private boolean isConnected = false;
	private BufferedInputStream reader = null;
	private OutputStream writer = null;
	private boolean serialBusy = false;
	private int errCount = 0;
	private int currentAddr = -1;
	private List<ModbusEventListener> eventListeners = null;
	
	/**
	 * Constructor
	 * 
	 * @param port serial port to create connection
	 * @param baudrate baud rate of serial port
	 */
	public ModbusRtuConnection(String port, int baudrate) {
		this.serialport = port;
		this.baudrate = baudrate;
		eventListeners = new ArrayList<ModbusEventListener>();
	}
	
	/**
	 * Add Modbus event listener
	 * 
	 * @param listener
	 */
	public void addEventListener(ModbusEventListener listener) {
		synchronized (eventListeners) {
			eventListeners.add(listener);
		}
	}

	/**
	 * Create serial port connection
	 * 
	 * @return true if success
	 */
	public boolean connect() {
		try {
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(serialport);
			if ( portIdentifier != null && !portIdentifier.isCurrentlyOwned() ) {
				commPort = portIdentifier.open(this.getClass().getName(),3000);
				if ( commPort != null && commPort instanceof SerialPort ) {
					SerialPort serialPort = (SerialPort) commPort;
					serialPort.setSerialPortParams(baudrate,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
					reader = new BufferedInputStream(serialPort.getInputStream());
		        	writer = serialPort.getOutputStream();
		        	serialPort.addEventListener(new SerialReader());
		        	serialPort.notifyOnDataAvailable(true);
			        pt_buf = 0;
			        serialBusy = false;
			        isConnected = true;
			        logger.debug("Connected to serial port: " + serialport);
			        return true;
				}
	        }
		} catch (NoSuchPortException e) {
			logger.error("No such port: " + serialport + ". (errCount=" + errCount + ")");
		} catch (PortInUseException e) {
			logger.error("Port " + serialport + " is currently in use. (errCount=" + errCount + ")");
		} catch (UnsupportedCommOperationException e) {
			logger.error("Unsupported port configuration (errCount=" + errCount + ")");
		} catch (IOException e) {
			logger.error("IOException (errCount=" + errCount + ")");
		} catch (TooManyListenersException e) {
			logger.error("Too Many Listeners Exception (errCount=" + errCount + ")");
		}
    	errCount++;
    	writer = null;
    	return false;
	}
	
	/**
	 * A method to check if connected
	 * 
	 * @return true if connected. 
	 */
	public boolean isConnected() {
		return isConnected;
	}
	
	/**
	 * Close the connection
	 */
	public void close() {
		isConnected = false;
		try {
			if(reader != null) {
				reader.close();
				reader = null;
			}
			if(writer != null) {
				writer.close();
				writer = null;
			}
		} catch (IOException e) {
		}
		if(commPort != null) {
			commPort.close();
			commPort = null;
		}
		// notify all listeners
		synchronized (eventListeners) {
			for(ModbusEventListener l : eventListeners) {
				l.onConnectionClose();
			}
		}
	}
	
	/**
	 * Send byte command to connected serial port
	 * 
	 * @param cmd byte array command
	 * @return true if success
	 */
	public boolean sendCommand(byte[] cmd) {
		return sendCommand(cmd, READ_TIMEOUT);
	}
	
	/**
	 * Send byte command to connected serial port
	 * 
	 * @param cmd byte array command
	 * @param ttl response timeout in mill-seconds
	 * @return true if success
	 */
	public boolean sendCommand(byte[] cmd, int ttl) {
		currentAddr = ModbusUtil.getAddress(cmd);
		if(serialBusy)
			return false;
		try {
			synchronized (writer) {
				writer.write(cmd);
				writer.flush();
			}
			serialBusy = true;
			logger.debug("Sent data: " + ModbusUtil.toHexString(cmd));
		} catch (IOException e) {
			e.printStackTrace();
		}
		int timeout = ttl;
		while(timeout > 0 && serialBusy) {
			timeout--;
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
		if(timeout > 0 && !serialBusy) {
			// response received
			errCount = 0;
			// notify all listeners
			synchronized (eventListeners) {
				for(ModbusEventListener l : eventListeners) {
					l.onReceive(buffer);
				}
			}
			return true;
		}
		else {
			// response timeout
			errCount++;
			serialBusy = false;
		}
		return false;
	}
	
    /**
     * Handles the incoming message from the serial port. 
     */
	class SerialReader implements SerialPortEventListener {
		int dataLen = BUFFER_MAX_SIZE;
		boolean funcException = false;
		@Override
		public void serialEvent(SerialPortEvent arg0) {
			try {
				while (isConnected && reader.available() > 0) {
					byte b = (byte) reader.read();
					// check Slave Address
					if (pt_buf == ModbusUtil.MB_ADDR_POS) {
						if (b == currentAddr) 
							buffer[pt_buf++] = b;
						else {
							pt_buf = 0;
							dataLen = BUFFER_MAX_SIZE;
							funcException = false;
						}
					}
					// check function code
					else if(pt_buf == ModbusUtil.MB_FUNC_POS) {
						buffer[pt_buf++] = b;
						if((b & 0xf0) == 0x80) 
							funcException = true;
						else
							funcException = false;
					}
					// get data length or error code
					else if (pt_buf == ModbusUtil.MB_RESP_LENGTH_POS) {
						buffer[pt_buf++] = b;
						if(funcException == true) {
							dataLen = 0;
							logger.debug("Received exception code: " + b);
						}
						else {
							dataLen = b >= 0 ? b : (b + 256);
							//log(Logger.TYPE_DEBUG, "Received data length: " + dataLen);
						}
					}
					// verify crc value
					else if (pt_buf == dataLen + ModbusUtil.MB_RESP_DATA_POS + 1) {
						buffer[pt_buf++] = b;
						logger.debug("Received data: " + ModbusUtil.toHexString(buffer, 0, pt_buf));
						// Check crc
						byte[] expected_crc = ModbusUtil.calculateCRC(buffer, 0, ModbusUtil.MB_RESP_DATA_POS + dataLen);
						if ((expected_crc[0] == buffer[pt_buf-2]) && (expected_crc[1] == buffer[pt_buf-1])) {
							// received a correct msg
							serialBusy = false;
						}
						else {
							logger.debug("CRC check failed: expected: "
									+ ModbusUtil.toHexString(expected_crc)
									+ " / read: "
									+ ModbusUtil.toHexString(buffer, pt_buf-2, 2));
						}
						pt_buf = 0;
						dataLen = BUFFER_MAX_SIZE;
					} else {
						buffer[pt_buf++] = b;
					}
				}
			} catch (IOException e) {
				close();
			}
		}
		
	}

}
