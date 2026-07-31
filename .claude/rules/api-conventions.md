---
paths:
  - "src/main/java/**/controller/**/*.java"
  - "src/main/java/**/dto/**/*.java"
---

# API 작업 규칙

- API 관련 작업 시 docs/api/ 폴더의 명세 파일을 먼저 확인할 것
- 명세에 없는 필드는 임의로 추측해서 만들지 말 것
- 응답은 공통 포맷(ApiResponse<T> 등)으로 감싸서 반환
- 예외는 GlobalExceptionHandler(@RestControllerAdvice)에서 전역 처리