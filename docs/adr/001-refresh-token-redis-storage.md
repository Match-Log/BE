# ADR 001: Refresh Token 저장소로 Redis 선택

## 상태
결정됨

## 배경

인증 구조를 설계할 때 Refresh Token을 어디에 저장할지 결정해야 했다.
비교 대상은 **DB 저장 방식**과 **Redis 저장 방식** 두 가지였다.

참고로 유사 프로젝트(gangku)는 User 엔티티에 `refreshToken`, `refreshTokenExpiresAt` 필드를 두고 DB에서 관리한다.

## 선택지 비교

| 항목 | DB 저장 (gangku 방식) | Redis 저장 (matchLog 방식) |
|---|---|---|
| reissue 시 DB 조회 | O (User SELECT + UPDATE) | X |
| logout 시 DB 조회 | O (User UPDATE) | X |
| TTL 자동 만료 | X (ExpiresAt 컬럼 직접 관리) | O (Redis TTL) |
| User 엔티티 복잡도 | 증가 (토큰 관련 필드 추가) | 유지 |
| 인프라 의존성 | DB만 | DB + Redis |

## 결정

**Redis에 저장** (`refresh:{userId}` 키, TTL 7일)

- reissue·logout 요청이 DB write 없이 Redis 조작만으로 처리됨
- TTL 설정으로 만료 관리를 Redis에 위임, 별도 스케줄러 불필요
- User 엔티티가 인증 관심사를 갖지 않아도 됨

## 트레이드오프

- Redis 장애 시 모든 Refresh Token이 소실되어 전 사용자 재로그인 필요
- DB 방식 대비 인프라 의존성이 하나 늘어남

## 미결 사항

- logout 시 Access Token 블랙리스트 처리 여부 → [ADR 002](./002-access-token-blacklist.md) 참고
