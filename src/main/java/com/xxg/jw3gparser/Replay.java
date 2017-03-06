package com.xxg.jw3gparser;

import java.io.*;
import java.util.Arrays;
import java.util.zip.DataFormatException;

public class Replay {
	
	private Header header;
	
	private UncompressedData uncompressedData;

	public Replay(File w3gFile) throws IOException, W3GException, DataFormatException {
		// 将文件转为字节数组，方便处理
		byte[] fileBytes = fileToByteArray(w3gFile);

		// 找到Header起始位置(主要是兼容网易对战平台nwg格式录像)
		String fileHex = HexUtil.encodeHexString(fileBytes);
		String seqHex = HexUtil.encodeHexString(Header.BEGIN_TITLE.getBytes("UTF-8"));
		int index = fileHex.indexOf(seqHex) / 2;
		fileBytes = Arrays.copyOfRange(fileBytes, index, fileBytes.length);

		// 解析Header
		header = new Header(fileBytes);

		// 遍历解析每个压缩数据块，解压缩，合并
		long compressedDataBlockCount = header.getCompressedDataBlockCount();
		byte[] uncompressedDataBytes = new byte[0]; // 所有压缩数据块中数据解压合并到这个数组中
		int offset = 68;
		for(int i = 0; i < compressedDataBlockCount; i++) {
			CompressedDataBlock compressedDataBlock = new CompressedDataBlock(fileBytes, offset);

			// 数组合并
			byte[] blockUncompressedData = compressedDataBlock.getUncompressedDataBytes();
			byte[] temp = new byte[uncompressedDataBytes.length + blockUncompressedData.length];
			System.arraycopy(uncompressedDataBytes, 0, temp, 0, uncompressedDataBytes.length);
			System.arraycopy(blockUncompressedData, 0, temp, uncompressedDataBytes.length, blockUncompressedData.length);
			uncompressedDataBytes = temp;

			int blockCompressedDataSize = compressedDataBlock.getCompressedDataSize();
			offset += 8 + blockCompressedDataSize;
		}

		// 处理解压缩后的字节数组
		uncompressedData = new UncompressedData(uncompressedDataBytes);
	}

	/**
	 * 将文件转换成字节数组
	 * @param w3gFile 文件
	 * @return 字节数组
	 * @throws IOException
	 */
	private byte[] fileToByteArray(File w3gFile) throws IOException {

		FileInputStream fileInputStream = new FileInputStream(w3gFile);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int n;
		
		try {
			while((n = fileInputStream.read(buffer)) != -1) {
				byteArrayOutputStream.write(buffer, 0, n);
			}
		} finally {
			fileInputStream.close();
		}
		
		return byteArrayOutputStream.toByteArray();
	}

	public Header getHeader() {
		return header;
	}

	public UncompressedData getUncompressedData() {
		return uncompressedData;
	}

}