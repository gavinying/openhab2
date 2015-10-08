/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.eniwise.internal.protocol;

/**
 * The EniWiseProtocol class defines common constants, which are 
 * used for EniWise devices.
 * 
 * @author Ying Shaodong - Initial contribution
 */
public class EniWiseProtocol {
	
    // ----- Device Control Registers defined by EniWise
    public static final int MBREG_FACTORY_RESET = 40099;
    public static final int MBREG_SAVE_REBOOT = 40100;

    // ----- Device Configuration Registers defined by EniWise
    public static final int MBREG_DEVCFG_BASE = 40001;
    public static final int MBREG_DEVCFG_LENGTH = 10;

    public static final int OFFSET_DEVCFG_MAGICCODE = 0;
    public static final int OFFSET_DEVCFG_NETWORKID = 1;
    public static final int OFFSET_DEVCFG_ADDRESS = 2;
    public static final int OFFSET_DEVCFG_BAUDRATE = 3;
    
    // ----- Data Scalar defined by EniWise
    public static final double[] DATA_SCALAR_ENERGY = new double[] {0.0001, 0.001, 0.01, 0.1};
    public static final double[] DATA_SCALAR_POWER = new double[] {0.001, 0.01, 0.1, 1.0};
    public static final double[] DATA_SCALAR_CURRENT = new double[] {0.001, 0.01, 0.1, 1.0};
    public static final double DATA_SCALAR_PF = 0.01;
    public static final double DATA_SCALAR_V = 0.1;
    public static final double DATA_SCALAR_Hz = 0.01;

    // ----- Device Metering Data defined by EniWise SCPM-S6 Protocol V1
    public static final int MBREG_S6_BASE = 46001;
    public static final int MBREG_S6_LENGTH = 108;
    public static final int MBREG_S6_MCFG_LENGTH = 10;

    public static final int OFFSET_S6_MCFG_CT_TYPE = 0;
    public static final int OFFSET_S6_MCFG_CT_RATING = 1;
    public static final int OFFSET_S6_MCFG_DATA_SCALAR = 2;
    public static final int OFFSET_S6_MCFG_DEMAND_WINDOW = 3;
    public static final int OFFSET_S6_MCFG_STATUS = 4;
    public static final int OFFSET_S6_MCFG_RESET_ACC = 9;

    public static final int OFFSET_S6_CH1_kWh_MSW = 10;
    public static final int OFFSET_S6_CH1_kWh_LSW = 11;
    public static final int OFFSET_S6_CH2_kWh_MSW = 12;
    public static final int OFFSET_S6_CH2_kWh_LSW = 13;
    public static final int OFFSET_S6_CH3_kWh_MSW = 14;
    public static final int OFFSET_S6_CH3_kWh_LSW = 15;
    public static final int OFFSET_S6_CH4_kWh_MSW = 16;
    public static final int OFFSET_S6_CH4_kWh_LSW = 17;
    public static final int OFFSET_S6_CH5_kWh_MSW = 18;
    public static final int OFFSET_S6_CH5_kWh_LSW = 19;
    public static final int OFFSET_S6_CH6_kWh_MSW = 20;
    public static final int OFFSET_S6_CH6_kWh_LSW = 21;
    public static final int OFFSET_S6_CH1_kWh_P_MSW = 22;
    public static final int OFFSET_S6_CH1_kWh_P_LSW = 23;
    public static final int OFFSET_S6_CH2_kWh_P_MSW = 24;
    public static final int OFFSET_S6_CH2_kWh_P_LSW = 25;
    public static final int OFFSET_S6_CH3_kWh_P_MSW = 26;
    public static final int OFFSET_S6_CH3_kWh_P_LSW = 27;
    public static final int OFFSET_S6_CH4_kWh_P_MSW = 28;
    public static final int OFFSET_S6_CH4_kWh_P_LSW = 29;
    public static final int OFFSET_S6_CH5_kWh_P_MSW = 30;
    public static final int OFFSET_S6_CH5_kWh_P_LSW = 31;
    public static final int OFFSET_S6_CH6_kWh_P_MSW = 32;
    public static final int OFFSET_S6_CH6_kWh_P_LSW = 33;
    public static final int OFFSET_S6_CH1_kWh_N_MSW = 34;
    public static final int OFFSET_S6_CH1_kWh_N_LSW = 35;
    public static final int OFFSET_S6_CH2_kWh_N_MSW = 36;
    public static final int OFFSET_S6_CH2_kWh_N_LSW = 37;
    public static final int OFFSET_S6_CH3_kWh_N_MSW = 38;
    public static final int OFFSET_S6_CH3_kWh_N_LSW = 39;
    public static final int OFFSET_S6_CH4_kWh_N_MSW = 40;
    public static final int OFFSET_S6_CH4_kWh_N_LSW = 41;
    public static final int OFFSET_S6_CH5_kWh_N_MSW = 42;
    public static final int OFFSET_S6_CH5_kWh_N_LSW = 43;
    public static final int OFFSET_S6_CH6_kWh_N_MSW = 44;
    public static final int OFFSET_S6_CH6_kWh_N_LSW = 45;
    public static final int OFFSET_S6_CH1_kVARh_MSW = 46;
    public static final int OFFSET_S6_CH1_kVARh_LSW = 47;
    public static final int OFFSET_S6_CH2_kVARh_MSW = 48;
    public static final int OFFSET_S6_CH2_kVARh_LSW = 49;
    public static final int OFFSET_S6_CH3_kVARh_MSW = 50;
    public static final int OFFSET_S6_CH3_kVARh_LSW = 51;
    public static final int OFFSET_S6_CH4_kVARh_MSW = 52;
    public static final int OFFSET_S6_CH4_kVARh_LSW = 53;
    public static final int OFFSET_S6_CH5_kVARh_MSW = 54;
    public static final int OFFSET_S6_CH5_kVARh_LSW = 55;
    public static final int OFFSET_S6_CH6_kVARh_MSW = 56;
    public static final int OFFSET_S6_CH6_kVARh_LSW = 57;
    public static final int OFFSET_S6_CH1_kVARh_P_MSW = 58;
    public static final int OFFSET_S6_CH1_kVARh_P_LSW = 59;
    public static final int OFFSET_S6_CH2_kVARh_P_MSW = 60;
    public static final int OFFSET_S6_CH2_kVARh_P_LSW = 61;
    public static final int OFFSET_S6_CH3_kVARh_P_MSW = 62;
    public static final int OFFSET_S6_CH3_kVARh_P_LSW = 63;
    public static final int OFFSET_S6_CH4_kVARh_P_MSW = 64;
    public static final int OFFSET_S6_CH4_kVARh_P_LSW = 65;
    public static final int OFFSET_S6_CH5_kVARh_P_MSW = 66;
    public static final int OFFSET_S6_CH5_kVARh_P_LSW = 67;
    public static final int OFFSET_S6_CH6_kVARh_P_MSW = 68;
    public static final int OFFSET_S6_CH6_kVARh_P_LSW = 69;
    public static final int OFFSET_S6_CH1_kVARh_N_MSW = 70;
    public static final int OFFSET_S6_CH1_kVARh_N_LSW = 71;
    public static final int OFFSET_S6_CH2_kVARh_N_MSW = 72;
    public static final int OFFSET_S6_CH2_kVARh_N_LSW = 73;
    public static final int OFFSET_S6_CH3_kVARh_N_MSW = 74;
    public static final int OFFSET_S6_CH3_kVARh_N_LSW = 75;
    public static final int OFFSET_S6_CH4_kVARh_N_MSW = 76;
    public static final int OFFSET_S6_CH4_kVARh_N_LSW = 77;
    public static final int OFFSET_S6_CH5_kVARh_N_MSW = 78;
    public static final int OFFSET_S6_CH5_kVARh_N_LSW = 79;
    public static final int OFFSET_S6_CH6_kVARh_N_MSW = 80;
    public static final int OFFSET_S6_CH6_kVARh_N_LSW = 81;
    public static final int OFFSET_S6_CH1_kW = 82;
    public static final int OFFSET_S6_CH2_kW = 83;
    public static final int OFFSET_S6_CH3_kW = 84;
    public static final int OFFSET_S6_CH4_kW = 85;
    public static final int OFFSET_S6_CH5_kW = 86;
    public static final int OFFSET_S6_CH6_kW = 87;
    public static final int OFFSET_S6_CH1_kVAR = 88;
    public static final int OFFSET_S6_CH2_kVAR = 89;
    public static final int OFFSET_S6_CH3_kVAR = 90;
    public static final int OFFSET_S6_CH4_kVAR = 91;
    public static final int OFFSET_S6_CH5_kVAR = 92;
    public static final int OFFSET_S6_CH6_kVAR = 93;
    public static final int OFFSET_S6_CH1_A = 94;
    public static final int OFFSET_S6_CH2_A = 95;
    public static final int OFFSET_S6_CH3_A = 96;
    public static final int OFFSET_S6_CH4_A = 97;
    public static final int OFFSET_S6_CH5_A = 98;
    public static final int OFFSET_S6_CH6_A = 99;
    public static final int OFFSET_S6_CH1_PF = 100;
    public static final int OFFSET_S6_CH2_PF = 101;
    public static final int OFFSET_S6_CH3_PF = 102;
    public static final int OFFSET_S6_CH4_PF = 103;
    public static final int OFFSET_S6_CH5_PF = 104;
    public static final int OFFSET_S6_CH6_PF = 105;
    public static final int OFFSET_S6_V = 106;
    public static final int OFFSET_S6_Hz = 107;
    
}
