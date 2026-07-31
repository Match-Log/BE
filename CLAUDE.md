# CLAUDE.md

## 프로젝트 개요
Match-Log — 축구/풋살 팀 매치 기록 서비스 (선수단 관리, 경기 투표, 라인업, 전술, 피드백, 스탯 기록)
- 백엔드: [JAVA SPRING]
- DB: [MYSQL]

## 빌드 & 실행
- 빌드: ./gradlew build
- 테스트: ./gradlew test
- 로컬 실행: ./gradlew bootRun

## 문서 위치
- API 명세: docs/api/
- DB 스키마: docs/db/schema.dbml

## 커밋 규칙

| 커밋 유형 | 의미 |
|---|---|
| feat | 새로운 기능 추가 |
| fix | 버그 수정 |
| docs | 문서 수정 |
| style | 코드 formatting, 세미콜론 누락 등 코드 자체의 변경이 없는 경우 |
| refactor | 코드 리팩토링 |
| test | 테스트 코드, 리팩토링 테스트 코드 추가 |
| chore | 패키지 매니저 수정, 그 외 기타 수정 (ex. .gitignore) |
| design | CSS 등 사용자 UI 디자인 변경 |
| comment | 필요한 주석 추가 및 변경 |
| rename | 파일 또는 폴더명을 수정하거나 옮기는 작업만인 경우 |
| remove | 파일을 삭제하는 작업만 수행한 경우 |
| !BREAKING CHANGE | 커다란 API 변경의 경우 |
| !HOTFIX | 급하게 치명적인 버그를 고쳐야 하는 경우 |

- 커밋 메시지는 위 유형 중 하나로 시작할 것 (예: `feat: 로그인 API 추가`)
- 커밋 하나에는 하나의 유형/목적만 담을 것
