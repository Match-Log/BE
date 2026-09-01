package com.matchlog.be.external.oauth.google;

import com.matchlog.be.config.oauth.GoogleOAuthProperties;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.AuthErrorCode;
import com.matchlog.be.external.oauth.OAuthClient;
import com.matchlog.be.external.oauth.OAuthUserInfo;
import com.matchlog.be.external.oauth.google.dto.GoogleTokenResponse;
import com.matchlog.be.external.oauth.google.dto.GoogleUserInfoResponse;
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
public class GoogleOAuthClient implements OAuthClient {

    private final GoogleOAuthProperties properties;
    private final WebClient.Builder webClientBuilder;

    @Override
    public OAuthUserInfo getUserInfo(String code) {
        String accessToken = fetchAccessToken(code);
        return fetchUserInfo(accessToken);
    }

    private String fetchAccessToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("redirect_uri", properties.getRedirectUri());

        GoogleTokenResponse response =
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
                        .bodyToMono(GoogleTokenResponse.class)
                        .block();

        return response.getAccessToken();
    }

    private OAuthUserInfo fetchUserInfo(String accessToken) {
        GoogleUserInfoResponse response =
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
                        .bodyToMono(GoogleUserInfoResponse.class)
                        .block();

        return new OAuthUserInfo(response.getEmail(), response.getName(), response.getPicture());
    }
}
