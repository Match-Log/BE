# Exception 시나리오 문서

서비스 레이어 구현 시 참조용 문서입니다.  
각 서비스 메서드마다 **어떤 조건에서 어떤 에러를 throw** 해야 하는지 정리합니다.

> **공통 규칙**
> - `UNAUTHORIZED` (401) — JWT 필터(`JwtAuthenticationFilter`)에서 처리. 서비스에서 직접 throw 불필요.
> - `INVALID_REQUEST_BODY` (400) — `@Valid` 또는 `HttpMessageNotReadableException`으로 처리. 서비스에서 직접 throw 불필요.
> - `INTERNAL_SERVER_ERROR` (500) — `GlobalExceptionHandler`의 폴백 핸들러가 처리.
> - 위 세 가지를 제외한 나머지만 서비스에서 `throw new CustomException(...)` 처리.

---

## ⚠️ 구현 시 주의사항 (헷갈리기 쉬운 지점 3가지)

서비스 레이어 작성 전 반드시 숙지할 것. 아래 3가지는 ErrorCode enum 자체는 정상 구현되어 있으나, **서비스에서 잘못 쓰기 쉬운 지점**이다.

### (1) "팀 내 대상 조회 실패"에 쓰는 ErrorCode가 API마다 다르다
동일하게 "팀 내 특정 멤버/참가정보를 못 찾음"이지만 API에 따라 던지는 코드가 다르다. **문서 표에 적힌 코드를 그대로 사용**할 것.

| 사용처 | 던질 코드 |
|---|---|
| `ParticipationService.updateParticipation`, `removeFromRoster` | `ParticipationErrorCode.PARTICIPATION_NOT_FOUND` |
| `ParticipationService.assignKicker`, `getKicker` | `TeamErrorCode.MEMBER_NOT_FOUND` |
| `Lineup/Stat/Feedback/Tactic`에서 대상 선수가 팀 소속 아님 | `TeamErrorCode.MEMBER_NOT_FOUND` |

### (2) `MATCH_ALREADY_FINISHED` / `MATCH_NOT_FINISHED`는 반드시 커스텀 메시지로 던진다
두 코드의 enum **기본 메시지는 각각 "수정"·"스탯" 문구로 고정**되어 있다. 삭제·투표·라인업·전술·피드백 등 다른 맥락에서는 아래처럼 **2-arg 생성자로 상황에 맞는 메시지를 반드시 넘길 것** (문서 각 표의 메시지 컬럼 참고).

```java
// ❌ 기본 메시지("종료된 경기는 수정할 수 없습니다.")가 그대로 나감 → 맥락과 안 맞음
throw new CustomException(MatchErrorCode.MATCH_ALREADY_FINISHED);

// ✅ 맥락에 맞는 메시지를 넘김
throw new CustomException(MatchErrorCode.MATCH_ALREADY_FINISHED, "종료된 경기에는 투표할 수 없습니다.");
```

### (3) `FeedbackErrorCode.INVALID_SCOPE`와 `TacticErrorCode.INVALID_SCOPE`는 code 문자열이 같다
둘 다 `code = "INVALID_SCOPE"`, `status = 400`으로 동일하다. 도메인 분리를 위해 enum만 나눠둔 것이므로 **각 서비스는 자기 도메인의 enum을 사용**하면 되지만, 클라이언트는 code 문자열만으로 Feedback/Tactic을 구분할 수 없다는 점을 인지할 것.

---

## 1. AuthService

### `checkEmail(email)`
| 조건 | throw |
|---|---|
| 이미 DB에 존재하는 이메일 | `AuthErrorCode.EMAIL_CONFLICT` |

### `signup(request)`
| 조건 | throw |
|---|---|
| 이미 DB에 존재하는 이메일 | `AuthErrorCode.EMAIL_CONFLICT` |
| ~~provider가 KAKAO인데 password가 포함된 경우~~ MVP에선 고려 대상 X | ~~`CommonErrorCode.INVALID_REQUEST_BODY`, "소셜 로그인 가입 시 비밀번호를 설정할 수 없습니다."~~ |

### `login(request)`
| 조건 | throw |
|---|---|
| 이메일로 유저 조회 실패 | `AuthErrorCode.INVALID_CREDENTIALS` |
| 비밀번호 불일치 | `AuthErrorCode.INVALID_CREDENTIALS` |
| ~~provider가 LOCAL이 아닌 유저가 이메일 로그인 시도~~ | ~~`AuthErrorCode.INVALID_CREDENTIALS`, "소셜 로그인 계정입니다."~~ |

> **Note**: 이메일 미존재와 비밀번호 불일치를 동일 에러코드로 처리 — 보안상 어느 쪽이 틀렸는지 노출하지 않기 위함.

### `reissue(refreshToken)`
| 조건 | throw |
|---|---|
| refreshToken 값이 null 또는 빈 문자열 | `AuthErrorCode.INVALID_REFRESH_TOKEN` |
| 리프레시 토큰이 Redis에 없음 (만료 또는 로그아웃됨) | `AuthErrorCode.INVALID_REFRESH_TOKEN` |
| 리프레시 토큰 파싱/서명 검증 실패 | `AuthErrorCode.INVALID_REFRESH_TOKEN` |
| 토큰의 userId와 Redis 저장 값 불일치 | `AuthErrorCode.INVALID_REFRESH_TOKEN` |

### `logout(userId)`
| 조건 | throw |
|---|---|
| - (JWT 필터 통과 후 Redis에서 토큰 삭제만 하면 됨) | 없음 |

### `oauthLogin(provider, code)` — 소셜 로그인
| 조건 | throw |
|---|---|
| authorization code가 만료되었거나 유효하지 않음 | `AuthErrorCode.INVALID_OAUTH_CODE` |
| 소셜 서버에서 유저 정보 조회 실패 (외부 API 오류) | `AuthErrorCode.OAUTH_SERVER_ERROR` |

---

## 2. UserService

### `getUser(targetUserId, requestUserId)`
| 조건 | throw |
|---|---|
| targetUserId로 유저 조회 실패 | `UserErrorCode.USER_NOT_FOUND` |

> **Note**: 본인/타인 여부는 예외가 아닌 응답 DTO 분기로 처리 (`requestUserId == targetUserId` → 상세 정보 포함).

### `updateProfile(targetUserId, requestUserId, request)`
| 조건 | throw |
|---|---|
| targetUserId로 유저 조회 실패 | `UserErrorCode.USER_NOT_FOUND` |
| `requestUserId != targetUserId` (본인 아님) | `CommonErrorCode.FORBIDDEN`, "본인 프로필만 수정할 수 있습니다." |

### `changePassword(targetUserId, requestUserId, request)`
| 조건 | throw |
|---|---|
| targetUserId로 유저 조회 실패 | `UserErrorCode.USER_NOT_FOUND` |
| `requestUserId != targetUserId` (본인 아님) | `CommonErrorCode.FORBIDDEN`, "본인 비밀번호만 변경할 수 있습니다." |
| provider가 LOCAL이 아닌 유저 (소셜 로그인 유저는 비밀번호 없음) | `CommonErrorCode.FORBIDDEN`, "소셜 로그인 계정은 비밀번호를 변경할 수 없습니다." |
| 현재 비밀번호 불일치 | `AuthErrorCode.INVALID_CURRENT_PASSWORD` |
| 새 비밀번호가 현재 비밀번호와 동일 | `AuthErrorCode.SAME_AS_CURRENT_PASSWORD` |

### `deleteUser(targetUserId, requestUserId)`
| 조건 | throw |
|---|---|
| targetUserId로 유저 조회 실패 | `UserErrorCode.USER_NOT_FOUND` |
| `requestUserId != targetUserId` (본인 아님) | `CommonErrorCode.FORBIDDEN`, "본인 계정만 탈퇴할 수 있습니다." |
| 탈퇴 대상 유저가 MANAGER인 팀이 존재하는 경우 | `UserErrorCode.MANAGER_TEAM_EXISTS`, "MANAGER로 등록된 팀이 있습니다. 팀을 삭제하거나 역할을 이전 후 탈퇴하세요." |

---

## 3. PlayerService

### `registerPlayer(userId, request)`
| 조건 | throw |
|---|---|
| userId로 유저 조회 실패 | `UserErrorCode.USER_NOT_FOUND` |
| 해당 userId로 이미 Player가 존재하는 경우 | `PlayerErrorCode.PLAYER_ALREADY_EXISTS`, "이미 선수 등록이 완료된 계정입니다." |

> **정책 결정**: 중복 등록 불가. 선수 정보 변경은 `updatePlayer()`를 사용.

### `getPlayer(playerId)`
| 조건 | throw |
|---|---|
| playerId로 선수 조회 실패 | `PlayerErrorCode.PLAYER_NOT_FOUND` |

### `updatePlayer(playerId, requestUserId, request)` — 선수 정보 수정
| 조건 | throw |
|---|---|
| playerId로 선수 조회 실패 | `PlayerErrorCode.PLAYER_NOT_FOUND` |
| 해당 playerId의 userId가 requestUserId와 불일치 | `CommonErrorCode.FORBIDDEN`, "본인 선수 정보만 수정할 수 있습니다." |

---

## 4. TeamService

### `createTeam(userId, request)`
| 조건 | throw |
|---|---|
| userId로 Player 조회 실패 (팀 생성 전 선수 등록 필요) | `PlayerErrorCode.PLAYER_NOT_FOUND` |
| 해당 유저가 이미 2개 팀에 소속되어 있음 | `TeamErrorCode.TEAM_LIMIT_EXCEEDED`, "최대 2개 팀까지 소속될 수 있습니다." |

### `getTeam(teamId, requestUserId)`
| 조건 | throw |
|---|---|
| teamId로 팀 조회 실패 | `TeamErrorCode.TEAM_NOT_FOUND` |
| 요청자가 해당 팀 소속이 아님 (Participation 없음) | `CommonErrorCode.FORBIDDEN`, "해당 팀에 접근 권한이 없습니다." |

### `updateTeam(teamId, requestUserId, request)`
| 조건 | throw |
|---|---|
| teamId로 팀 조회 실패 | `TeamErrorCode.TEAM_NOT_FOUND` |
| 요청자의 팀 내 역할이 MANAGER가 아님 | `CommonErrorCode.FORBIDDEN`, "팀 정보 수정 권한이 없습니다. (MANAGER만 가능)" |

### `deleteTeam(teamId, requestUserId)`
| 조건 | throw |
|---|---|
| teamId로 팀 조회 실패 | `TeamErrorCode.TEAM_NOT_FOUND` |
| 요청자의 팀 내 역할이 MANAGER가 아님 | `CommonErrorCode.FORBIDDEN`, "팀 삭제 권한이 없습니다. (MANAGER만 가능)" |

### `getMyTeams(requestUserId)`
| 조건 | throw |
|---|---|
| - (소속 팀 없으면 빈 배열 반환) | 없음 |

### `getInviteCode(teamId, requestUserId)`
| 조건 | throw |
|---|---|
| teamId로 팀 조회 실패 | `TeamErrorCode.TEAM_NOT_FOUND` |
| 요청자의 팀 내 역할이 MANAGER가 아님 | `CommonErrorCode.FORBIDDEN`, "초대 코드 조회 권한이 없습니다. (MANAGER만 가능)" |

### `joinTeam(requestUserId, inviteCode)`
| 조건 | throw |
|---|---|
| requestUserId로 Player 조회 실패 (팀 가입 전 선수 등록 필요) | `PlayerErrorCode.PLAYER_NOT_FOUND` |
| inviteCode로 팀 조회 실패 | `TeamErrorCode.INVITE_CODE_NOT_FOUND` |
| 요청자가 이미 해당 팀에 가입되어 있음 (Participation 존재) | `TeamErrorCode.ALREADY_JOINED` |
| 요청자가 이미 2개 팀에 소속되어 있음 | `TeamErrorCode.TEAM_LIMIT_EXCEEDED`, "최대 2개 팀까지 소속될 수 있습니다." |

---

## 5. ParticipationService

### `getRoster(teamId, requestUserId)`
| 조건 | throw |
|---|---|
| teamId로 팀 조회 실패 | `TeamErrorCode.TEAM_NOT_FOUND` |
| 요청자가 해당 팀 소속이 아님 | `CommonErrorCode.FORBIDDEN`, "해당 팀에 접근 권한이 없습니다." |

### `updateParticipation(teamId, targetPlayerId, requestUserId, request)`
| 조건 | throw |
|---|---|
| teamId + requestUserId로 요청자 Participation 조회 실패 | `CommonErrorCode.FORBIDDEN`, "해당 팀에 접근 권한이 없습니다." |
| 요청자의 팀 내 역할이 MANAGER가 아님 | `CommonErrorCode.FORBIDDEN`, "참가 정보 수정 권한이 없습니다. (MANAGER만 가능)" |
| teamId + targetPlayerId로 대상 Participation 조회 실패 | `ParticipationErrorCode.PARTICIPATION_NOT_FOUND` |
| 자기 자신의 역할을 변경하려는 경우 | `CommonErrorCode.FORBIDDEN`, "본인의 역할은 변경할 수 없습니다." |

### `removeFromRoster(teamId, targetPlayerId, requestUserId)`
| 조건 | throw |
|---|---|
| 요청자의 팀 내 역할이 MANAGER가 아님 | `CommonErrorCode.FORBIDDEN`, "팀원 제외 권한이 없습니다. (MANAGER만 가능)" |
| teamId + targetPlayerId로 Participation 조회 실패 | `ParticipationErrorCode.PARTICIPATION_NOT_FOUND` |
| 자기 자신을 제외하려는 경우 (탈퇴는 별도 API) | `CommonErrorCode.FORBIDDEN`, "본인은 제외할 수 없습니다. 탈퇴 API를 사용하세요." |
| 유일한 MANAGER를 제외하려는 경우 | `TeamErrorCode.LAST_MANAGER_CANNOT_BE_REMOVED`, "팀의 마지막 MANAGER는 제외할 수 없습니다." |

### `assignKicker(teamId, targetPlayerId, requestUserId, request)`
| 조건 | throw |
|---|---|
| 요청자의 팀 내 역할이 MANAGER가 아님 | `CommonErrorCode.FORBIDDEN`, "전담 키커 지정 권한이 없습니다. (MANAGER만 가능)" |
| teamId + targetPlayerId로 Participation 조회 실패 | `TeamErrorCode.MEMBER_NOT_FOUND` |
| isCaptain=true인데 이미 다른 선수가 주장으로 지정되어 있는 경우 | `TeamErrorCode.CAPTAIN_ALREADY_ASSIGNED`, "이미 주장이 지정되어 있습니다." |
| isPkTaker=true인데 이미 다른 선수가 PK 키커로 지정되어 있는 경우 | `TeamErrorCode.PK_TAKER_ALREADY_ASSIGNED`, "이미 PK 키커가 지정되어 있습니다." |

### `getKicker(teamId, targetPlayerId, requestUserId)`
| 조건 | throw |
|---|---|
| 요청자가 해당 팀 소속이 아님 | `CommonErrorCode.FORBIDDEN`, "해당 팀에 접근 권한이 없습니다." |
| teamId + targetPlayerId로 Participation 조회 실패 | `TeamErrorCode.MEMBER_NOT_FOUND` |

---

## 6. MatchService

### `createMatch(teamId, requestUserId, request)`
| 조건 | throw |
|---|---|
| teamId로 팀 조회 실패 | `TeamErrorCode.TEAM_NOT_FOUND` |
| 요청자의 팀 내 역할이 MANAGER가 아님 | `CommonErrorCode.FORBIDDEN`, "경기 생성 권한이 없습니다. (MANAGER만 가능)" |
| matchDate가 과거 날짜인 경우 | `MatchErrorCode.INVALID_MATCH_DATE`, "경기 날짜는 현재 이후여야 합니다." |
| 동일 팀, 동일 날짜/시간에 이미 경기가 존재하는 경우 | `MatchErrorCode.MATCH_ALREADY_EXISTS`, "해당 날짜에 이미 경기가 존재합니다." |

### `getMatch(matchId, requestUserId)`
| 조건 | throw |
|---|---|
| matchId로 경기 조회 실패 | `MatchErrorCode.MATCH_NOT_FOUND` |
| 요청자가 해당 경기의 팀 소속이 아님 | `CommonErrorCode.FORBIDDEN`, "해당 경기에 접근 권한이 없습니다." |

### `getMatches(teamId, requestUserId)`
| 조건 | throw |
|---|---|
| teamId로 팀 조회 실패 | `TeamErrorCode.TEAM_NOT_FOUND` |
| 요청자가 해당 팀 소속이 아님 | `CommonErrorCode.FORBIDDEN`, "해당 팀에 접근 권한이 없습니다." |

### `updateMatch(matchId, requestUserId, request)`
| 조건 | throw |
|---|---|
| matchId로 경기 조회 실패 | `MatchErrorCode.MATCH_NOT_FOUND` |
| 요청자의 팀 내 역할이 MANAGER가 아님 | `CommonErrorCode.FORBIDDEN`, "경기 수정 권한이 없습니다. (MANAGER만 가능)" |
| 이미 종료된 경기(isFinished=true)를 수정하려는 경우 | `MatchErrorCode.MATCH_ALREADY_FINISHED`, "종료된 경기는 수정할 수 없습니다." |
| matchDate를 과거 날짜로 변경하려는 경우 | `MatchErrorCode.INVALID_MATCH_DATE`, "경기 날짜는 현재 이후여야 합니다." |

### `deleteMatch(matchId, requestUserId)`
| 조건 | throw |
|---|---|
| matchId로 경기 조회 실패 | `MatchErrorCode.MATCH_NOT_FOUND` |
| 요청자의 팀 내 역할이 MANAGER가 아님 | `CommonErrorCode.FORBIDDEN`, "경기 삭제 권한이 없습니다. (MANAGER만 가능)" |
| 이미 종료된 경기(isFinished=true)를 삭제하려는 경우 | `MatchErrorCode.MATCH_ALREADY_FINISHED`, "종료된 경기는 삭제할 수 없습니다." |

---

## 7. VoteService

### `submitVote(matchId, requestUserId, request)`
| 조건 | throw |
|---|---|
| matchId로 경기 조회 실패 | `MatchErrorCode.MATCH_NOT_FOUND` |
| 요청자가 해당 경기의 팀 소속이 아님 | `CommonErrorCode.FORBIDDEN`, "해당 경기에 접근 권한이 없습니다." |
| 요청자가 해당 경기에 이미 투표한 기록이 있음 | `MatchErrorCode.ALREADY_VOTED` |
| 이미 종료된 경기(isFinished=true)에 투표 시도 | `MatchErrorCode.MATCH_ALREADY_FINISHED`, "종료된 경기에는 투표할 수 없습니다." |

### `updateVote(matchId, requestUserId, request)`
| 조건 | throw |
|---|---|
| matchId로 경기 조회 실패 | `MatchErrorCode.MATCH_NOT_FOUND` |
| 요청자가 해당 경기의 팀 소속이 아님 | `CommonErrorCode.FORBIDDEN`, "해당 경기에 접근 권한이 없습니다." |
| 요청자의 기존 투표 기록 없음 (PATCH인데 투표 안 한 상태) | `MatchErrorCode.VOTE_NOT_FOUND`, "투표 기록이 없습니다. 먼저 투표해주세요." |
| 이미 종료된 경기(isFinished=true)에 투표 수정 시도 | `MatchErrorCode.MATCH_ALREADY_FINISHED`, "종료된 경기의 투표는 수정할 수 없습니다." |

### `getVoteStatus(matchId, requestUserId)`
| 조건 | throw |
|---|---|
| matchId로 경기 조회 실패 | `MatchErrorCode.MATCH_NOT_FOUND` |
| 요청자가 해당 경기의 팀 소속이 아님 | `CommonErrorCode.FORBIDDEN`, "해당 경기에 접근 권한이 없습니다." |

---

## 8. LineupService

### `saveLineup(matchId, requestUserId, request)`
| 조건 | throw |
|---|---|
| matchId로 경기 조회 실패 | `MatchErrorCode.MATCH_NOT_FOUND` |
| 요청자의 팀 내 역할이 MANAGER가 아님 | `CommonErrorCode.FORBIDDEN`, "라인업 수정 권한이 없습니다. (MANAGER만 가능)" |
| 이미 종료된 경기(isFinished=true)의 라인업을 수정하려는 경우 | `MatchErrorCode.MATCH_ALREADY_FINISHED`, "종료된 경기의 라인업은 수정할 수 없습니다." |
| spots에 포함된 playerId가 해당 팀 소속이 아님 | `TeamErrorCode.MEMBER_NOT_FOUND` |
| 동일한 playerId가 spots에 중복으로 포함된 경우 | `LineupErrorCode.DUPLICATE_PLAYER_IN_LINEUP`, "동일 선수는 한 번만 배치할 수 있습니다." |
| 동일한 position이 선발(isStarter=true) spots에 중복으로 포함된 경우 | `LineupErrorCode.DUPLICATE_POSITION_IN_LINEUP`, "동일 포지션에 두 명을 배치할 수 없습니다." |

### `getLineup(matchId, requestUserId)`
| 조건 | throw |
|---|---|
| matchId로 경기 조회 실패 | `MatchErrorCode.MATCH_NOT_FOUND` |
| 요청자가 해당 경기의 팀 소속이 아님 | `CommonErrorCode.FORBIDDEN`, "해당 경기에 접근 권한이 없습니다." |

---

## 9. StatService

### `savePlayerStat(matchId, targetPlayerId, requestUserId, request)`
| 조건 | throw |
|---|---|
| matchId로 경기 조회 실패 | `MatchErrorCode.MATCH_NOT_FOUND` |
| 요청자의 팀 내 역할이 MANAGER가 아님 | `CommonErrorCode.FORBIDDEN`, "스탯 입력 권한이 없습니다. (MANAGER만 가능)" |
| 아직 종료되지 않은 경기(isFinished=false)에 스탯 입력 시도 | `MatchErrorCode.MATCH_NOT_FINISHED`, "종료된 경기에만 스탯을 입력할 수 있습니다." |
| targetPlayerId가 해당 팀 소속이 아님 | `TeamErrorCode.MEMBER_NOT_FOUND` |
| GK 전용 필드(saves, goalsConceded, cleanSheet)를 GK가 아닌 선수에게 입력하는 경우 | `StatErrorCode.INVALID_STAT_FOR_POSITION`, "해당 포지션에는 입력할 수 없는 스탯입니다." |

### `getPlayerStat(matchId, targetPlayerId, requestUserId)`
| 조건 | throw |
|---|---|
| matchId로 경기 조회 실패 | `MatchErrorCode.MATCH_NOT_FOUND` |
| 요청자가 해당 경기의 팀 소속이 아님 | `CommonErrorCode.FORBIDDEN`, "해당 경기에 접근 권한이 없습니다." |
| targetPlayerId가 해당 팀 소속이 아님 | `TeamErrorCode.MEMBER_NOT_FOUND` |

### `getMatchStats(matchId, requestUserId)`
| 조건 | throw |
|---|---|
| matchId로 경기 조회 실패 | `MatchErrorCode.MATCH_NOT_FOUND` |
| 요청자가 해당 경기의 팀 소속이 아님 | `CommonErrorCode.FORBIDDEN`, "해당 경기에 접근 권한이 없습니다." |

### `getSeasonStats(userId, teamId, requestUserId)`
| 조건 | throw |
|---|---|
| teamId로 팀 조회 실패 | `TeamErrorCode.TEAM_NOT_FOUND` |
| 요청자가 해당 팀 소속이 아님 | `CommonErrorCode.FORBIDDEN`, "해당 팀에 접근 권한이 없습니다." |
| userId로 유저 조회 실패 | `UserErrorCode.USER_NOT_FOUND` |

---

## 10. FeedbackService

### `saveFeedback(matchId, scope, requestUserId, request)`
| 조건 | throw |
|---|---|
| matchId로 경기 조회 실패 | `MatchErrorCode.MATCH_NOT_FOUND` |
| 요청자의 팀 내 역할이 MANAGER가 아님 | `CommonErrorCode.FORBIDDEN`, "개인 전술/피드백 작성 권한이 없습니다. (MANAGER만 가능)" |
| 아직 종료되지 않은 경기(isFinished=false)에 피드백 입력 시도 | `MatchErrorCode.MATCH_NOT_FINISHED`, "종료된 경기에만 피드백을 작성할 수 있습니다." |
| scope=player일 때, 대상 선수가 해당 팀 소속이 아님 | `TeamErrorCode.MEMBER_NOT_FOUND` |
| scope 값이 team/player 외의 값인 경우 | `FeedbackErrorCode.INVALID_SCOPE`, "scope는 team 또는 player여야 합니다." |

### `getFeedback(matchId, scope, requestUserId)`
| 조건 | throw |
|---|---|
| matchId로 경기 조회 실패 | `MatchErrorCode.MATCH_NOT_FOUND` |
| 요청자가 해당 경기의 팀 소속이 아님 | `CommonErrorCode.FORBIDDEN`, "해당 피드백에 접근 권한이 없습니다." |
| scope 값이 team/player 외의 값인 경우 | `FeedbackErrorCode.INVALID_SCOPE` |
| scope=player인데 playerId가 누락된 경우 | `CommonErrorCode.INVALID_REQUEST_BODY`, "개인 피드백 조회 시 playerId가 필요합니다." |

---

## 11. TacticService

### `saveTactic(matchId, scope, requestUserId, request)`
| 조건 | throw |
|---|---|
| matchId로 경기 조회 실패 | `MatchErrorCode.MATCH_NOT_FOUND` |
| 요청자의 팀 내 역할이 MANAGER가 아님 | `CommonErrorCode.FORBIDDEN`, "전술 작성 권한이 없습니다. (MANAGER만 가능)" |
| 이미 종료된 경기(isFinished=true)에 전술 작성 시도 | `MatchErrorCode.MATCH_ALREADY_FINISHED`, "종료된 경기에는 전술을 작성할 수 없습니다." |
| scope=player일 때, 대상 선수가 해당 팀 소속이 아님 | `TeamErrorCode.MEMBER_NOT_FOUND` |
| scope 값이 team/player 외의 값인 경우 | `TacticErrorCode.INVALID_SCOPE` |

### `getTactic(matchId, scope, requestUserId)`
| 조건 | throw |
|---|---|
| matchId로 경기 조회 실패 | `MatchErrorCode.MATCH_NOT_FOUND` |
| 요청자가 해당 경기의 팀 소속이 아님 | `CommonErrorCode.FORBIDDEN`, "해당 전술에 접근 권한이 없습니다." |
| scope 값이 team/player 외의 값인 경우 | `TacticErrorCode.INVALID_SCOPE` |
| scope=player인데 playerId가 누락된 경우 | `CommonErrorCode.INVALID_REQUEST_BODY`, "개인 전술 조회 시 playerId가 필요합니다." |

---

## 12. DocumentService

### `createDocument(requestUserId, request)`
| 조건 | throw |
|---|---|
| request.teamId로 팀 조회 실패 | `TeamErrorCode.TEAM_NOT_FOUND` |
| 요청자가 해당 팀 소속이 아님 | `CommonErrorCode.FORBIDDEN`, "해당 팀에 접근 권한이 없습니다." |
| 요청자의 팀 내 역할이 MANAGER가 아님 | `CommonErrorCode.FORBIDDEN`, "게시글 작성 권한이 없습니다. (MANAGER만 가능)" |
| documentType이 VOTE인 경우 (직접 작성 불가) | `CommonErrorCode.INVALID_REQUEST_BODY`, "VOTE 게시글은 경기 생성 시 자동 생성됩니다." |
| matchId가 포함됐는데 해당 matchId로 경기 조회 실패 | `MatchErrorCode.MATCH_NOT_FOUND` |
| matchId가 포함됐는데 해당 경기가 요청 팀의 경기가 아닌 경우 | `CommonErrorCode.FORBIDDEN`, "해당 경기는 이 팀의 경기가 아닙니다." |

### `getDocument(boardId, requestUserId)`
| 조건 | throw |
|---|---|
| boardId로 게시글 조회 실패 | `DocumentErrorCode.BOARD_NOT_FOUND` |
| 요청자가 해당 게시글의 팀 소속이 아님 | `CommonErrorCode.FORBIDDEN`, "해당 팀에 접근 권한이 없습니다." |

### `getDocuments(teamId, requestUserId)`
| 조건 | throw |
|---|---|
| teamId로 팀 조회 실패 | `TeamErrorCode.TEAM_NOT_FOUND` |
| 요청자가 해당 팀 소속이 아님 | `CommonErrorCode.FORBIDDEN`, "해당 팀에 접근 권한이 없습니다." |

### `updateDocument(boardId, requestUserId, request)`
| 조건 | throw |
|---|---|
| boardId로 게시글 조회 실패 | `DocumentErrorCode.BOARD_NOT_FOUND` |
| 요청자가 작성자도 아니고 MANAGER도 아님 | `CommonErrorCode.FORBIDDEN`, "게시글 수정 권한이 없습니다. (작성자 또는 MANAGER만 가능)" |
| documentType이 VOTE인 게시글 수정 시도 | `DocumentErrorCode.VOTE_DOCUMENT_CANNOT_BE_MODIFIED`, "투표 게시글은 수정할 수 없습니다." |

### `deleteDocument(boardId, requestUserId)`
| 조건 | throw |
|---|---|
| boardId로 게시글 조회 실패 | `DocumentErrorCode.BOARD_NOT_FOUND` |
| 요청자가 작성자도 아니고 MANAGER도 아님 | `CommonErrorCode.FORBIDDEN`, "게시글 삭제 권한이 없습니다. (작성자 또는 MANAGER만 가능)" |
| documentType이 VOTE인 게시글 삭제 시도 | `DocumentErrorCode.VOTE_DOCUMENT_CANNOT_BE_DELETED`, "투표 게시글은 삭제할 수 없습니다. 경기를 삭제하세요." |

---

## 권한 체크 공통 패턴

서비스에서 반복되는 권한 체크 로직을 정리합니다.

### 팀 소속 여부 확인
```java
Participation participation = participationRepository
    .findByTeamIdAndPlayerId(teamId, playerId)
    .orElseThrow(() -> new CustomException(CommonErrorCode.FORBIDDEN, "해당 팀에 접근 권한이 없습니다."));
```

### MANAGER 역할 확인
```java
if (participation.getRole() != ParticipationRole.MANAGER) {
    throw new CustomException(CommonErrorCode.FORBIDDEN, "XXX 권한이 없습니다. (MANAGER만 가능)");
}
```

### 경기의 팀 소속 확인 (matchId → teamId → Participation)
```java
Match match = matchRepository.findById(matchId)
    .orElseThrow(() -> new CustomException(MatchErrorCode.MATCH_NOT_FOUND));

participationRepository.findByTeamIdAndPlayerId(match.getTeam().getId(), playerId)
    .orElseThrow(() -> new CustomException(CommonErrorCode.FORBIDDEN, "해당 경기에 접근 권한이 없습니다."));
```

### 본인 여부 확인
```java
if (!requestUserId.equals(targetUserId)) {
    throw new CustomException(CommonErrorCode.FORBIDDEN, "본인 XXX만 가능합니다.");
}
```

### 경기 종료 여부 확인
```java
if (match.isFinished()) {
    throw new CustomException(MatchErrorCode.MATCH_ALREADY_FINISHED, "종료된 경기는 수정할 수 없습니다.");
}
```

### 팀 소속 개수 제한 확인
```java
long teamCount = participationRepository.countByPlayerId(playerId);
if (teamCount >= 2) {
    throw new CustomException(TeamErrorCode.TEAM_LIMIT_EXCEEDED, "최대 2개 팀까지 소속될 수 있습니다.");
}
```

### 마지막 MANAGER 확인
```java
long managerCount = participationRepository.countByTeamIdAndRole(teamId, ParticipationRole.MANAGER);
if (managerCount <= 1) {
    throw new CustomException(TeamErrorCode.LAST_MANAGER_CANNOT_BE_REMOVED, "팀의 마지막 MANAGER는 제외할 수 없습니다.");
}
```

---

## 추가로 구현 필요한 ErrorCode 목록

현재 구현된 파일(`exception/constant/`) 외에 위 시나리오에서 새로 필요한 ErrorCode입니다.  
아래 항목들은 서비스 레이어 구현 전에 해당 enum 파일에 추가해야 합니다.

### 기존 파일에 항목 추가

| ErrorCode | HTTP | 설명 | 추가할 파일 |
|---|---|---|---|
| `AuthErrorCode.INVALID_CURRENT_PASSWORD` | 400 | 현재 비밀번호 불일치 | `AuthErrorCode.java` |
| `AuthErrorCode.SAME_AS_CURRENT_PASSWORD` | 400 | 새 비밀번호가 현재와 동일 | `AuthErrorCode.java` |
| `AuthErrorCode.INVALID_OAUTH_CODE` | 400 | 유효하지 않은 OAuth 인증 코드 | `AuthErrorCode.java` |
| `AuthErrorCode.OAUTH_SERVER_ERROR` | 502 | 소셜 서버 응답 오류 | `AuthErrorCode.java` |
| `UserErrorCode.MANAGER_TEAM_EXISTS` | 409 | 탈퇴 시 MANAGER 팀 존재 | `UserErrorCode.java` |
| `TeamErrorCode.TEAM_LIMIT_EXCEEDED` | 409 | 최대 팀 소속 수 초과 | `TeamErrorCode.java` |
| `TeamErrorCode.LAST_MANAGER_CANNOT_BE_REMOVED` | 409 | 마지막 MANAGER 제외 불가 | `TeamErrorCode.java` |
| `TeamErrorCode.CAPTAIN_ALREADY_ASSIGNED` | 409 | 주장 중복 지정 | `TeamErrorCode.java` |
| `TeamErrorCode.PK_TAKER_ALREADY_ASSIGNED` | 409 | PK 키커 중복 지정 | `TeamErrorCode.java` |
| `MatchErrorCode.INVALID_MATCH_DATE` | 400 | 경기 날짜 유효성 오류 | `MatchErrorCode.java` |
| `MatchErrorCode.MATCH_ALREADY_EXISTS` | 409 | 동일 날짜 경기 중복 | `MatchErrorCode.java` |
| `MatchErrorCode.MATCH_ALREADY_FINISHED` | 409 | 종료된 경기 수정/삭제 시도 | `MatchErrorCode.java` |
| `MatchErrorCode.MATCH_NOT_FINISHED` | 409 | 미종료 경기에 스탯/피드백 입력 시도 | `MatchErrorCode.java` |
| `MatchErrorCode.VOTE_NOT_FOUND` | 404 | 투표 기록 없음 | `MatchErrorCode.java` |
| `DocumentErrorCode.VOTE_DOCUMENT_CANNOT_BE_MODIFIED` | 409 | VOTE 게시글 수정 불가 | `DocumentErrorCode.java` |
| `DocumentErrorCode.VOTE_DOCUMENT_CANNOT_BE_DELETED` | 409 | VOTE 게시글 삭제 불가 | `DocumentErrorCode.java` |

### 새 파일 생성 필요

| ErrorCode | HTTP | 설명 | 생성할 파일 |
|---|---|---|---|
| `PlayerErrorCode.PLAYER_NOT_FOUND` | 404 | 선수 조회 실패 | `PlayerErrorCode.java` |
| `PlayerErrorCode.PLAYER_ALREADY_EXISTS` | 409 | 선수 중복 등록 | `PlayerErrorCode.java` |
| `LineupErrorCode.DUPLICATE_PLAYER_IN_LINEUP` | 400 | 라인업 선수 중복 | `LineupErrorCode.java` |
| `LineupErrorCode.DUPLICATE_POSITION_IN_LINEUP` | 400 | 라인업 포지션 중복 | `LineupErrorCode.java` |
| `StatErrorCode.INVALID_STAT_FOR_POSITION` | 400 | 포지션 불일치 스탯 | `StatErrorCode.java` |
| `FeedbackErrorCode.INVALID_SCOPE` | 400 | 유효하지 않은 scope 값 | `FeedbackErrorCode.java` |
| `TacticErrorCode.INVALID_SCOPE` | 400 | 유효하지 않은 scope 값 | `TacticErrorCode.java` |
