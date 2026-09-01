# ADR 002: 로그아웃 시 Access Token 블랙리스트 처리

## 상태
결정됨

## 배경

로그아웃 시 Redis에서 Refresh Token만 삭제하면, 이미 발급된 Access Token은 만료(30분)까지 여전히 유효하다.
탈취된 Access Token으로 로그아웃 후에도 API 호출이 가능한 보안 취약점이 존재한다.

## 결정

로그아웃 시 해당 Access Token을 Redis 블랙리스트에 등록한다.

**키 구조**: `blacklist:{accessToken}`
**TTL**: Access Token의 남은 유효시간 (만료와 동시에 Redis에서 자동 삭제)

## 흐름

```
로그아웃 요청
  → Redis에서 refresh:{userId} 삭제
  → Access Token 남은 유효시간 계산
  → blacklist:{accessToken} 을 남은 TTL로 Redis 저장

이후 요청
  → JwtAuthFilter: JWT 서명 검증 통과
  → Redis blacklist 조회
  → 키 존재하면 인증 등록 안 함 → 401
```

## TTL을 남은 유효시간으로 설정하는 이유

Access Token 만료 이후에는 JWT 서명 검증 단계에서 이미 reject되므로 블랙리스트 키가 필요 없다.
TTL을 남은 유효시간으로 설정하면 토큰 만료와 동시에 Redis 키도 자동 삭제되어 메모리 낭비를 막는다.

## 트레이드오프

- 모든 인증 요청마다 Redis 조회가 1회 추가됨
- Redis 장애 시 블랙리스트 조회 실패 → fail-open 전략으로 토큰 허용 (가용성 우선)
