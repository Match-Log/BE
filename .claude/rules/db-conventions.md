---
paths:
  - "src/main/java/**/domain/**/*.java"
  - "src/main/java/**/repository/**/*.java"
---

# DB 작업 규칙

- 테이블/컬럼/관계 관련 작업 시 docs/db/schema.dbml을 먼저 확인할 것
- 스키마에 없는 컬럼이나 관계를 임의로 추측해서 만들지 말 것
- 연관관계는 기본 지연로딩(LAZY), N+1 문제 예상되는 조회는 fetch join 또는 @EntityGraph 사용
- 스키마 변경 시 dbdiagram.io에서 수정한 DBML 코드를 그대로 복사해서 docs/db/schema.dbml 덮어쓰기