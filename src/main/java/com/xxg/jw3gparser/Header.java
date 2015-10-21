package com.xxg.jw3gparser;

import java.util.zip.CRC32;

public class Header {
	
	public static final String BEGIN_TITLE = "Warcraft III recorded game\u001A\0";
	
	private long headerSize;

	private long compressedDataSize;

	private long headerVersion;

	private long uncompressedDataSize;

	private long compressedDataBlockCount;

	private String versionIdentifier;

	private long versionNumber;

	private int buildNumber;

	private int flag;

	private long duration;

	public Header(byte[] fileBytes) throws W3GException {
		
		// 读取开头的字符串"Warcraft III recorded game\u001A\0"
		String beginTitle = new String(fileBytes, 0, 28);
		if (!BEGIN_TITLE.equals(beginTitle)) {
			throw new W3GException("录像格式不正确。");
		}

		// header部分总大小（版本小于或等于V1.06是0x40(64)，版本大于或等于V1.07是0x44(68)）
		headerSize = LittleEndianTool.getUnsignedInt32(fileBytes, 28);
		if (headerSize != 0x44) {
			throw new W3GException("不支持V1.06及以下版本的录像。");
		}

		// 压缩文件大小
		compressedDataSize = LittleEndianTool.getUnsignedInt32(fileBytes, 32);

		// header版本（版本小于或等于V1.06是0，版本大于或等于V1.07是1）
		headerVersion = LittleEndianTool.getUnsignedInt32(fileBytes, 36);
		if (headerVersion != 1) {
			throw new W3GException("不支持V1.06及以下版本的录像。");
		}

		// 解压缩数据大小
		uncompressedDataSize = LittleEndianTool.getUnsignedInt32(fileBytes, 40);

		// 压缩数据块数量
		compressedDataBlockCount = LittleEndianTool.getUnsignedInt32(fileBytes, 44);

		// WAR3：非冰封王座录像，W3XP冰封王座录像
		versionIdentifier = LittleEndianTool.getString(fileBytes, 48, 4);

		// 版本号（例如1.24版本对应的值是24）
		versionNumber = LittleEndianTool.getUnsignedInt32(fileBytes, 52);

		// Build号
		buildNumber = LittleEndianTool.getUnsignedInt16(fileBytes, 56);

		// 单人游戏（0x0000） 多人游戏（0x8000，对应十进制32768）
		flag = LittleEndianTool.getUnsignedInt16(fileBytes, 58);

		// 录像时长（毫秒）
		duration = LittleEndianTool.getUnsignedInt32(fileBytes, 60);

		// CRC32校验码
		long crc32 = LittleEndianTool.getUnsignedInt32(fileBytes, 64);

		// 这里来校验CRC32，将最后四位也就是CRC32所在的四个字节设为0后计算CRC32的值
		CRC32 crc32Tool = new CRC32();
		crc32Tool.update(fileBytes, 0, 64);
		crc32Tool.update(0);
		crc32Tool.update(0);
		crc32Tool.update(0);
		crc32Tool.update(0);

		// 判断Header中后四位读取的CRC32的值和计算得到的值比较，看是否一致
		if (crc32 != crc32Tool.getValue()) {
			throw new W3GException("Header部分CRC32校验不通过。");
		}
	}

	public long getHeaderSize() {
		return headerSize;
	}

	public long getCompressedDataSize() {
		return compressedDataSize;
	}

	public long getHeaderVersion() {
		return headerVersion;
	}

	public long getUncompressedDataSize() {
		return uncompressedDataSize;
	}

	public long getCompressedDataBlockCount() {
		return compressedDataBlockCount;
	}

	public String getVersionIdentifier() {
		return versionIdentifier;
	}

	public long getVersionNumber() {
		return versionNumber;
	}

	public int getBuildNumber() {
		return buildNumber;
	}

	public int getFlag() {
		return flag;
	}

	public long getDuration() {
		return duration;
	}
	
}