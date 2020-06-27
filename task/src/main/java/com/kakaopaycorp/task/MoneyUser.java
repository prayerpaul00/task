package com.kakaopaycorp.task;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

public class MoneyUser
{
	private String     receivedUserId;	// 머니뿌리기 받은 ID
	private long       receivedAmount;	// 머니뿌리기 받은 금
	private String     receivedTime;	// 머니뿌리기 받은 시각
	
	public MoneyUser( long amount )
	{
		this.receivedAmount = amount;
	}
	
	/**
	 * @return the recvUserId
	 */
	public String getReceivedUserId() {
		return receivedUserId;
	}
	/**
	 * @return the recvAmt
	 */
	public long getReceivedAmount() {
		return receivedAmount;
	}
	/**
	 * @return the recvTime
	 */
	public String getReceivedTime() {
		return receivedTime;
	}
	/**
	 * @param recvUserId the recvUserId to set
	 */
	public void setReceivedUserId(String recvUserId) {
		this.receivedUserId = recvUserId;
	}
	/**
	 * @param recvAmt the recvAmt to set
	 */
	public void setReceivedAmount(long recvAmt) {
		this.receivedAmount = recvAmt;
	}
	/**
	 * @param recvTime the recvTime to set
	 */
	public void setReceivedTime(String recvTime) {
		this.receivedTime = recvTime;
	}
		
	public JSONObject toJSON()
	{
		JSONObject json = new JSONObject();
		try {
			json.put( "받은 사용자 아이디", receivedUserId );
			json.put( "받은 금액"       , receivedAmount );
			json.put( "받은 시각"       , receivedTime   );
		} catch (JSONException e) {}
		
		return json;
	}
}
