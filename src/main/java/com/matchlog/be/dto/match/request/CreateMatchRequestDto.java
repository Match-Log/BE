package com.matchlog.be.dto.match.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.matchlog.be.constant.match.HomeAway;
import com.matchlog.be.constant.match.MatchType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMatchRequestDto {

    @NotBlank private String opponent;

    @NotNull
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime matchDate;

    private String location;

    @Size(max = 255)
    private String locationAddress;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitude;

    @Size(max = 30)
    private String kakaoPlaceId;

    private String memo;

    @NotNull private HomeAway homeAway;

    @NotNull private MatchType matchType;

    /** 위도·경도는 둘 다 있거나 둘 다 없어야 한다. */
    @JsonIgnore
    @AssertTrue
    public boolean isCoordinatePairValid() {
        return (latitude == null) == (longitude == null);
    }

    /** 장소 이름 없이 주소/좌표/카카오 장소 ID만 보낼 수 없다. */
    @JsonIgnore
    @AssertTrue
    public boolean isLocationDetailWithName() {
        return location != null
                || (locationAddress == null
                        && latitude == null
                        && longitude == null
                        && kakaoPlaceId == null);
    }
}
