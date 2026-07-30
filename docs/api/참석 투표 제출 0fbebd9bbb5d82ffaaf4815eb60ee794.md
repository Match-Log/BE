# 참석 투표 제출

HTTP 메서드: POST
HTTP 상태코드: 201 Created, 400 Bad Request, 401 Unauthorized, 404 Not Found, 409 Conflict, 500 Server Error
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

## 201 Created

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
    "code": "ALREADY_VOTED",
    "message": "이미 투표한 경기입니다. 수정은 PATCH를 사용하세요."
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