package com.kakaopaycorp.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public class RoomMoneyDAO {
	
	private static Map<String, RoomMoney> map = new HashMap<String, RoomMoney>();
	
    public RoomMoney getRoomMoney( String key ) 
    {
        return map.get( key );
    }
    
    public List<RoomMoney> getRoomMoneyList()
    {
    	return new ArrayList<RoomMoney>(map.values());
    }
    
    public boolean containRoomMoney( String key )
    {
    	return map.containsKey(key);
    }
    
    public void putRoomMoney( String key, RoomMoney rm )
    {
    	map.put( key, rm );
    }
    
    public boolean containRoomMoneyUser( String key, String userId )
    {
    	RoomMoney rm = getRoomMoney( key );
    	
    	List<MoneyUser> list = rm.getMoneyUserList();
    	for( int i=0, n=list.size(); i < n; i++ )
    	{
    		MoneyUser mu = list.get( i );
    		if( userId.equals(mu.getReceivedUserId()) ) 
    			return true;
    	}
    	return false;
    }
    
    /**
     * 해당 토큰에 아직 받기가 안 되어 있는 값 리턴
     * @param key
     * @return mu
     */
    public MoneyUser getRoomMoneyUser( String key )
    {
    	RoomMoney rm = getRoomMoney( key );
    	
    	List<MoneyUser> list = rm.getMoneyUserList();
    	for( int i=0, n=list.size(); i < n; i++ )
    	{
    		MoneyUser mu = list.get( i );
    		if( null == mu.getReceivedUserId() ) return mu;
    	}
    	return null;
    }
    
}
