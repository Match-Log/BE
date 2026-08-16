# 외부 provider 로그인 및 회원가입 (Kakao, Google)

HTTP 메서드: POST
HTTP 상태코드: 200 OK, 400 Bad Request, 409 Conflict, 502 Bad Gateway, 500 Server Error
URL Path (https:// 없으면 FE와 BE 인스턴스 주소): /api/v1/auth/oauth/{provider}
버전: V1
분류: Auth
엑세스 토큰 필요: X

# Path Variable

| 변수 | 값 | 설명 |
| --- | --- | --- |
| provider | KAKAO / GOOGLE | 소셜 로그인 제공자 |

# Request

## Headers

- `Content-Type: application/json`
- `Accept: application/json`

## Body

```json
{
  "code": "인가코드"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| code | string | ✅ | OAuth 인가 코드 · FE가 provider 인증 후 받은 코드 |

# Response

## 200 OK

신규 유저면 자동 가입 후 토큰 발급, 기존 유저면 바로 토큰 발급.

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "name": "임준혁"
}
```

## 400 Bad Request

```json
{
  "error": {
    "code": "INVALID_OAUTH_CODE",
    "message": "유효하지 않은 OAuth 인증 코드입니다."
  }
}
```

## 409 Conflict → 동일 이메일이 다른 provider로 이미 가입된 경우

```json
{
  "error": {
    "code": "PROVIDER_MISMATCH",
    "message": "이미 다른 방식으로 가입된 이메일입니다."
  }
}
```

## 502 Bad Gateway → 소셜 로그인 서버 오류

```json
{
  "error": {
    "code": "OAUTH_SERVER_ERROR",
    "message": "소셜 로그인 서버에서 오류가 발생했습니다."
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
