package com.xxg.jw3gparser;

import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class CompressedDataBlock {
	
	private int compressedDataSize;
	
	private int uncompressedDataSize;
	
	private byte[] uncompressedDataBytes;
	
	/**
	 *  @param fileBytes 录像文件转成的字节数组
	 *  @param offset 压缩数据块的开始位置
	 *  @throws DataFormatException
	 *  @throws W3GException
	 */
	public CompressedDataBlock(byte[] fileBytes, int offset) throws DataFormatException, W3GException {
				
		// 压缩数据大小
		compressedDataSize = LittleEndianTool.getUnsignedInt16(fileBytes, offset);
		
		// 解压缩后数据大小
		uncompressedDataSize = LittleEndianTool.getUnsignedInt16(fileBytes, offset + 2);

		// 压缩数据，从第8个字节开始，长度为compressedDataSize，解压缩
		uncompressedDataBytes = new byte[uncompressedDataSize];
		Inflater inflater = new Inflater();
		inflater.setInput(fileBytes, offset + 8, compressedDataSize);
		int realUncompressedDataSize = inflater.inflate(uncompressedDataBytes);
		if(realUncompressedDataSize != uncompressedDataSize) {
			throw new W3GException("Uncompressed data error");
		}

	}

	public int getCompressedDataSize() {
		return compressedDataSize;
	}

	public int getUncompressedDataSize() {
		return uncompressedDataSize;
	}

	public byte[] getUncompressedDataBytes() {
		return uncompressedDataBytes;
	}
	
}