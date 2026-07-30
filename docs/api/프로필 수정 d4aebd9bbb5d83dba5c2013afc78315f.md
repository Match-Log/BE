# 프로필 수정

HTTP 메서드: PATCH
HTTP 상태코드: 200 Ok, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 500 Server Error
URL Path (https:// 없으면 FE와 BE 인스턴스 주소): /api/v1/users/{userId}
버전: V1
분류: User
엑세스 토큰 필요: O

# Request

## Headers

- `Content-Type: application/json`
- `Accept: application/json`
- `Authorization: Bearer {accessToken}`

## Body

```json
{
  "name": "임준혁",
  "avatarUrl": "https://storage.example.com/avatars/1.jpg"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| name | string | ❌ | 2~50자 |
| avatarUrl | string | ❌ | 프로필 사진 URL |

# Response

## 200 OK

```json
{
  "userId": 1,
  "name": "임준혁",
  "avatarUrl": "https://storage.example.com/avatars/1.jpg",
  "updatedAt": "2025-03-01T00:00:00"
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

## 403 Forbidden

```json
{
  "error": {
    "code": "FORBIDDEN",
    "message": "본인 프로필만 수정할 수 있습니다."
  }
}
```

## 404 Not Found

```json
{
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "존재하지 않는 유저입니다."
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