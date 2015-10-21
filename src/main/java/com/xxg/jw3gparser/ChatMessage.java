package com.xxg.jw3gparser;

public class ChatMessage {

	/**
	 * 发送者
	 */
	private Player from;
	
	/**
	 * 发送方式
	 * 0:发送给所有玩家
	 * 1:发送给队友
	 * 2:发送给裁判或观看者
	 * 3+N:发送给指定玩家
	 */
	private long mode;
	
	/**
	 * 接收者（mode为3+N时有效）
	 */
	private Player to;
	
	/**
	 * 消息发送时间
	 */
	private long time;
	
	/**
	 * 消息内容
	 */
	private String message;

	public Player getFrom() {
		return from;
	}

	public void setFrom(Player from) {
		this.from = from;
	}

	public long getMode() {
		return mode;
	}

	public void setMode(long mode) {
		this.mode = mode;
	}

	public Player getTo() {
		return to;
	}

	public void setTo(Player to) {
		this.to = to;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}