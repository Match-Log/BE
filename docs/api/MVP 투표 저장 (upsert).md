# MVP 투표 저장 (upsert)

HTTP 메서드: PUT
HTTP 상태코드: 200 OK, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict, 500 Server Error
URL Path (https:// 없으면 FE와 BE 인스턴스 주소): /api/v1/matches/{matchId}/mvp-votes
버전: V1
분류: Match
엑세스 토큰 필요: O

# Request

## Headers

- `Content-Type: application/json`
- `Accept: application/json`
- `Authorization: Bearer {accessToken}`

## Body

```json
{
  "votedPlayerId": 9
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| votedPlayerId | number | ✅ | MVP로 지명할 선수의 playerId. 본인 투표 허용. |

# Response

## 200 OK

투표가 없으면 생성, 있으면 재투표(수정). 저장 즉시 해당 경기 전체 재집계 — 최다득표 전원(동점 포함)이 `winners`에 반영됨.

```json
{
  "matchId": 1,
  "totalVotes": 5,
  "results": [
    { "playerId": 9, "name": "임준혁", "voteCount": 3 },
    { "playerId": 4, "name": "김민준", "voteCount": 2 }
  ],
  "winners": [9],
  "myVote": 9
}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| totalVotes | number | 해당 경기에 투표된 전체 표 수 |
| results | array | 후보별 득표 현황, 득표수 내림차순 |
| winners | number[] | 최다득표 playerId 목록. 동점이면 2명 이상, 득표 0표면 빈 배열 |
| myVote | number \| null | 요청자 본인이 투표한 playerId. 투표 안 했으면 null |

## 400 Bad Request

```json
{
  "error": {
    "code": "INVALID_REQUEST_BODY",
    "message": "votedPlayerId는 필수입니다."
  }
}
```

## 401 Unauthorized

```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "인증이 필요합니다."
  }
}
```

## 403 Forbidden

```json
{
  "error": {
    "code": "FORBIDDEN",
    "message": "해당 팀에 접근 권한이 없습니다."
  }
}
```

## 404 Not Found

```json
{
  "error": {
    "code": "MATCH_NOT_FOUND",
    "message": "존재하지 않는 경기입니다."
  }
}
```

```json
{
  "error": {
    "code": "PLAYER_NOT_FOUND",
    "message": "존재하지 않는 선수입니다."
  }
}
```

```json
{
  "error": {
    "code": "MEMBER_NOT_FOUND",
    "message": "해당 팀에 존재하지 않는 멤버입니다."
  }
}
```

## 409 Conflict

```json
{
  "error": {
    "code": "MATCH_NOT_FINISHED",
    "message": "종료된 경기에만 MVP 투표를 할 수 있습니다."
  }
}
```

## 500 Internal Server Error

```json
{
  "error": {
    "code": "INTERNAL_SERVER_ERROR",
    "message": "서버 내부에 오류가 발생했습니다."
  }
}
```
