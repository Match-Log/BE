# MVP 투표 현황 조회

HTTP 메서드: GET
HTTP 상태코드: 200 Ok, 401 Unauthorized, 403 Forbidden, 404 Not Found, 500 Server Error
URL Path (https:// 없으면 FE와 BE 인스턴스 주소): /api/v1/matches/{matchId}/mvp-votes
버전: V1
분류: Match
엑세스 토큰 필요: O

# Request

## Headers

- `Content-Type: application/json`
- `Accept: application/json`
- `Authorization: Bearer {accessToken}`

Body 없음.

# Response

## 200 OK

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

투표가 아직 없으면 `totalVotes: 0`, `results: []`, `winners: []`, `myVote: null`.

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

## 500 Internal Server Error

```json
{
  "error": {
    "code": "INTERNAL_SERVER_ERROR",
    "message": "서버 내부에 오류가 발생했습니다."
  }
}
```
