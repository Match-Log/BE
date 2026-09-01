package com.matchlog.be.external.oauth.kakao;

import com.matchlog.be.config.oauth.KakaoOAuthProperties;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.AuthErrorCode;
import com.matchlog.be.external.oauth.OAuthClient;
import com.matchlog.be.external.oauth.OAuthUserInfo;
import com.matchlog.be.external.oauth.kakao.dto.KakaoTokenResponse;
import com.matchlog.be.external.oauth.kakao.dto.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthClient {

    private final KakaoOAuthProperties properties;
    private final WebClient.Builder webClientBuilder;

    @Override
    public OAuthUserInfo getUserInfo(String code) {
        String accessToken = fetchAccessToken(code);
        return fetchUserInfo(accessToken);
    }

    private String fetchAccessToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("redirect_uri", properties.getRedirectUri());
        form.add("code", code);

        KakaoTokenResponse response =
                webClientBuilder
                        .build()
                        .post()
                        .uri(properties.getTokenUri())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(BodyInserters.fromFormData(form))
                        .retrieve()
                        .onStatus(
                                status -> status.value() == 400,
                                r ->
                                        Mono.error(
                                                new CustomException(
                                                        AuthErrorCode.INVALID_OAUTH_CODE)))
                        .onStatus(
                                status -> status.isError(),
                                r ->
                                        Mono.error(
                                                new CustomException(
                                                        AuthErrorCode.OAUTH_SERVER_ERROR)))
                        .bodyToMono(KakaoTokenResponse.class)
                        .block();

        return response.getAccessToken();
    }

    private OAuthUserInfo fetchUserInfo(String accessToken) {
        KakaoUserInfoResponse response =
                webClientBuilder
                        .build()
                        .get()
                        .uri(properties.getUserInfoUri())
                        .header("Authorization", "Bearer " + accessToken)
                        .retrieve()
                        .onStatus(
                                status -> status.isError(),
                                r ->
                                        Mono.error(
                                                new CustomException(
                                                        AuthErrorCode.OAUTH_SERVER_ERROR)))
                        .bodyToMono(KakaoUserInfoResponse.class)
                        .block();

        KakaoUserInfoResponse.KakaoAccount account = response.getKakaoAccount();
        return new OAuthUserInfo(
                account.getEmail(),
                account.getProfile().getNickname(),
                account.getProfile().getProfileImageUrl());
    }
}
