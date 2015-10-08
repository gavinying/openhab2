/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.eniwise.internal.protocol;

/**
 * The ModbusUtil class defines common util methods, which are 
 * used for Modbus RTU communication.
 * 
 * @author Ying Shaodong - Initial contribution
 */
public class ModbusUtil {

	public static final int MB_ADDRESS_BROADCAST = 0;
	public static final int MB_ADDRESS_MIN = 1;
	public static final int MB_ADDRESS_MAX = 247;

	public static final int MB_ADDR_POS = 0;
	public static final int MB_FUNC_POS = 1;
	public static final int MB_RESP_LENGTH_POS = 2;
	public static final int MB_RESP_DATA_POS = 3;

	public static final byte READ_COILS = 1;
	public static final byte READ_INPUT_DISCRETES = 2;
	public static final byte READ_HOLDING_REGISTERS = 3;
	public static final byte READ_INPUT_REGISTERS = 4;
	public static final byte WRITE_COIL = 5;
	public static final byte WRITE_SINGLE_REGISTER = 6;
	public static final byte WRITE_MULTIPLE_REGISTERS = 16;
	public static final byte REPORT_SLAVE_ID = 17;

	/**
	 * Construct Modbus RTU READ_HOLDING_REGISTERS(3) command
	 * 
	 * @param addr Modbus address of the device
	 * @param offset offset of the first register to read
	 * @param length number of registers to read
	 * @return byte format command
	 */
	public static byte[] constructCommand3(byte addr, int offset, int length) {
		byte[] cmd = new byte[] { addr, READ_HOLDING_REGISTERS, 0, 0, 0, 0, 0,
				0 };
		byte[] off = unsignedShortToRegister(offset);
		System.arraycopy(off, 0, cmd, 2, 2);
		byte[] len = unsignedShortToRegister(length);
		System.arraycopy(len, 0, cmd, 4, 2);
		byte[] crc = calculateCRC(cmd, 0, 6);
		System.arraycopy(crc, 0, cmd, 6, 2);
		return cmd;
	}

	/**
	 * Construct Modbus RTU READ_INPUT_REGISTERS(4) command
	 * 
	 * @param addr Modbus address of the device
	 * @param offset offset of the first register to read
	 * @param length number of registers to read
	 * @return byte format command
	 */
	public static byte[] constructCommand4(byte addr, int offset, int length) {
		byte[] cmd = new byte[] { addr, READ_INPUT_REGISTERS, 0, 0, 0, 0, 0, 0 };
		byte[] off = unsignedShortToRegister(offset);
		System.arraycopy(off, 0, cmd, 2, 2);
		byte[] len = unsignedShortToRegister(length);
		System.arraycopy(len, 0, cmd, 4, 2);
		byte[] crc = calculateCRC(cmd, 0, 6);
		System.arraycopy(crc, 0, cmd, 6, 2);
		return cmd;
	}

	/**
	 * Construct Modbus RTU WRITE_SINGLE_REGISTER(6) command
	 * 
	 * @param addr Modbus address of the device
	 * @param offset offset of the register to write
	 * @param value the value to write
	 * @return byte format command
	 */
	public static byte[] constructCommand6(byte addr, int offset, int value) {
		byte[] cmd = new byte[] { addr, WRITE_SINGLE_REGISTER, 0, 0, 0, 0, 0, 0 };
		byte[] off = unsignedShortToRegister(offset);
		System.arraycopy(off, 0, cmd, 2, 2);
		byte[] val = unsignedShortToRegister(value);
		System.arraycopy(val, 0, cmd, 4, 2);
		byte[] crc = calculateCRC(cmd, 0, 6);
		System.arraycopy(crc, 0, cmd, 6, 2);
		return cmd;
	}

	/**
	 * Construct Modbus RTU WRITE_MULTIPLE_REGISTERS(16) command
	 *  
	 * @param addr Modbus address of the device
	 * @param offset offset of the first register to write
	 * @param regCount number of registers to write
	 * @param value array of values to write
	 * @return byte format command
	 */
	public static byte[] constructCommand16(byte addr, int offset,
			int regCount, int[] value) {
		byte[] cmd = new byte[9 + regCount * 2];
		cmd[0] = addr;
		cmd[1] = WRITE_MULTIPLE_REGISTERS;
		byte[] off = unsignedShortToRegister(offset);
		System.arraycopy(off, 0, cmd, 2, 2);
		byte[] len = unsignedShortToRegister(regCount);
		System.arraycopy(len, 0, cmd, 4, 2);
		cmd[6] = lowByte(regCount * 2);
		for (int i = 0; i < regCount; i++) {
			byte[] v = unsignedShortToRegister(value[i]);
			System.arraycopy(v, 0, cmd, 7 + i * 2, 2);
		}
		byte[] crc = calculateCRC(cmd, 0, 7 + regCount * 2);
		System.arraycopy(crc, 0, cmd, 7 + regCount * 2, 2);
		return cmd;
	}

	/**
	 * Construct Modbus RTU REPORT_SLAVE_ID(17) command
	 * 
	 * @param addr Modbus address of the device 
	 * @return byte format command
	 */
	public static byte[] constructCommand17(byte addr) {
		byte[] cmd = new byte[] { addr, REPORT_SLAVE_ID, 0, 0 };
		byte[] crc = calculateCRC(cmd, 0, 2);
		System.arraycopy(crc, 0, cmd, 2, 2);
		return cmd;
	}

	/**
	 * Get Modbus address from a byte message
	 * 
	 * @param packet
	 * @return
	 */
	public static int getAddress(byte[] packet) {
		return (int) packet[MB_ADDR_POS] & 0xFF;
	}

	public static int getFuncCode(byte[] packet) {
		return (int) packet[MB_FUNC_POS] & 0xFF;
	}

	/**
	 * Returns the given byte[] as hex encoded string.
	 *
	 * @param data a byte[] array.
	 * @return a hex encoded String.
	 */
	public static final String toHexString(byte[] data) {
		return toHexString(data, 0, data.length);
	}// toHex

	/**
	 * Returns a <tt>String</tt> containing unsigned hexadecimal numbers as
	 * digits. The <tt>String</tt> will coontain two hex digit characters for
	 * each byte from the passed in <tt>byte[]</tt>.
	 *
	 * @param data
	 *            the array of bytes to be converted into a hex-string.
	 * @param off
	 *            the offset to start converting from.
	 * @param length
	 *            the number of bytes to be converted.
	 *
	 * @return the generated hexadecimal representation as <code>String</code>.
	 */
	public static final String toHexString(byte[] data, int off, int length) {
		// double size, two bytes (hex range) for one byte
		StringBuffer buf = new StringBuffer(data.length * 2);
		for (int i = off; i < off + length; i++) {
			// don't forget the second hex digit
			if (((int) data[i] & 0xff) < 0x10) {
				buf.append("0");
			}
			buf.append(Long.toString((int) data[i] & 0xff, 16));
			//if (i < data.length - 1) {
			//	buf.append(" ");
			//}
		}
		return buf.toString().toUpperCase();
	}// toHex

	/**
	 * Returns a <tt>byte[]</tt> containing the given byte as unsigned
	 * hexadecimal number digits.
	 * <p/>
	 *
	 * @param i the int to be converted into a hex string.
	 * @return the generated hexadecimal representation as <code>byte[]</code>.
	 */
	public static final byte[] toHex(int i) {
		StringBuffer buf = new StringBuffer(2);
		// don't forget the second hex digit
		if (((int) i & 0xff) < 0x10) {
			buf.append("0");
		}
		buf.append(Long.toString((int) i & 0xff, 16).toUpperCase());
		return buf.toString().getBytes();
	}// toHex

	
	/**
	 * Convert the Big-endian register (16-bit value) at the given index into a signed
	 * <tt>short</tt>.
	 * 
	 * @param bytes byte format data
	 * @param idx an offset into the given bytes
	 * @return the signed short as <tt>short</tt>.
	 */
	public static final short registerBEToShort(byte[] bytes, int idx) {
		return (short) (((bytes[idx] & 0xff) << 8) | (bytes[idx + 1] & 0xff));
	}
	
	/**
	 * Convert the Little-endian register (16-bit value) at the given index into a signed
	 * <tt>short</tt>.
	 * 
	 * @param bytes byte format data
	 * @param idx an offset into the given bytes
	 * @return the signed short as <tt>short</tt>.
	 */
	public static final short registerLEToShort(byte[] bytes, int idx) {
		return (short) (((bytes[idx + 1] & 0xff) << 8) | (bytes[idx] & 0xff));
	}

	/**
	 * Convert the Big-endian register (16-bit value) at the given index into an unsigned
	 * <tt>short</tt>.
	 * 
	 * @param bytes byte format data
	 * @param idx an offset into the given bytes
	 * @return the unsigned short as <tt>int</tt>.
	 */
	public static final int registerBEToUShort(byte[] bytes, int idx) {
		return ((bytes[idx] & 0xff) << 8) | (bytes[idx + 1] & 0xff);
	}

	/**
	 * Convert the Little-endian register (16-bit value) at the given index into an unsigned
	 * <tt>short</tt>.
	 * 
	 * @param bytes byte format data
	 * @param idx an offset into the given bytes
	 * @return the unsigned short as <tt>int</tt>.
	 */
	public static final int registerLEToUShort(byte[] bytes, int idx) {
		return ((bytes[idx + 1] & 0xff) << 8) | (bytes[idx] & 0xff);
	}

	/**
	 * Convert the given unsigned short into a register (2 bytes). The byte
	 * values in the register。 
	 * 
	 * @param v the unsigned short as <tt>int</tt>.
	 * @return the register as <tt>byte[2]</tt>.
	 */
	public static final byte[] unsignedShortToRegister(int v) {
		byte[] register = new byte[2];
		register[0] = (byte) (0xff & (v >> 8));
		register[1] = (byte) (0xff & v);
		return register;
	}

	/**
	 * Convert the given signed short into a register (2 bytes). The byte
	 * values in the register。 
	 * 
	 * @param v the signed short as <tt>short</tt>.
	 * @return the register as <tt>byte[2]</tt>.
	 */
	public static final byte[] shortToRegister(short s) {
		byte[] register = new byte[2];
		register[0] = (byte) (0xff & (s >> 8));
		register[1] = (byte) (0xff & s);
		return register;
	}

	
	/**
	 * Convert the Big-endian register (4-byte value) at the given index into a signed
	 * <tt>int</tt>.
	 * 
	 * @param bytes byte format data
	 * @param idx an offset into the given bytes
	 * @return the signed int as <tt>int</tt>.
	 */
	public static final int registersBEToInt(byte[] bytes, int idx) {
		return ((bytes[idx] & 0xff) << 24) | ((bytes[idx + 1] & 0xff) << 16)
				| ((bytes[idx + 2] & 0xff) << 8) | (bytes[idx + 3] & 0xff);
	}

	/**
	 * Convert the Big-endian register (4-byte value) at the given index into an unsigned
	 * <tt>int</tt>.
	 * 
	 * @param bytes byte format data
	 * @param idx an offset into the given bytes
	 * @return the unsigned int as <tt>long</tt>.
	 */
	public static final long registersBEToUInt(byte[] bytes, int idx) {
		long v = registersBEToInt(bytes, idx);
		return v < 0 ? v + 0x100000000L : v;
	}

	/**
	 * Convert the Little-endian register (4-byte value) at the given index into a signed
	 * <tt>int</tt>.
	 * 
	 * @param bytes byte format data
	 * @param idx an offset into the given bytes
	 * @return the signed int as <tt>int</tt>.
	 */
	public static final int registersLEToInt(byte[] bytes, int idx) {
		return ((bytes[idx] & 0xff) | ((bytes[idx + 1] & 0xff) << 8)
				| ((bytes[idx + 2] & 0xff) << 16) | ((bytes[idx + 3] & 0xff) << 24));
	}

	/**
	 * Convert the Little-endian register (4-byte value) at the given index into an unsigned
	 * <tt>int</tt>.
	 * 
	 * @param bytes byte format data
	 * @param idx an offset into the given bytes
	 * @return the unsigned int as <tt>long</tt>.
	 */
	public static final long registersLEToUInt(byte[] bytes, int idx) {
		long v = registersLEToInt(bytes, idx);
		return v < 0 ? v + 0x100000000L : v;
	}

	/**
	 * Convert the Middle-endian register (4-byte value) at the given index into a signed
	 * <tt>int</tt>.
	 * 
	 * @param bytes byte format data
	 * @param idx an offset into the given bytes
	 * @return the signed int as <tt>int</tt>.
	 */
	public static final int registersMEToInt(byte[] bytes, int idx) {
		return ((bytes[idx] & 0xff) << 8) | (bytes[idx + 1] & 0xff)
				| ((bytes[idx + 2] & 0xff) << 24)
				| ((bytes[idx + 3] & 0xff) << 16);
	}

	/**
	 * Convert the Middle-endian register (4-byte value) at the given index into an unsigned
	 * <tt>int</tt>.
	 * 
	 * @param bytes byte format data
	 * @param idx an offset into the given bytes
	 * @return the unsigned int as <tt>long</tt>.
	 */
	public static final long registersMEToUInt(byte[] bytes, int idx) {
		long v = registersMEToInt(bytes, idx);
		return v < 0 ? v + 0x100000000L : v;
	}

	/**
	 * Convert an int value to a Big-endian byte[4] array.
	 *
	 * @param v the value to be converted.
	 * @return a byte[4] containing the value.
	 */
	public static final byte[] intToBERegisters(int v) {
		byte[] registers = new byte[4];
		registers[0] = (byte) (0xff & (v >> 24));
		registers[1] = (byte) (0xff & (v >> 16));
		registers[2] = (byte) (0xff & (v >> 8));
		registers[3] = (byte) (0xff & v);
		return registers;
	}

	/**
	 * Convert an int value to a Little-endian byte[4] array.
	 *
	 * @param v the value to be converted.
	 * @return a byte[4] containing the value.
	 */
	public static final byte[] intToLERegisters(int v) {
		byte[] registers = new byte[4];
		registers[0] = (byte) (0xff & v);
		registers[1] = (byte) (0xff & (v >> 8));
		registers[2] = (byte) (0xff & (v >> 16));
		registers[3] = (byte) (0xff & (v >> 24));
		return registers;
	}

	/**
	 * Converts a Big-endian byte[8] binary long value into a long primitive.
	 *
	 * @param bytes a byte[8] containing a long value.
	 * @return a long value.
	 */
	public static final long registersBEToLong(byte[] bytes, int idx) {
		long v = ((((long) (bytes[idx] & 0xff) << 56)
				| ((long) (bytes[idx + 1] & 0xff) << 48)
				| ((long) (bytes[idx + 2] & 0xff) << 40)
				| ((long) (bytes[idx + 3] & 0xff) << 32)
				| ((long) (bytes[idx + 4] & 0xff) << 24)
				| ((long) (bytes[idx + 5] & 0xff) << 16)
				| ((long) (bytes[idx + 6] & 0xff) << 8) 
				| ((long) (bytes[idx + 7] & 0xff))));
		return v;
	}

	/**
	 * Converts a long value to a byte[8].
	 *
	 * @param v the value to be converted.
	 * @return a byte[8] containing the long value.
	 */
	public static final byte[] longToRegisters(long v) {
		byte[] registers = new byte[8];
		registers[0] = (byte) (0xff & (v >> 56));
		registers[1] = (byte) (0xff & (v >> 48));
		registers[2] = (byte) (0xff & (v >> 40));
		registers[3] = (byte) (0xff & (v >> 32));
		registers[4] = (byte) (0xff & (v >> 24));
		registers[5] = (byte) (0xff & (v >> 16));
		registers[6] = (byte) (0xff & (v >> 8));
		registers[7] = (byte) (0xff & v);
		return registers;
	}

	/**
	 * Converts a byte[4] binary float value to a float primitive.
	 *
	 * @param bytes
	 *            the byte[4] containing the float value.
	 * @return a float value.
	 */
	public static final float registersToFloat(byte[] bytes) {
		return Float.intBitsToFloat((((bytes[0] & 0xff) << 24)
									| ((bytes[1] & 0xff) << 16) 
									| ((bytes[2] & 0xff) << 8) 
									| (bytes[3] & 0xff)));
	}

	/**
	 * Converts a float value to a byte[4] binary float value.
	 *
	 * @param f the float to be converted.
	 * @return a byte[4] containing the float value.
	 */
	public static final byte[] floatToRegisters(float f) {
		return intToBERegisters(Float.floatToIntBits(f));
	}

	/**
	 * Converts a byte[8] binary double value into a double primitive.
	 *
	 * @param bytes a byte[8] to be converted.
	 * @return a double value.
	 */
	public static final double registersToDouble(byte[] bytes) {
		return Double
				.longBitsToDouble(((((long) (bytes[0] & 0xff) << 56)
						| ((long) (bytes[1] & 0xff) << 48)
						| ((long) (bytes[2] & 0xff) << 40)
						| ((long) (bytes[3] & 0xff) << 32)
						| ((long) (bytes[4] & 0xff) << 24)
						| ((long) (bytes[5] & 0xff) << 16)
						| ((long) (bytes[6] & 0xff) << 8) 
						| ((long) (bytes[7] & 0xff)))));
	}

	/**
	 * Converts a double value to a byte[8].
	 *
	 * @param d the double to be converted.
	 * @return a byte[8].
	 */
	public static final byte[] doubleToRegisters(double d) {
		return longToRegisters(Double.doubleToLongBits(d));
	}

	/**
	 * Converts an unsigned byte to an integer.
	 *
	 * @param b the byte to be converted.
	 * @return an integer containing the unsigned byte value.
	 */
	public static final int unsignedByteToInt(byte b) {
		return (int) b & 0xFF;
	}

	/**
	 * Returns the low byte of a 2-byte word as <tt>int</tt>.
	 *
	 * @param wd
	 * @return the low byte.
	 */
	public static final byte lowByte(int wd) {
		return (new Integer(0xff & wd).byteValue());
	}

	/**
	 * Returns the high byte of a 2-byte word as <tt>int</tt>.
	 *
	 * @param wd
	 * @return the low byte.
	 */
	public static final byte hiByte(int wd) {
		return (new Integer(0xff & (wd >> 8)).byteValue());
	}

	/**
	 * Calculate CRC code
	 * 
	 * @param data byte array message
	 * @param offset the offset of the first byte
	 * @param len number of bytes
	 * @return a byte[2] CRC code
	 */
	public static final byte[] calculateCRC(byte[] data, int offset, int len) {
		int[] crc = { 0xFF, 0xFF };
		int nextByte = 0;
		int uIndex; /* will index into CRC lookup *//* table */
		/* pass through message buffer */
		for (int i = offset; i < len && i < data.length; i++) {
			nextByte = 0xFF & ((int) data[i]);
			uIndex = crc[0] ^ nextByte; // *puchMsg++; /* calculate the CRC */
			crc[0] = crc[1] ^ auchCRCHi[uIndex];
			crc[1] = auchCRCLo[uIndex];
		}
		return new byte[] { lowByte(crc[0]), lowByte(crc[1]) };
	}

	/**
	 * Calculate CRC code (CRC16_XMODEM)
	 * 
	 * @param data byte array message
	 * @param offset the offset of the first byte
	 * @param len number of bytes
	 * @return a byte[2] CRC code
	 */
	public static final int[] calculateCRC1(byte[] data, int off, int len) {
		int[] ret = new int[2];
		int crc = 0x0; // initial value

		for (int j = off; j < (off + len); j++) {
			for (int i = 0, data_c = 0xff & data[j]; i < 8; i++, data_c >>= 1) {
				if (((crc & 0x0001) ^ (data_c & 0x0001)) == 1)
					crc = (crc >> 1) ^ 0x1021;
				else
					crc >>= 1;
			}
		}
		// System.out.println(Integer.toHexString(crc & 0xffff));
		ret[0] = (byte) (crc & 0xFF);
		ret[1] = (byte) ((crc >> 8) & 0xFF);
		return ret;
	}

	private static final int polynomial = 0x1021;
	private static final int[] table = new int[256];
	static { // initialize static lookup table
		for (int i = 0; i < 256; i++) {
			int crc = i << 8;
			for (int j = 0; j < 8; j++) {
				if ((crc & 0x8000) == 0x8000) {
					crc = (crc << 1) ^ polynomial;
				} else {
					crc = (crc << 1);
				}
			}
			table[i] = crc & 0xffff;
		}
	}

	/**
	 * Calculate CRC code (CRC16_CCITT)
	 * 
	 * @param data byte array message
	 * @param offset the offset of the first byte
	 * @param len number of bytes
	 * @return a byte[2] CRC code
	 */
	public static final int[] calculateCRC2(byte[] bytes, int off, int len) {
		int[] ret = new int[2];
		int crc = 0xffff;
		for (int i = off; i < (off + len); i++) {
			int b = (bytes[i] & 0xff);
			crc = (table[((crc >> 8) & 0xff) ^ b] ^ (crc << 8)) & 0xffff;
		}
		// System.out.println(Integer.toHexString(crc & 0xffff));
		ret[0] = (byte) (crc & 0xFF);
		ret[1] = (byte) ((crc >> 8) & 0xFF);
		return ret;
	}

	/**
	 * Calculate LRC code
	 * 
	 * @param data byte array message
	 * @param offset the offset of the first byte
	 * @param len number of bytes
	 * @return a LRC code as <tt>int</tt>.
	 */
	public static final int calculateLRC(byte[] data, int off, int len) {
		int lrc = 0;
		for (int i = off; i < len; i++) {
			lrc += (int) data[i] & 0xff; // calculate with unsigned bytes
		}
		lrc = (lrc ^ 0xff) + 1; // two's complement
		return (int) ((byte) lrc) & 0xff;
	}

	/* Table of CRC values for high-order byte */
	private final static short[] auchCRCHi = { 0x00, 0xC1, 0x81, 0x40, 0x01,
			0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
			0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81,
			0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
			0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00,
			0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
			0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81,
			0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
			0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00,
			0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
			0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80,
			0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
			0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00,
			0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
			0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81,
			0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
			0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00,
			0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
			0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80,
			0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
			0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01,
			0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
			0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80,
			0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40 };

	/* Table of CRC values for low-order byte */
	private final static short[] auchCRCLo = { 0x00, 0xC0, 0xC1, 0x01, 0xC3,
			0x03, 0x02, 0xC2, 0xC6, 0x06, 0x07, 0xC7, 0x05, 0xC5, 0xC4, 0x04,
			0xCC, 0x0C, 0x0D, 0xCD, 0x0F, 0xCF, 0xCE, 0x0E, 0x0A, 0xCA, 0xCB,
			0x0B, 0xC9, 0x09, 0x08, 0xC8, 0xD8, 0x18, 0x19, 0xD9, 0x1B, 0xDB,
			0xDA, 0x1A, 0x1E, 0xDE, 0xDF, 0x1F, 0xDD, 0x1D, 0x1C, 0xDC, 0x14,
			0xD4, 0xD5, 0x15, 0xD7, 0x17, 0x16, 0xD6, 0xD2, 0x12, 0x13, 0xD3,
			0x11, 0xD1, 0xD0, 0x10, 0xF0, 0x30, 0x31, 0xF1, 0x33, 0xF3, 0xF2,
			0x32, 0x36, 0xF6, 0xF7, 0x37, 0xF5, 0x35, 0x34, 0xF4, 0x3C, 0xFC,
			0xFD, 0x3D, 0xFF, 0x3F, 0x3E, 0xFE, 0xFA, 0x3A, 0x3B, 0xFB, 0x39,
			0xF9, 0xF8, 0x38, 0x28, 0xE8, 0xE9, 0x29, 0xEB, 0x2B, 0x2A, 0xEA,
			0xEE, 0x2E, 0x2F, 0xEF, 0x2D, 0xED, 0xEC, 0x2C, 0xE4, 0x24, 0x25,
			0xE5, 0x27, 0xE7, 0xE6, 0x26, 0x22, 0xE2, 0xE3, 0x23, 0xE1, 0x21,
			0x20, 0xE0, 0xA0, 0x60, 0x61, 0xA1, 0x63, 0xA3, 0xA2, 0x62, 0x66,
			0xA6, 0xA7, 0x67, 0xA5, 0x65, 0x64, 0xA4, 0x6C, 0xAC, 0xAD, 0x6D,
			0xAF, 0x6F, 0x6E, 0xAE, 0xAA, 0x6A, 0x6B, 0xAB, 0x69, 0xA9, 0xA8,
			0x68, 0x78, 0xB8, 0xB9, 0x79, 0xBB, 0x7B, 0x7A, 0xBA, 0xBE, 0x7E,
			0x7F, 0xBF, 0x7D, 0xBD, 0xBC, 0x7C, 0xB4, 0x74, 0x75, 0xB5, 0x77,
			0xB7, 0xB6, 0x76, 0x72, 0xB2, 0xB3, 0x73, 0xB1, 0x71, 0x70, 0xB0,
			0x50, 0x90, 0x91, 0x51, 0x93, 0x53, 0x52, 0x92, 0x96, 0x56, 0x57,
			0x97, 0x55, 0x95, 0x94, 0x54, 0x9C, 0x5C, 0x5D, 0x9D, 0x5F, 0x9F,
			0x9E, 0x5E, 0x5A, 0x9A, 0x9B, 0x5B, 0x99, 0x59, 0x58, 0x98, 0x88,
			0x48, 0x49, 0x89, 0x4B, 0x8B, 0x8A, 0x4A, 0x4E, 0x8E, 0x8F, 0x4F,
			0x8D, 0x4D, 0x4C, 0x8C, 0x44, 0x84, 0x85, 0x45, 0x87, 0x47, 0x46,
			0x86, 0x82, 0x42, 0x43, 0x83, 0x41, 0x81, 0x80, 0x40 };

}
