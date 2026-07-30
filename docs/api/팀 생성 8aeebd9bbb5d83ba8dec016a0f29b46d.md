# 팀 생성

HTTP 메서드: POST
HTTP 상태코드: 201 Created, 400 Bad Request, 401 Unauthorized, 500 Server Error
URL Path (https:// 없으면 FE와 BE 인스턴스 주소): /api/v1/teams
버전: V1
분류: Team
엑세스 토큰 필요: O

# Request

## Headers

- `Content-Type: application/json`
- `Accept: application/json`
- `Authorization: Bearer {accessToken}`

## Body

```json
{
  "name": "FC 한강불사조",
  "teamImage": "https://storage.example.com/teams/1.jpg",
  "region": "서울 마포구",
  "foundedYear": 2020,
  "homeGround": "한강공원 풋살장"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| name | string | ✅ | 팀 이름 · 2~100자 |
| teamImage | string | ❌ | 팀 이미지 URL · nullable |
| region | string | ❌ | 활동 지역 |
| foundedYear | int | ❌ | 창립 연도 |
| homeGround | string | ❌ | 주 활동 장소 |

inviteCode는 서버에서 자동 생성됩니다. (재발급 불가)

# Response

## 201 Created

```json
{
  "teamId": 1,
  "name": "FC 한강불사조",
  "teamImage": null,
  "region": "서울 마포구",
  "foundedYear": 2020,
  "homeGround": "한강공원 풋살장",
  "inviteCode": "HK4829",
  "createdAt": "2025-03-01T00:00:00"
}
```

## 400 Bad Request

```json
{
  "error": {
    "code": "INVALID_REQUEST_BODY",
    "message": "요청 값이 올바르지 않습니다."
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

## 500 Internal Server Error

```json
{
  "error": {
    "code": "INTERNAL_SERVER_ERROR",
    "message": "서버 내부에 오류가 발생했습니다."
  }
}
```