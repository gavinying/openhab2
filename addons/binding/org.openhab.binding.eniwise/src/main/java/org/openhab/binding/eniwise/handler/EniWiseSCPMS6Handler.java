/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.eniwise.handler;

import static org.openhab.binding.eniwise.EniWiseBindingConstants.*;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.eniwise.EniWiseBindingConstants;
import org.openhab.binding.eniwise.internal.protocol.EniWiseProtocol;
import org.openhab.binding.eniwise.internal.protocol.ModbusRtuConnection;
import org.openhab.binding.eniwise.internal.protocol.ModbusEventListener;
import org.openhab.binding.eniwise.internal.protocol.ModbusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EniWiseSCPMS6Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author Ying Shaodong - Initial contribution
 */
public class EniWiseSCPMS6Handler extends BaseThingHandler implements ModbusEventListener {

    private Logger logger = LoggerFactory.getLogger(EniWiseSCPMS6Handler.class);
    private String serialPort = "unknown";
    private int baudrate = 9600;
    private byte mbAddress = -1;
	private ModbusRtuConnection connection;
	private ScheduledFuture<?> statusCheckerFuture;
	
	public EniWiseSCPMS6Handler(Thing thing) {
		super(thing);
		try {
			serialPort = (String) this.getConfig().get(PARAMETER_SERIAL_PORT);
			baudrate = ((BigDecimal) this.getConfig().get(PARAMETER_BAUD_RATE)).intValue();
			mbAddress = ((BigDecimal) this.getConfig().get(PARAMETER_MB_ADDRESS)).byteValue();
		}
		catch (Exception ex) {
			logger.error("Failed to read parameters for EniWiseSCPMS6Handler, please check the configurations.");
		}
		connection = new ModbusRtuConnection(serialPort, baudrate);
		connection.addEventListener(this);
	}

	/**
	 * Initialize the state of the EniWise Thing.
	 */
	@Override
	public void initialize() {
		//super.initialize();
		if(connection == null)
			return;
		logger.debug("Initializing handler for EniWise SCPM-S6 @ {}", serialPort);
		// Connect
		connection.connect();
		// Start the status checker
		Runnable statusChecker = new Runnable() {
			@Override
			public void run() {
				try {
					logger.debug("Checking status of EniWise SCPM-S6 @ {}", serialPort);
					queryDevice();
				} catch (LinkageError e) {
					logger.warn(
							"Failed to check the status for EniWise SCPM-S6 @ {}. If a Serial link is used to connect to the EniWise SCPM-S6, please check that the Bundle org.openhab.io.transport.serial is available. Cause: {}",
							serialPort, e.getMessage());
					// Stop to check the status of this AVR.
					if (statusCheckerFuture != null) {
						statusCheckerFuture.cancel(false);
					}
				}
			}
		};
		statusCheckerFuture = scheduler.scheduleWithFixedDelay(statusChecker, 1, 10, TimeUnit.SECONDS);
	}
	
	/**
	 * Close the connection and stop the status checker.
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (statusCheckerFuture != null) {
			statusCheckerFuture.cancel(true);
		}
		if (connection != null) {
			connection.close();
		}
	}
	
	/**
	 * Query the device and update the status.
	 */
	private void queryDevice() {
		if(!connection.isConnected()) {
			if(!connection.connect())
				return;
		}
		byte[] cmd = ModbusUtil.constructCommand3(mbAddress, EniWiseProtocol.MBREG_S6_BASE, EniWiseProtocol.MBREG_S6_LENGTH);
		if (connection.sendCommand(cmd)) {
			// If success, update its status to ONLINE
			updateStatus(ThingStatus.ONLINE);
		} else {
			// If the request has not been sent or response timeout, update its status to OFFLINE.
			updateStatus(ThingStatus.OFFLINE);
		}
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("to be implemented for handling command. ");
	}

	@Override
	public void onReceive(byte[] packet) {
		int addr = ModbusUtil.getAddress(packet);
		int func = ModbusUtil.getFuncCode(packet);
		if (func == ModbusUtil.REPORT_SLAVE_ID) {
		} else if (func == ModbusUtil.READ_HOLDING_REGISTERS) {
			logger.debug("Received an <READ_MULTIPLE_REGISTERS> response from Device (#"
							+ addr + ")");
			// Meter Configuration
			int ds = ModbusUtil.registerBEToShort(packet, ModbusUtil.MB_RESP_DATA_POS 
					+ EniWiseProtocol.OFFSET_S6_MCFG_DATA_SCALAR * 2);
			
			// Voltage
            updateState(EniWiseBindingConstants.CHANNEL_V, new DecimalType(
            		ModbusUtil.registerBEToUShort(packet, ModbusUtil.MB_RESP_DATA_POS 
            				+ EniWiseProtocol.OFFSET_S6_V * 2) 
            				* EniWiseProtocol.DATA_SCALAR_V));
			// Line Frequency
            updateState(EniWiseBindingConstants.CHANNEL_Hz, new DecimalType(
            		ModbusUtil.registerBEToUShort(packet, ModbusUtil.MB_RESP_DATA_POS 
            				+ EniWiseProtocol.OFFSET_S6_Hz * 2) 
            				* EniWiseProtocol.DATA_SCALAR_Hz));
			// Active Energy Consumption (kWh)
			updateState(EniWiseBindingConstants.CHANNEL_kWh1, new DecimalType(
					ModbusUtil.registersBEToUInt(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH1_kWh_MSW	* 2) 
							* EniWiseProtocol.DATA_SCALAR_ENERGY[ds]));
			updateState(EniWiseBindingConstants.CHANNEL_kWh2, new DecimalType(
					ModbusUtil.registersBEToUInt(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH2_kWh_MSW	* 2) 
							* EniWiseProtocol.DATA_SCALAR_ENERGY[ds]));
			updateState(EniWiseBindingConstants.CHANNEL_kWh3, new DecimalType(
					ModbusUtil.registersBEToUInt(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH3_kWh_MSW	* 2) 
							* EniWiseProtocol.DATA_SCALAR_ENERGY[ds]));
			updateState(EniWiseBindingConstants.CHANNEL_kWh4, new DecimalType(
					ModbusUtil.registersBEToUInt(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH4_kWh_MSW	* 2) 
							* EniWiseProtocol.DATA_SCALAR_ENERGY[ds]));
			updateState(EniWiseBindingConstants.CHANNEL_kWh5, new DecimalType(
					ModbusUtil.registersBEToUInt(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH5_kWh_MSW	* 2) 
							* EniWiseProtocol.DATA_SCALAR_ENERGY[ds]));
			updateState(EniWiseBindingConstants.CHANNEL_kWh6, new DecimalType(
					ModbusUtil.registersBEToUInt(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH6_kWh_MSW	* 2) 
							* EniWiseProtocol.DATA_SCALAR_ENERGY[ds]));
			// Reactive Energy Consumption (kVARh)
			updateState(EniWiseBindingConstants.CHANNEL_kVARh1, new DecimalType(
					ModbusUtil.registersBEToUInt(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH1_kVARh_MSW	* 2) 
							* EniWiseProtocol.DATA_SCALAR_ENERGY[ds]));
			updateState(EniWiseBindingConstants.CHANNEL_kVARh2, new DecimalType(
					ModbusUtil.registersBEToUInt(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH2_kVARh_MSW	* 2) 
							* EniWiseProtocol.DATA_SCALAR_ENERGY[ds]));
			updateState(EniWiseBindingConstants.CHANNEL_kVARh3, new DecimalType(
					ModbusUtil.registersBEToUInt(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH3_kVARh_MSW	* 2) 
							* EniWiseProtocol.DATA_SCALAR_ENERGY[ds]));
			updateState(EniWiseBindingConstants.CHANNEL_kVARh4, new DecimalType(
					ModbusUtil.registersBEToUInt(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH4_kVARh_MSW	* 2) 
							* EniWiseProtocol.DATA_SCALAR_ENERGY[ds]));
			updateState(EniWiseBindingConstants.CHANNEL_kVARh5, new DecimalType(
					ModbusUtil.registersBEToUInt(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH5_kVARh_MSW	* 2) 
							* EniWiseProtocol.DATA_SCALAR_ENERGY[ds]));
			updateState(EniWiseBindingConstants.CHANNEL_kVARh6, new DecimalType(
					ModbusUtil.registersBEToUInt(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH6_kVARh_MSW	* 2) 
							* EniWiseProtocol.DATA_SCALAR_ENERGY[ds]));
			// Current (A)
			updateState(EniWiseBindingConstants.CHANNEL_A1, new DecimalType(
					ModbusUtil.registerBEToShort(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH1_A * 2) 
							* EniWiseProtocol.DATA_SCALAR_CURRENT[ds]));
			updateState(EniWiseBindingConstants.CHANNEL_A2, new DecimalType(
					ModbusUtil.registerBEToShort(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH2_A * 2) 
							* EniWiseProtocol.DATA_SCALAR_CURRENT[ds]));
			updateState(EniWiseBindingConstants.CHANNEL_kWh3, new DecimalType(
					ModbusUtil.registerBEToShort(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH3_A * 2) 
							* EniWiseProtocol.DATA_SCALAR_CURRENT[ds]));
			updateState(EniWiseBindingConstants.CHANNEL_kWh4, new DecimalType(
					ModbusUtil.registerBEToShort(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH4_A * 2) 
							* EniWiseProtocol.DATA_SCALAR_CURRENT[ds]));
			updateState(EniWiseBindingConstants.CHANNEL_kWh5, new DecimalType(
					ModbusUtil.registerBEToShort(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH5_A * 2) 
							* EniWiseProtocol.DATA_SCALAR_CURRENT[ds]));
			updateState(EniWiseBindingConstants.CHANNEL_kWh6, new DecimalType(
					ModbusUtil.registerBEToShort(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH6_A * 2) 
							* EniWiseProtocol.DATA_SCALAR_CURRENT[ds]));
			// Power factor (PF)
			updateState(EniWiseBindingConstants.CHANNEL_PF1, new DecimalType(
					ModbusUtil.registerBEToUShort(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH1_PF * 2) 
							* EniWiseProtocol.DATA_SCALAR_PF));
			updateState(EniWiseBindingConstants.CHANNEL_PF2, new DecimalType(
					ModbusUtil.registerBEToUShort(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH2_PF * 2) 
							* EniWiseProtocol.DATA_SCALAR_PF));
			updateState(EniWiseBindingConstants.CHANNEL_PF3, new DecimalType(
					ModbusUtil.registerBEToUShort(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH3_PF * 2) 
							* EniWiseProtocol.DATA_SCALAR_PF));
			updateState(EniWiseBindingConstants.CHANNEL_PF4, new DecimalType(
					ModbusUtil.registerBEToUShort(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH4_PF * 2) 
							* EniWiseProtocol.DATA_SCALAR_PF));
			updateState(EniWiseBindingConstants.CHANNEL_PF5, new DecimalType(
					ModbusUtil.registerBEToUShort(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH5_PF * 2) 
							* EniWiseProtocol.DATA_SCALAR_PF));
			updateState(EniWiseBindingConstants.CHANNEL_PF6, new DecimalType(
					ModbusUtil.registerBEToUShort(packet, 
							ModbusUtil.MB_RESP_DATA_POS 
							+ EniWiseProtocol.OFFSET_S6_CH6_PF * 2) 
							* EniWiseProtocol.DATA_SCALAR_PF));

		} else if (func == ModbusUtil.WRITE_MULTIPLE_REGISTERS) {
			logger.debug("Received an <WRITE_MULTIPLE_REGISTERS> response from Device (#"
							+ addr + ")");
		} else {
			logger.warn("Received an unkown response from Device (#"
					+ addr + ") (func code = " + func + ")");
		}
	}

	@Override
	public void onConnectionClose() {
		logger.warn("EniWiseSCPMS6Handler disconnected from " + serialPort);
	}
}
