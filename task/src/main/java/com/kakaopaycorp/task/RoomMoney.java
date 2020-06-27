package com.kakaopaycorp.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

public class RoomMoney {

	private String roomId;       // X-ROOM-ID
	private String moneyToken;	 // 머니뿌리기 Token Key
	private String registerId;  // 머니뿌리기 등록자 ID
	private int    expireSec;    // 머니뿌리기 타임아웃 초(10분 == 600초)
	private int    numOfUser;    // 머니뿌리기 선착순 인원
	private String status;       // 머니뿌리기 완료여부 (1:진행중, 9:완료)
	private long amount;         // 머니뿌리기 금액
	private long receivedAmount; // 머니뿌리기 완료금액
	private String registerTime; // 머니뿌리기 등록일시
	private String finishTime;   // 머니뿌리기 종료일시
	private List<MoneyUser> list; // 머니뿌리기 받은 목록
	
	public RoomMoney( String id, String token, String userId, int users, long amt )
	{
		this.roomId       = id;
		this.moneyToken   = token;
		this.registerId   = userId;
		this.expireSec    = 600;
		this.status       = "1";
		this.numOfUser    = users;
		this.amount       = amt;
		this.receivedAmount = 0;
		this.registerTime = (new SimpleDateFormat("yyyyMMddHHmmss.SSS")).format( new Date() );
		this.list         = Collections.synchronizedList(new ArrayList<MoneyUser>());
	}
    
	/**
	 * @return the roomId
	 */
	public String getRoomId() {
		return roomId;
	}
	/**
	 * @return the moneyToken
	 */
	public String getMoneyToken() {
		return moneyToken;
	}
	/**
	 * @return the moneyUserId
	 */
	public String getRegisterId() {
		return registerId;
	}
	/**
	 * @return the expireSec
	 */
	public int getExpireSec() {
		return expireSec;
	}
	/**
	 * @return the numOfGetUser
	 */
	public int getNumOfUser() {
		return numOfUser;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @return the amount
	 */
	public long getAmount() {
		return amount;
	}
	/**
	 * @return the amount
	 */
	public long getReceivedAmount() {
		return receivedAmount;
	}
	/**
	 * @return the registerTime
	 */
	public String getRegisterTime() {
		return registerTime;
	}
	/**
	 * @return the finishTime
	 */
	public String getFinishTime() {
		return finishTime;
	}
	
	
	/**
	 * @param roomId the roomId to set
	 */
	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	/**
	 * @param moneyToken the moneyToken to set
	 */
	public void setMoneyToken(String moneyToken) {
		this.moneyToken = moneyToken;
	}
	/**
	 * @param moneyUserId the moneyUserId to set
	 */
	public void setRegisterId(String moneyUserId) {
		this.registerId = moneyUserId;
	}
	/**
	 * @param expireSec the expireSec to set
	 */
	public void setExpireSec(int expireSec) {
		this.expireSec = expireSec;
	}
	/**
	 * @param numOfGetUser the numOfGetUser to set
	 */
	public void setNumOfUser(int numOfGetUser) {
		this.numOfUser = numOfGetUser;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @param amount the amount to set
	 */
	public void setAmount(long amount) {
		this.amount = amount;
	}
	/**
	 * @param registerTime the registerTime to set
	 */
	public void setRegisterTime(String registerTime) {
		this.registerTime = registerTime;
	}
	/**
	 * @param finishTime the finishTime to set
	 */
	public void setFinishTime(String finishTime) {
		this.finishTime = finishTime;
	}
	
	/**
	 * 
	 * @return
	 */
	public int  getMoneyUserCnt() {
		return list.size();
	}
	public List<MoneyUser> getMoneyUserList()
	{
		return list;
	}
	/**
	 * 
	 * @param mu
	 */
	public void addMoneyUser( MoneyUser mu ) {
		list.add( mu );
	}
	
	public void sumReceivedAmount( long amount )
	{
		this.receivedAmount += amount;
	}
	
	/*
	public JSONObject toJSON() throws JSONException
	{
		JSONObject json = new JSONObject();
		json.put( "뿌린 시각"      , registerTime );
		json.put( "뿌린 금액"      , amount );
		json.put( "받기 완료된 금액" , receivedAmount );
		int receivedUserCnt = getMoneyUserCnt();
		JSONArray array = new JSONArray();
		for( int i=0, n=receivedUserCnt; i < n; i++ )
		{
			MoneyUser mu = getMoneyUserList().get(i);
			array.put( mu.toJSON() );
		}
		json.put( "받기 완료된 정보", array );
		return json;
	}
	*/
    
	public String toString()
	{
		JSONObject json = new JSONObject();
		
		try
		{
			json.put( "뿌린 시각"      , registerTime );
			json.put( "뿌린 금액"      , amount );
			json.put( "받기 완료된 금액" , receivedAmount );
			
			int receivedUserCnt = getMoneyUserCnt();
			JSONArray array = new JSONArray();
			for( int i=0, n=receivedUserCnt; i < n; i++ )
			{
				MoneyUser mu = getMoneyUserList().get(i);
				if( null != mu.getReceivedUserId() )
					array.put( mu.toJSON() );
			}
			json.put( "받기 완료된 정보", array );

		} catch (JSONException e) {
			return "";
		}
		
		return json.toString();
	}
}
