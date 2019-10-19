## Send - 요청 패킷 만들기 - 1) 일반적 상황-목적지 IP주소 입력

## Send - 요청 패킷 만들기 - 2) 내 MAC주소의 변경
1. (새 MAC주소 != 기존 MAC주소) --> Flag = true
2. if (flag == true) --> ARP_request.ipAddr = my_ipAddr

## Receive - 요청 패킷인데 IP주소가 내 IP주소
1. 내 MAC 주소를 담은 응답 패킷 만들기 ---> Send 

## Receive - 응답 패킷
1. 받은 MAC주소를 캐시 테이블에 저장
