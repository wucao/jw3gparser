package com.xxg.jw3gparser;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ReplayData {
	
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
	private List<Player> playerList;
	
	/**
	 * 游戏进行时的时间（毫秒）
	 */
	private long time;
	
	/**
	* 是否暂停
	*/
	private boolean isPause;
	
	/**
	 * 聊天信息集合
	 */
	private List<ChatMessage> chatList = new ArrayList<ChatMessage>();
	
	public ReplayData(byte[] uncompressedDataBytes, int offset, List<Player> playerList) throws W3GException, UnsupportedEncodingException {
		
		this.uncompressedDataBytes = uncompressedDataBytes;
		this.offset = offset;
		this.playerList = playerList;
		
		analysis();
	}

	/**
	 * 解析
	 */
	private void analysis() throws UnsupportedEncodingException, W3GException
	{
		byte blockId = 0;
		while ((blockId = uncompressedDataBytes[offset]) != 0) {
			switch (blockId) {
			
				// 聊天信息
	            case 0x20:
	            	analysisChatMessage();
	            	break;
	            	
				// 时间段（一般是100毫秒左右一段）
				case 0x1E:
                case 0x1F:
                	analysisTimeSlot();
                	break;
                	
                // 玩家离开游戏
                case 0x17:
                	analysisLeaveGame();
                	break;

                // 未知的BlockId
                case 0x1A:
				case 0x1B:
				case 0x1C: 
					offset += 5;
					break;
                case 0x22:
                	offset += 6;
                	break;
                case 0x23:
                	offset += 11;
                	break;
                case 0x2F:
                	offset += 9;
                	break;

                // 无效的Block
                default:
                    throw new W3GException("Unkown block id: " + blockId);
			}
		}
	}

	/**
	 *  解析聊天信息
	 */
	private void analysisChatMessage() throws UnsupportedEncodingException {

		ChatMessage chatMessage = new ChatMessage();
		
		offset++;
		byte playerId = uncompressedDataBytes[offset];
		chatMessage.setFrom(getPlayById(playerId));
		offset++;
		
		int bytes = LittleEndianTool.getUnsignedInt16(uncompressedDataBytes, offset);
		offset += 2;
		
		offset++;
		
		long mode = LittleEndianTool.getUnsignedInt32(uncompressedDataBytes, offset);
		if(mode >= 3) {
			int receiverPlayerId = (int) (mode - 3);
			chatMessage.setTo(getPlayBySlotNumber(receiverPlayerId));
		}
		chatMessage.setMode(mode);
		offset += 4;

		String message = new String(uncompressedDataBytes, offset, bytes - 6, "UTF-8");
		chatMessage.setMessage(message);
		offset += bytes - 5;
		
		chatMessage.setTime(time);
		chatList.add(chatMessage);
	}
	
	/**
	*  解析一个时间块
	 * @throws W3GException 
	*/
	private void analysisTimeSlot() throws W3GException {
		
		offset++;
		
		int bytes = LittleEndianTool.getUnsignedInt16(uncompressedDataBytes, offset);
		offset += 2;
		
		// 游戏时间在非暂停状态下增加
		int timeIncrement = LittleEndianTool.getUnsignedInt16(uncompressedDataBytes, offset);
		if(!isPause) {
			time += timeIncrement;
		}
		offset += 2;
		
		// 解析Action
		analysisAction(offset + bytes - 2);
	}
	
	/**
	 * 解析TimeSlot中的Action
	 * @param end TimeSlot的结束位置
	 * @throws W3GException 
	 */
	private void analysisAction(int timeSlotEnd) throws W3GException {
		
		while(offset != timeSlotEnd) {
			
			byte playerId = uncompressedDataBytes[offset];
			Player player = getPlayById(playerId);
			int action = player.getAction();
			offset++;
			int commandDataBlockbytes = LittleEndianTool.getUnsignedInt16(uncompressedDataBytes, offset);
			offset += 2;
			int commandDataBlockEnd = offset + commandDataBlockbytes;
			boolean lastActionWasDeselect = false;
			while(offset != commandDataBlockEnd) {
				
				byte actionId = uncompressedDataBytes[offset];
				
				boolean thisActionIsDeselect = false;
				if(actionId == 0x16 && uncompressedDataBytes[offset + 1] == 0x02) {
					thisActionIsDeselect = true;
				}
				
				switch (actionId) {
				
					// 暂停游戏
					case 0x01:
						isPause = true;
						offset++;
						break;
					
					// 继续游戏
					case 0x02:
						isPause = false;
						offset++;
						break;
					
					case 0x03:
						offset += 2;
						break;
					case 0x04:
					case 0x05:
						offset++;
						break;
					case 0x06:
						offset++;
						while(uncompressedDataBytes[offset] != 0) {
							offset++;
						}
						offset++;
						break;
					case 0x07:
						offset += 5;
						break;
					case 0x10:
						offset += 15;
						action++;
						break;
					case 0x11:
						offset += 23;
						action++;
						break;
					case 0x12:
						offset += 31;
						action++;
						break;
					case 0x13:
						offset += 39;
						action++;
						break;
					case 0x14:
						offset += 44;
						action++;
						break;
					case 0x16:
						offset++;
						byte selectMode = uncompressedDataBytes[offset];
						offset++;
						if(selectMode == 0x02) {
							action++;
						} else {
							if(!lastActionWasDeselect) {
								action++;
							}
						}
						int number = LittleEndianTool.getUnsignedInt16(uncompressedDataBytes, offset);
						offset += 2;
						offset += number * 8;
						break;
					case 0X17:
						offset += 2;
						int n = LittleEndianTool.getUnsignedInt16(uncompressedDataBytes, offset);
						offset += 2;
						offset += n * 8;
						action++;
						break;
					case 0x18:
						offset += 3;
						action++;
						break;
					case 0x19:
						offset += 13;
						break;
					case 0x1a:
						offset++;
						break;
					case 0x1b:
						offset += 10;
						break;
					case 0x1c:
						offset += 10;
						action++;
						break;
					case 0x1d:
						offset += 9;
						action++;
						break;
					case 0x1e:
						offset += 6;
						action++;
						break;
					case 0x21:
						offset += 9;
						break;
					case 0x20:
					case 0x22:
					case 0x23:
					case 0x24:
					case 0x25:
					case 0x26:
					case 0x29:
					case 0x2a:
					case 0x2b:
					case 0x2c:
					case 0x2f:
					case 0x30:
					case 0x31:
					case 0x32:
						offset++;
						break;
					case 0x27:
					case 0x28:
					case 0x2d:
						offset += 6;
						break;
					case 0x2e:
						offset += 5;
						break;
					case 0x50:
						offset += 6;
						break;
					case 0x51:
						offset += 10;
						break;
					case 0x60:
						offset += 9;
						while(uncompressedDataBytes[offset] != 0) {
							offset++;
						}
						offset++;
						break;
					case 0x61:
						offset++;
						action++;
						break;
					case 0x62:
						offset += 13;
						break;
					case 0x66:
					case 0x67:
						offset++;
						action++;
						break;
					case 0x68:
						offset += 13;
						break;
					case 0x69:
					case 0x6a:
						offset += 17;
						break;
					case 0x75:
						offset += 2;
						break;
					default:
						throw new W3GException("Unkown action id: " + actionId);
				}
				
				lastActionWasDeselect = thisActionIsDeselect;
			}
			
			player.setAction(action);
		}
	}
	
	/**
	 * 玩家离开游戏Block解析
	 */
	private void analysisLeaveGame() {
		
		offset += 5;
		
		// 玩家离开游戏就不再计算游戏时间
		byte playerId = uncompressedDataBytes[offset];
		Player player = getPlayById(playerId);
		player.setPlayTime(time);
		offset += 9;
		
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
	
	/**
	 *  通过玩家SlotNumber获取Player对象
	 *  @param slotNumber 玩家SlotNumber
	 *  @return 对应的Player对象
	 */
	private Player getPlayBySlotNumber(int slotNumber) {
		
		Player p = null;
		for(Player player : playerList) {
			if(slotNumber == player.getSlotNumber()) {
				p = player;
				break;
			}
		}
		return p;
	}

	public List<ChatMessage> getChatList() {
		return chatList;
	}
	
}