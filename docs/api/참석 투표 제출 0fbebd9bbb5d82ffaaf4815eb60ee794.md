# 참석 투표 저장 (upsert)

HTTP 메서드: PUT
HTTP 상태코드: 200 OK, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict, 500 Server Error
URL Path (https:// 없으면 FE와 BE 인스턴스 주소): /api/v1/matches/{matchId}/votes
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
  "status": "ATTEND"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| status | string | ✅ | ATTEND / PENDING / ABSENT |

# Response

## 200 OK

투표가 없으면 생성, 있으면 수정.

```json
{
  "matchId": 1,
  "playerId": 9,
  "status": "ATTEND",
  "votedAt": "2025-07-10T10:00:00"
}
```

## 400 Bad Request

```json
{
  "error": {
    "code": "INVALID_REQUEST_BODY",
    "message": "유효하지 않은 투표 상태입니다."
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
    "message": "해당 경기에 접근 권한이 없습니다."
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

## 409 Conflict

```json
{
  "error": {
    "code": "MATCH_ALREADY_FINISHED",
    "message": "종료된 경기는 수정할 수 없습니다."
  }
}
```

```json
{
  "error": {
    "code": "VOTE_DEADLINE_PASSED",
    "message": "경기 시작 1시간 전부터 투표할 수 없습니다."
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
