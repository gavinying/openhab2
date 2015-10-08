/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.eniwise;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link EniWiseBindingConstants} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author Ying Shaodong - Initial contribution
 */
public class EniWiseBindingConstants {

    public static final String BINDING_ID = "eniwise";
    
    public final static Set<String> SUPPORTED_DEVICE_MODELS = ImmutableSet.of("SCPM-S16", "SCPM-M12", "SCPM-T12", "SCPM-S6");
	
    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_SCPMS6 = new ThingTypeUID(BINDING_ID, "scpms6");
    public final static ThingTypeUID THING_TYPE_SCPMT12 = new ThingTypeUID(BINDING_ID, "scpmt12");

    // List of all Parameters
    public final static String PARAMETER_SERIAL_PORT = "serialport";
    public final static String PARAMETER_BAUD_RATE = "baudrate";
    public final static String PARAMETER_MB_ADDRESS = "mbaddress";

    // List of all Channel ids
    public final static String CHANNEL_V = "V";
    public final static String CHANNEL_Hz = "Hz";
    public final static String CHANNEL_kWh1 = "kWh1";
    public final static String CHANNEL_kWh2 = "kWh2";
    public final static String CHANNEL_kWh3 = "kWh3";
    public final static String CHANNEL_kWh4 = "kWh4";
    public final static String CHANNEL_kWh5 = "kWh5";
    public final static String CHANNEL_kWh6 = "kWh6";
    public final static String CHANNEL_kVARh1 = "kVARh1";
    public final static String CHANNEL_kVARh2 = "kVARh2";
    public final static String CHANNEL_kVARh3 = "kVARh3";
    public final static String CHANNEL_kVARh4 = "kVARh4";
    public final static String CHANNEL_kVARh5 = "kVARh5";
    public final static String CHANNEL_kVARh6 = "kVARh6";
    public final static String CHANNEL_A1 = "A1";
    public final static String CHANNEL_A2 = "A2";
    public final static String CHANNEL_A3 = "A3";
    public final static String CHANNEL_A4 = "A4";
    public final static String CHANNEL_A5 = "A5";
    public final static String CHANNEL_A6 = "A6";
    public final static String CHANNEL_PF1 = "PF1";
    public final static String CHANNEL_PF2 = "PF2";
    public final static String CHANNEL_PF3 = "PF3";
    public final static String CHANNEL_PF4 = "PF4";
    public final static String CHANNEL_PF5 = "PF5";
    public final static String CHANNEL_PF6 = "PF6";
}
