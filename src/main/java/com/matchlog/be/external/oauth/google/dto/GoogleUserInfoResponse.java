package com.matchlog.be.external.oauth.google.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleUserInfoResponse {

    private String email;
    private String name;
    private String picture;
}
