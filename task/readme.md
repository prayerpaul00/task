데이터베이스와 연결을 한 소스를 작성하지 못해서 다수의 서버와 다수의 인스턴스에서 실행가능한 소스가 아닙니다.
요즘 Cloud 업체들이 무료로 1년간 사용하게 하는 것을 늦게 알게되어서 요구한 사항을 만족하지 못했습니다. 
해당 과제를 진행하기 위해서 제가 경험하지 못했던 요즘의 기술들을 짧은 시간에 익혀서 수행하는데 한계가 있었음을 밝힙니다.
테스트 코드 또한 Spring Boot에서 Unit Test를 하기위해서 알아야 할 것들을 찾아서 하기에는 주어진 시간이 저에게는 부족했습니다.

아래에 curl명령어로라도 테스트 한 내용을 첨부합니다.

1. 머니뿌리기
1.1 정상
==> request
curl --location --request POST 'http://localhost:8080/moneys/' \
--header 'X-USER-ID: test1' \
--header 'X-ROOM-ID: 12345' \
--header 'Content-Type: application/json' \
--data-raw '{"numOfUser":"4", "amount":"100003"}'
==> response
{"머니뿌리기 토큰":"2il"}
1.2 정상
==> request
curl --location --request POST 'http://localhost:8080/moneys/' \
--header 'X-USER-ID: test1' \
--header 'X-ROOM-ID: 67890' \
--header 'Content-Type: application/json' \
--data-raw '{"numOfUser":"5", "amount":"200003"}'
==> response
{"머니뿌리기 토큰":"jDQ"}
1.3 정상
==> request
curl --location --request POST 'http://localhost:8080/moneys/' \
--header 'X-USER-ID: test1' \
--header 'X-ROOM-ID: 12345' \
--header 'Content-Type: application/json' \
--data-raw '{"numOfUser":"4", "amount":"100003"}'
==> response
{"머니뿌리기 토큰":"pD2"}


2. 머니뿌리기 조회
2.1 등록자의 경우
==> request
curl --location --request GET 'http://localhost:8080/moneys/' \
--header 'X-USER-ID: test1' \
--header 'X-ROOM-ID: 12345' \
--header 'Content-Type: application/json' \
--data-raw '{"moneyToken": "2il"}'
==> response
{"뿌린 시각":"20200627133920.983","뿌린 금액":100003,"받기 완료된 금액":0,"받기 완료된 정보":[]}
==> request
curl --location --request GET 'http://localhost:8080/moneys/' \
--header 'X-USER-ID: test1' \
--header 'X-ROOM-ID: 67890' \
--header 'Content-Type: application/json' \
--data-raw '{"moneyToken": "jDQ"}'
==> response
{"뿌린 시각":"20200627134049.386","뿌린 금액":200003,"받기 완료된 금액":0,"받기 완료된 정보":[]}
2.2 머니뿌리기 조회(등록자가 아닌 경우)
2.2.1
==> request
curl --location --request GET 'http://localhost:8080/moneys/' \
--header 'X-USER-ID: test2' \
--header 'X-ROOM-ID: 12345' \
--header 'Content-Type: application/json' \
--data-raw '{"moneyToken": "2il"}'
==> response
{"오류 코드":"102", "오류 메시지":"머니뿌리기 조회는 등록자만 가능합니다."}
2.2.2
==> request
curl --location --request GET 'http://localhost:8080/moneys/' \
--header 'X-USER-ID: test2' \
--header 'X-ROOM-ID: 67890' \
--header 'Content-Type: application/json' \
--data-raw '{"moneyToken": "jDQ"}'
==> response
{"오류 코드":"102", "오류 메시지":"머니뿌리기 조회는 등록자만 가능합니다."}


3. 받기 
3.1 유효기간 종료(10분지나서 테스트)
==> request
curl --location --request PUT 'http://localhost:8080/moneys/' \
--header 'X-USER-ID: test2' \
--header 'X-ROOM-ID: 12345' \
--header 'Content-Type: application/json' \
--data-raw '{"moneyToken": "2il"}'
==> response
{"오류 코드":"204", "오류 메시지":"받기 유효기간이 종료되었습니다."}
3.2 정상 받기
==> request
curl --location --request PUT 'http://localhost:8080/moneys/' \
--header 'X-USER-ID: test2' \
--header 'X-ROOM-ID: 12345' \
--header 'Content-Type: application/json' \
--data-raw '{"moneyToken": "pD2"}'
==> response
{"받은 금액":25003}
3.3 정상 받기
==> request
curl --location --request PUT 'http://localhost:8080/moneys/' \
--header 'X-USER-ID: test3' \
--header 'X-ROOM-ID: 12345' \
--header 'Content-Type: application/json' \
--data-raw '{"moneyToken": "pD2"}'
==> response
{"받은 금액":25000}
3.4 정상 받기
==> request
curl --location --request PUT 'http://localhost:8080/moneys/' \
--header 'X-USER-ID: test4' \
--header 'X-ROOM-ID: 12345' \
--header 'Content-Type: application/json' \
--data-raw '{"moneyToken": "pD2"}'
==> response
{"받은 금액":25000}
3.5 조회
curl --location --request GET 'http://localhost:8080/moneys/' \
--header 'X-USER-ID: test1' \
--header 'X-ROOM-ID: 12345' \
--header 'Content-Type: application/json' \
--data-raw '{"moneyToken": "pD2"}'
==> response
{"뿌린 시각":"20200627135357.918","뿌린 금액":100003,"받기 완료된 금액":75003,"받기 완료된 정보":[{"받은 사용자 아이디":"test2","받은 금액":25003,"받은 시각":"20200627135552.031"},{"받은 사용자 아이디":"test3","받은 금액":25000,"받은 시각":"20200627135636.935"},{"받은 사용자 아이디":"test4","받은 금액":25000,"받은 시각":"20200627135717.764"}]}
3.6 등록자 받기 오류
curl --location --request PUT 'http://localhost:8080/moneys/' \
--header 'X-USER-ID: test1' \
--header 'X-ROOM-ID: 12345' \
--header 'Content-Type: application/json' \
--data-raw '{"moneyToken": "pD2"}'
==> response
{"오류 코드":"203", "오류 메시지":"등록자는 받을 수 없습니다."}
3.7 중복 받기 오류
curl --location --request PUT 'http://localhost:8080/moneys/' \
--header 'X-USER-ID: test4' \
--header 'X-ROOM-ID: 12345' \
--header 'Content-Type: application/json' \
--data-raw '{"moneyToken": "pD2"}'
==> response
{"오류 코드":"205", "오류 메시지":"이미 받은 사람은 중복으로 받을 수 없습니다."}
3.7 정상 받기
==> request
curl --location --request PUT 'http://localhost:8080/moneys/' \
--header 'X-USER-ID: test5' \
--header 'X-ROOM-ID: 12345' \
--header 'Content-Type: application/json' \
--data-raw '{"moneyToken": "pD2"}'
==> response
{"받은 금액":25000}
3.1.8 받기 완료 오류
==> request
curl --location --request PUT 'http://localhost:8080/moneys/' \
--header 'X-USER-ID: test6' \
--header 'X-ROOM-ID: 12345' \
--header 'Content-Type: application/json' \
--data-raw '{"moneyToken": "pD2"}'
==> response
{"오류 코드":"206", "오류 메시지":"이미 받기가 완료되었습니다."}
3.1.9 받기 완료 조회
==> request
curl --location --request GET 'http://localhost:8080/moneys/' \
--header 'X-USER-ID: test1' \
--header 'X-ROOM-ID: 12345' \
--header 'Content-Type: application/json' \
--data-raw '{"moneyToken": "pD2"}'
==> response
{"뿌린 시각":"20200627135357.918","뿌린 금액":100003,"받기 완료된 금액":100003,"받기 완료된 정보":[{"받은 사용자 아이디":"test2","받은 금액":25003,"받은 시각":"20200627135552.031"},{"받은 사용자 아이디":"test3","받은 금액":25000,"받은 시각":"20200627135636.935"},{"받은 사용자 아이디":"test4","받은 금액":25000,"받은 시각":"20200627135717.764"},{"받은 사용자 아이디":"test5","받은 금액":25000,"받은 시각":"20200627140052.927"}]}