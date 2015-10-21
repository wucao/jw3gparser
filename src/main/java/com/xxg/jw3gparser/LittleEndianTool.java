package com.xxg.jw3gparser;

/**
 * Little-Endian（小字节序）工具类
 * @author 叉叉哥（806223819@qq.com）
 */
public class LittleEndianTool {

	/**
	 * 以Little-Endian（小字节序）方式读取字节数组中的一个16位（2个字节）无符号整数
	 * @param bytes 字节数组
	 * @param offset 开始字节的位置索引
	 * @return 16位（2个字节）无符号整数
	 */
	public static int getUnsignedInt16(byte[] bytes, int offset) {
		int b0 = bytes[offset] & 0xFF;
		int b1 = bytes[offset + 1] & 0xFF;
		return b0 + (b1 << 8);
	}
	
	/**
	 * 以Little-Endian（小字节序）方式读取字节数组中的一个32位（4个字节）无符号整数
	 * @param bytes 字节数组
	 * @param offset 开始字节的位置索引
	 * @return 32位（4个字节）无符号整数
	 */
	public static long getUnsignedInt32(byte[] bytes, int offset) {
		long b0 = bytes[offset] & 0xFFl;
		long b1 = bytes[offset + 1] & 0xFFl;
		long b2 = bytes[offset + 2] & 0xFFl;
		long b3 = bytes[offset + 3] & 0xFFl;
		return b0 + (b1 << 8) + (b2 << 16) + (b3 << 24);
	}
	
	/**
	 * 以Little-Endian（小字节序）方式读取字节数组中的字符串
	 * @param bytes 字节数组
	 * @param offset 开始字节的位置索引
	 * @param length 需要读取的长度
	 * @return 读取的字符串
	 */
	public static String getString(byte[] bytes, int offset, int length) {
		byte[] temp = new byte[length];
		for(int i = 0; i < length; i++) {
			temp[i] = bytes[offset + length - i - 1];
		}
		return new String(temp);
	}

}