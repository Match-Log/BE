package com.matchlog.be.external.oauth;

public interface OAuthClient {
    OAuthUserInfo getUserInfo(String code);
}
