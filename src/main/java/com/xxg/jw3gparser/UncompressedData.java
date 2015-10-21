package com.xxg.jw3gparser;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class UncompressedData {

	/**
	 * 解压缩的字节数组
	 */
	private byte[] uncompressedDataBytes;
	
	/**
	 * 解析的字节位置
	 */
	private int offset;
	
	/**
	 * 玩家列表
	 */
	private List<Player> playerList = new ArrayList<Player>();
	
	/**
	 * 游戏名称
	 */
	private String gameName;
	
	/**
	 * 地图路径
	 */
	private String map;

	/**
	 * 游戏创建者名称
	 */
	private String createrName;
	
	/** 
	 * 游戏进行时的信息 
	 */  
	private ReplayData replayData;
	
	public UncompressedData(byte[] uncompressedDataBytes) throws UnsupportedEncodingException, W3GException {  
        
	    this.uncompressedDataBytes = uncompressedDataBytes;  
	                  
	    // 跳过前4个未知字节  
	    offset += 4;  
	      
	    // 解析第一个玩家  
	    analysisPlayerRecode();  
	      
	    // 游戏名称（UTF-8编码）  
	    int begin = offset;  
	    while(uncompressedDataBytes[offset] != 0) {  
	        offset++;  
	    }  
	    gameName = new String(uncompressedDataBytes, begin, offset - begin, "UTF-8");  
	    offset++;  
	      
	    // 跳过一个空字节  
	    offset++;  
	      
	    // 解析一段特殊编码的字节串，其中包含游戏设置、地图和创建者  
	    analysisEncodedBytes();  
	      
	    // 跳过PlayerCount、GameType、LanguageID  
	    offset += 12;  
	      
	    // 解析玩家列表  
	    while(uncompressedDataBytes[offset] == 0x16) {  
	          
	        analysisPlayerRecode();  
	          
	        // 跳过4个未知的字节0x00000000  
	        offset += 4;  
	    }  
	      
	    // GameStartRecord - RecordID、number of data bytes following  
	    offset += 3;  
	      
	    // 解析每个Slot  
	    byte slotCount = uncompressedDataBytes[offset];  
	    offset++;  
	    for(int i = 0; i < slotCount; i++) {  
	        analysisSlotRecode(i);  
	    }  
	      
	    // RandomSeed、RandomSeed、StartSpotCount  
	    offset += 6;  
	      
	    // 游戏进行时的信息解析  
	    replayData = new ReplayData(uncompressedDataBytes, offset, playerList);  
	}  

	/**
	 *  解析PlayerRecode
	 *  @throws UnsupportedEncodingException
	 */
	private void analysisPlayerRecode() throws UnsupportedEncodingException {
		
		Player player = new Player();
		playerList.add(player);
		
		// 是否是主机(0为主机)
		byte isHostByte = uncompressedDataBytes[offset];
		boolean isHost = isHostByte == 0;
		player.setHost(isHost);
		offset++;
		
		// 玩家ID
		byte playerId = uncompressedDataBytes[offset];
		player.setPlayerId(playerId);
		offset++;
		
		// 玩家名称（UTF-8编码）
		int begin = offset;
		while(uncompressedDataBytes[offset] != 0) {
			offset++;
		}
		String playerName = new String(uncompressedDataBytes, begin, offset - begin, "UTF-8");
		player.setPlayerName(playerName);
		offset++;
		
		// 附加数据大小
		int additionalDataSize = uncompressedDataBytes[offset];
		offset++;
		
		// 加上附加数据大小
		offset += additionalDataSize;
		
	}
	
	/**
	 *  解析特殊编码的字节串
	 *  @throws UnsupportedEncodingException
	 */
	private void analysisEncodedBytes() throws UnsupportedEncodingException {
		
		int begin = offset;
		while(uncompressedDataBytes[offset] != 0) {
			offset++;
		}
		
		// 编码的数据和解码后的数据的长度
		int encodeLength = offset - begin - 1;
		int decodeLength = encodeLength - (encodeLength - 1) / 8 - 1;
		
		// 编码的数据和解码后的数据
		byte[] encodeData = new byte[encodeLength];
		byte[] decodeData = new byte[decodeLength];
		
		// 将编码字节串部分拷贝成一个单独的字节数组，便于解析
		System.arraycopy(uncompressedDataBytes, begin, encodeData, 0, encodeLength);
		
		// 解码（解码的代码来自于http://w3g.deepnode.de/files/w3g_format.txt文档4.3部分，由C语言代码翻译成Java）
		byte mask = 0;
		int decodePos = 0;
		int encodePos = 0;
		while (encodePos < encodeLength) {
			if (encodePos % 8 == 0) {
				mask = encodeData[encodePos];
			} else {
				if ((mask & (0x1 << (encodePos % 8))) == 0) {
					decodeData[decodePos++] = (byte) (encodeData[encodePos] - 1);
				} else {
					decodeData[decodePos++] = encodeData[encodePos];
				}
			}
			encodePos++;
		}
		
		// 直接跳过游戏设置，这部分不再解析了
		int decodeOffset = 13;
		int decodeBegin = decodeOffset;
		
		// 地图路径
		while(decodeData[decodeOffset] != 0) {
			decodeOffset++;
		}
		map = new String(decodeData, decodeBegin, decodeOffset - decodeBegin, "UTF-8");
		decodeOffset++;
		
		// 主机（游戏创建者）玩家名称
		decodeBegin = decodeOffset;
		while(decodeData[decodeOffset] != 0) {
			decodeOffset++;
		}
		createrName = new String(decodeData, decodeBegin, decodeOffset - decodeBegin, "UTF-8");
		decodeOffset++;
		
		offset++;
	}
	
	/**
	 *  解析每个Slot
	 */
	private void analysisSlotRecode(int slotNumber) {
		
		// 玩家ID
		byte playerId = uncompressedDataBytes[offset];
		offset++;
		
		// 跳过地图下载百分比
		offset++;
		
		// 状态 0空的 1关闭的 2使用的
		byte slotStatus = uncompressedDataBytes[offset];
		offset++;
		
		// 是否是电脑
		byte computerPlayFlag = uncompressedDataBytes[offset];
		boolean isComputer = computerPlayFlag == 1;
		offset++;
		
		// 队伍
		byte team = uncompressedDataBytes[offset];
		offset++;
		
		// 颜色
		byte color = uncompressedDataBytes[offset];
		offset++;
		
		// 种族
		byte race = uncompressedDataBytes[offset];
		offset++;
		
		// 电脑难度
		byte aiStrength = uncompressedDataBytes[offset];
		offset++;
		
		// 障碍（血量百分比）
		byte handicap = uncompressedDataBytes[offset];
		offset++;
		
		// 设置玩家列表
		if(slotStatus == 2) {
			Player player= null;
			if(!isComputer) {
				player = getPlayById(playerId);
			} else {
				player = new Player();
				playerList.add(player);
			}
			player.setComputer(isComputer);
			player.setAiStrength(PlayAiStrength.values()[aiStrength]);
			if(color < 12) {
				player.setColor(PlayerColor.values()[color]);
			}
			player.setHandicap(handicap);
			switch(race) {
				case 0x01:
				case 0x41:
					player.setRace(PlayerRace.HUMAN);
					break;
				case 0x02:
				case 0x42:
					player.setRace(PlayerRace.ORC);
					break;
				case 0x04:
				case 0x44:
					player.setRace(PlayerRace.NIGHT_ELF);
					break;
				case 0x08:
				case 0x48:
					player.setRace(PlayerRace.UNDEAD);
					break;
				case 0x20:
				case 0x60:
					player.setRace(PlayerRace.RANDOM);
					break;
			}
			player.setTeamNumber(team);
			player.setSlotNumber(slotNumber);
		}
		
	}
	
	/**
	 *  通过玩家ID获取Player对象
	 *  @param playerId 玩家ID
	 *  @return 对应的Player对象
	 */
	private Player getPlayById(byte playerId) {
		
		Player p = null;
		for(Player player : playerList) {
			if(playerId == player.getPlayerId()) {
				p = player;
				break;
			}
		}
		return p;
	}

	public List<Player> getPlayerList() {
		return playerList;
	}

	public String getGameName() {
		return gameName;
	}

	public String getMap() {
		return map;
	}

	public String getCreaterName() {
		return createrName;
	}
	
	public ReplayData getReplayData() {  
	    return replayData;  
	}

}