---
paths:
  - "src/main/java/**/repository/**/*.java"
---

# Repository 작업 규칙

서비스 레이어 구현 전, repository를 짤 때 반복적으로 지켜야 할 원칙을 정리한다.
기본 패키지/네이밍은 `package-structure.md`, 스키마/연관관계는 `db-conventions.md`를 함께 따른다.

## 1. 기본 규약

- `JpaRepository<Entity, Long>` 상속, 위치는 `repository/`
- JPQL은 `@Query` + `@Param` 조합 사용
- 동적 쿼리 등 커스텀 구현이 필요하면 `XxxRepositoryCustom` 인터페이스 + `XxxRepositoryImpl`로 분리
- 응답 전용 조회는 엔티티 전체 대신 `repository/projection/`의 인터페이스 프로젝션으로 받는다

## 2. 데이터 정합성은 애플리케이션 체크가 아니라 DB 제약으로 (가장 중요)

- `existsBy...` 조회 후 insert 하는 방식만으로는 동시 요청에서 뚫린다(TOCTOU). 조회 체크는 **UX용 에러 메시지** 목적, DB 유니크 제약은 **최종 안전장치** — 둘 다 둔다.
- 관련 시나리오: `ALREADY_JOINED`, `ALREADY_VOTED`, `CAPTAIN_ALREADY_ASSIGNED`, `PK_TAKER_ALREADY_ASSIGNED`
- **선행 과제 (현재 갭)**: 아래 복합 유니크 제약이 `docs/db/schema.dbml`과 엔티티 양쪽에 아직 없다. repository/service 구현 전에 `@UniqueConstraint` + DBML 반영이 필요하다.
  - `Participation(teamId, playerId)` — 같은 선수가 같은 팀에 중복 가입 방지
  - `Vote(matchId, playerId)` — 한 경기 중복 투표 방지

## 3. N+1 방지 — 목록/상세 조회는 fetch join / `@EntityGraph` 필수

모든 `@ManyToOne`이 LAZY이므로 목록 조회에서 반드시 N+1이 터진다(`db-conventions.md` 규칙).
matchLog 고위험 조회 3곳은 처음부터 fetch join 또는 `@EntityGraph`로 작성한다.

- **로스터** — `Participation → Player → User` (3단계, name/profileImage가 User에)
- **라인업** — `LineupSpot → Player` (quarter마다 spot 반복)
- **경기 스탯 / 투표 현황** — 선수 name·profileImage 동반 조회

## 4. 관심사 분리 — repository는 `Optional` 반환, 예외는 서비스에서

- `orElseThrow`를 repository에 넣지 않는다. repository는 순수 조회만 담당한다.
- 같은 조회라도 맥락에 따라 던지는 코드가 갈리기 때문이다. 예: 팀 내 대상 조회 실패가 `PARTICIPATION_NOT_FOUND`(updateParticipation/removeFromRoster)일 수도, `MEMBER_NOT_FOUND`(assignKicker/getKicker)일 수도 있다. → `docs/exception/exception-scenarios.md`의 "구현 시 주의사항 (1)" 참고.

## 5. 존재/개수만 필요하면 `countBy` / `existsBy`

엔티티를 통째로 로딩하지 말고 count/exists로 가볍게 처리한다.

- 팀 2개 제한 → `countByPlayerId`
- 마지막 MANAGER 검증 → `countByTeamIdAndRole`
- 이메일 중복 → `existsByEmail`

## 6. 중첩 프로퍼티 네이밍 함정

엔티티는 연관을 `Player`/`Team` **객체**로 보유하는데 시나리오는 `findByTeamIdAndPlayerId`처럼 ID로 부른다.
Spring Data가 `team.id`로 해석하긴 하지만 `teamId` 필드로 오인할 여지가 있으므로, 모호하면 `findByTeam_IdAndPlayer_Id`로 명시하거나 `@Query`로 못박는다.

## 7. 프로젝션 / 집계

- 응답 전용 조회(로스터·스탯 등)는 필요한 컬럼만 인터페이스 프로젝션으로 뽑는다.
- 시즌 스탯 등 집계는 `@Query` + `GROUP BY` + SUM을 프로젝션으로 수신한다. 집계 조회는 `@Transactional(readOnly = true)`.

## 8. 테스트 방언 (H2 vs MySQL)

- `build.gradle`에 H2와 TestContainers-MySQL이 공존한다.
- `PersonalFeedback.tags`가 JSON 컬럼이라 H2로 테스트하면 실제 MySQL과 동작이 달라진다.
- repository 통합 테스트는 `@DataJpaTest` + **TestContainers-MySQL** 기준으로 작성한다.

## 9. 기타 체크리스트

- `spring.jpa.open-in-view`는 기본 true → **false 권장**. 지연 로딩은 서비스 트랜잭션 경계 안에서 처리(LazyInitializationException 주의).
- 목록 API가 커지면 `Pageable` 도입 검토(`Slice` vs `Page`).
- 경기 `status`(upcoming/finished/all) 등 동적 조건이 늘면 QueryDSL 도입 검토(현재 의존성 없음, MVP는 JPQL로 충분).
