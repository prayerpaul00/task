package com.kakaopaycorp.task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.bytebuddy.utility.RandomString;

@RestController
@RequestMapping(path = "/moneys")
public class RoomMoneyController {
	
	private static String DATE_FORMAT = "yyyyMMddHHmmss.SSS";
	@Autowired
    private RoomMoneyDAO roomMoneyDao;
	
	@GetMapping(path="/", produces = "application/json")
    public String getRoomMoney(@RequestHeader(name = "X-USER-ID", required = true) String headerUserId,
            @RequestHeader(name = "X-ROOM-ID", required = true) String headerRoomId,
            @RequestBody HashMap<String, Object> map) throws JSONException 
    {
		String token = (String)map.get( "moneyToken" );
		// token 3자리 체크.
		if( null == token || 3 != token.length() ) return buildErrorMessage("201", "토큰이 유효하지 않습니다.");
		
		String key = buildRoomMoneyKey( headerRoomId, token );
		
		// 존재하는지 검증.
		if( !roomMoneyDao.containRoomMoney(key) ) return buildErrorMessage("101", "해당 토큰으로 머니뿌리기가 존재하지 않습니다.");
		
		// 머니뿌리기 등록자만 조회 가능.
		RoomMoney rm = roomMoneyDao.getRoomMoney( key );
		if( !headerUserId.equals(rm.getRegisterId()) ) return buildErrorMessage("102", "머니뿌리기 조회는 등록자만 가능합니다.");
		
		// 머니뿌리기 조회는 7일간만 가능.
		try {
			if( compareIntervalTime(Calendar.DATE, 7, rm.getRegisterTime()) ) return buildErrorMessage("103", "머니뿌리기 조회는 7일간 가능합니다.");
		} catch (ParseException e) {
			return null;
		}
		
		return rm.toString();
    }
	
	@GetMapping(path="/*/", produces = "application/json")
    public List<RoomMoney> getRoomMoneyList(@RequestHeader(name = "X-USER-ID", required = true) String headerUserId,
            @RequestHeader(name = "X-ROOM-ID", required = true) String headerRoomId,
            @RequestBody HashMap<String, Object> map) throws JSONException 
    {		
        return roomMoneyDao.getRoomMoneyList();
    }

	
	@PostMapping(path= "/", produces = "application/json")
    public String createRoomMoney(@RequestHeader(name = "X-USER-ID", required = true) String headerUserId,
            @RequestHeader(name = "X-ROOM-ID", required = true) String headerRoomId,
            @RequestBody HashMap<String, Object> map ) throws JSONException 
    {
		// 3자리 token 생성
		String token = RandomString.make( 3 );
		
		// 머니뿌리기 키생성. 방번호 + '-' + 토큰
		String key   = buildRoomMoneyKey( headerRoomId, token );;
		
		// GET 뿌릴 인원수 
		int    cnt   = Integer.parseUnsignedInt( (String)map.get("numOfUser") );
		// GET 뿌릴 금액
		long   amt   = Long.parseUnsignedLong( (String)map.get("amount") );
    	RoomMoney rm = new RoomMoney( headerRoomId, token, headerUserId, cnt, amt );
    	
    	// 분배 금액
    	long dividedAmt = (long)amt / cnt;
    	// 나머지 금액
    	long restAmt    = (long)amt % cnt;
    	for( int i=0; i < cnt; i++ )
    	{
    		// 맨 처음 등록시, 분배금액 + 나머지금액
    		if( i == 0 )
    		{
    			MoneyUser mu = new MoneyUser( dividedAmt + restAmt );
    			rm.addMoneyUser( mu );
    		}
    		// 다음 등록시, 분배금액
    		else
    		{
    			MoneyUser mu = new MoneyUser( dividedAmt );
    			rm.addMoneyUser( mu );
    		}
    	}
    	
    	roomMoneyDao.putRoomMoney( key, rm );
    	
    	return "{\"머니뿌리기 토큰\":\"" + token + "\"}";
    }
	
	@PutMapping(path= "/", produces = "application/json")
    public String updateRoomMoney(@RequestHeader(name = "X-USER-ID", required = true) String headerUserId,
            @RequestHeader(name = "X-ROOM-ID", required = true) String headerRoomId,
            @RequestBody HashMap<String, Object> map ) throws JSONException 
    {
		// token 3자리 체크
		String token = (String)map.get( "moneyToken" );
		if( null == token || token.length() != 3 ) return buildErrorMessage("201", "토큰이 유효하지 않습니다.");
		// 머니뿌리기 키 생성
		String key   = buildRoomMoneyKey( headerRoomId, token );
		
		// 머니뿌리기 대화과 동일한 대화방 사람만 받기 가능.
		// 머니뿌리기 키가 ROOM ID를 포함하고 있어서 동일 대화방 체크 가능.
		if( !roomMoneyDao.containRoomMoney(key) ) return buildErrorMessage("202", "대화방이 유효하지 않습니다.");
		
		// 머니뿌리기 등록자는 받기 안 됨.
		RoomMoney rm = roomMoneyDao.getRoomMoney( key );
		if( headerUserId.equals(rm.getRegisterId()) ) return buildErrorMessage("203", "등록자는 받을 수 없습니다.");
		
		// 머니뿌리가 상태 체크
		// 머니뿌리기 10분이 지나면 받기 실패 음답.
		// 머니뿌리기 시간이 초과한 머니뿌리기 상태 변경.
		if( "1".equals(rm.getStatus()) )
		{
			try
			{
				if( compareIntervalTime(Calendar.MINUTE, 10, rm.getRegisterTime()) ) 
				{
					// 시간 초과
					rm.setStatus( "9" );
					return buildErrorMessage("204", "받기 유효기간이 종료되었습니다.");
				}
			} catch (ParseException e)
			{
				return buildErrorMessage("999", "받기중에 오류가 발생했습니다.");
			}
			
			// 동일사용자 중복 받기 안 됨.
			if( roomMoneyDao.containRoomMoneyUser(key, headerUserId) ) return buildErrorMessage("205", "이미 받은 사람은 중복으로 받을 수 없습니다.");
			
			// 할당되지 않은 받기 리턴
			MoneyUser mu = roomMoneyDao.getRoomMoneyUser( key );
			if( null == mu )
			{
				// 상태 변경
				rm.setStatus( "8" );
				return buildErrorMessage("206", "이미 받기가 완료되었습니다.");
			}
			mu.setReceivedUserId( headerUserId );
			mu.setReceivedTime( new SimpleDateFormat(DATE_FORMAT).format(new Date()) );
			rm.sumReceivedAmount( mu.getReceivedAmount() );
		
	    	return "{\"받은 금액\":" + mu.getReceivedAmount() + "}";
		}
		// 받기 완료
		else if( "8".equals(rm.getStatus()) )
		{
			return buildErrorMessage("206", "이미 받기가 완료되었습니다.");
		}
		// 받기 시간초과
		else
		{
			return buildErrorMessage("204", "받기 유효기간이 종료되었습니다.");
		}
    }
	
	/**
	 * 기준일시와 체크하고자하는 시간이 넘었는지 여부 확인
	 * @param tp  Calendar.DATE/Calendar.MINUTE
	 * @param interval tp에 따른 시간 간격[ex) tp가 Calendar.DATE interval이 10이면, 10일]
	 * @param srcTime 기준일시
	 * @return
	 * @throws ParseException
	 */
	private boolean compareIntervalTime( int tp, int interval, String srcTime ) throws ParseException
	{
		// 현재시각
		SimpleDateFormat sdf = new SimpleDateFormat( DATE_FORMAT );
		String now = sdf.format( new Date() );
		
		// 등록시각 + interval
		Calendar cal = Calendar.getInstance();
		cal.setTime( sdf.parse(srcTime) );
		cal.add( tp, interval );
		
		String calcTime = sdf.format( cal.getTime() );
		
		// 비교
		// < 0 : 시간 초과
		if( calcTime.compareTo(now) < 0) return true;
		
		return false;
	}
	
	/**
	 * 방 ID와 토큰을 조립하여 머니뿌리기 키 생성
	 * @param roomId
	 * @param Token
	 * @return
	 */
	private String buildRoomMoneyKey( String roomId, String token )
	{
		return roomId + "-" + token;
	}
	
	/**
	 * 오류 코드와 메시지를 JSON 문자열로 변환
	 * @param errorCd
	 * @param errorMsg
	 * @return
	 */
	private String buildErrorMessage( String errorCd, String errorMsg )
	{
		return "{\"오류 코드\":\"" + errorCd + "\", \"오류 메시지\":\"" + errorMsg + "\"}"
;	}
}
