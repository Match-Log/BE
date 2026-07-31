---
paths:
  - "src/main/java/**/*.java"
---

# 패키지 구조 컨벤션

## repository
- `JpaRepository<Entity, Long>` 상속
- JPQL은 `@Query` + `@Param` 조합
- 필요 시 `repository/projection`에 인터페이스 프로젝션

## domain
- JPA 엔티티: `@Entity @Getter @NoArgsConstructor @AllArgsConstructor @Builder`
- public setter 없이 의도가 드러나는 메서드로 상태 변경 (예: `increaseParticipantCount()`, `changeStatusAsFinished()`)
- 생성은 정적 팩토리 `create(...)` 사용
- 타임스탬프는 `@PrePersist`/`@PreUpdate`
- 동시성 필요한 곳엔 `@Version`

## dto/\<feature\>/request, dto/\<feature\>/response
- 컨트롤러 경계의 요청/응답 계약
- 네이밍: `XxxRequestDto` / `XxxResponseDto`
- Lombok: `@Getter @Builder @AllArgsConstructor @NoArgsConstructor`
- 엔티티 → DTO 변환은 정적 팩토리 `from(...)`
- `LocalDateTime`은 `@JsonFormat(..., timezone = "Asia/Seoul")`

## model/\<feature\>
- dto와 달리 여러 곳(여러 DTO/서비스/캐시)에서 재사용되는 내부 값 객체
- 대부분 Java record (불변): `GatheringListItem`, `PageMeta`, `PrefixedId`, `HostSummary`
- 특정 엔드포인트 계약이 아니라 구조적 빌딩 블록이라는 게 dto와의 구분 기준

## constant/\<feature\>
- 상태/정렬/타입 enum: `GatheringStatus`, `GatheringSort`, `ParticipationStatus`, `ResourceType`(prefixed id 접두사), `CacheKeys`(캐시 키 상수 + 빌더 메서드)

## exception
- `ErrorCode` 인터페이스(code/message/status) + `exception/constant/XxxErrorCode` 도메인별 enum이 구현
- `CustomException`은 `ErrorCode`를 감싸는 단일 범용 `RuntimeException`
- `GlobalExceptionHandler`(`@RestControllerAdvice`)가 예외 타입 → `ErrorResponseDto`로 매핑
- `exception/annotation`엔 커스텀 Bean Validation 애너테이션

## external/\<provider\>
- 외부 서버 연동 클라이언트 (예: `AiApiClient`, WebClient 기반)
- 동기/비동기(`@Async`) 메서드 쌍으로 제공
- `mock` 서브패키지에 로컬용 스텁 구현

## util/\<concern\>
- 특정 도메인에 안 묶이는 횡단 관심사
- `cache`(`HomeCache` — 제네릭 cache-aside 래퍼), `jwt`, `object`(S3 URL 리졸버), `ai`(AI 요청/응답 매핑), `scheduler`(`@Scheduled` 잡)

## config/\<concern\>
- `@Configuration` 클래스, 외부 시스템/횡단 관심사별로 묶임 (`redis`, `aws`, `ai`, `auth`)
- `XxxProps`(`@ConfigurationProperties`류)와 짝을 이루는 경우 많음

## 테스트 구조
- `src/test/.../com/{package}/...`가 main 패키지 경로를 그대로 미러링
- 테스트 클래스는 프로덕션 클래스 1:1이 아니라 **"메서드/시나리오 1개당 테스트 클래스 1개"** 단위
    - 예: `service/gathering/CreateGatheringUnitTest`, `FinishGatheringUnitTest`, `GetGatheringListUnitTest` — 전부 `GatheringService`를 테스트하지만 클래스가 나뉨
- `@Tag("unit")` + Mockito `@Mock`/`@InjectMocks` 조합이 표준