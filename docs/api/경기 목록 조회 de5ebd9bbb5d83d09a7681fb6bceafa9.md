# 경기 목록 조회

HTTP 메서드: GET
HTTP 상태코드: 200 Ok, 401 Unauthorized, 403 Forbidden, 404 Not Found, 500 Server Error
URL Path (https:// 없으면 FE와 BE 인스턴스 주소): /api/v1/teams/{teamId}/matches
버전: V1
분류: Match
엑세스 토큰 필요: O

# Request

## Headers

- `Content-Type: application/json`
- `Accept: application/json`
- `Authorization: Bearer {accessToken}`

Body 없음.

예시: `GET /api/v1/teams/{teamId}/matches?status=upcoming`

| 파라미터 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| status | string | ❌ | upcoming / finished / all (기본값: all) |

# Response

## 200 OK

```json
[
  {
    "matchId": 1,
    "opponent": "서울 드래곤즈",
    "matchDate": "2025-07-14T07:00:00",
    "location": "한강공원 풋살장",
    "homeAway": "HOME",
    "matchType": "SOCCER",
    "scoreHome": null,
    "scoreAway": null,
    "isFinished": false
  }
]
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
    "code": "TEAM_NOT_FOUND",
    "message": "존재하지 않는 팀입니다."
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