package com.matchlog.be.dto.player.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.constant.lineup.Position;
import com.matchlog.be.constant.player.Career;
import com.matchlog.be.constant.player.PreferredFoot;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterPlayerRequestDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private Integer height;
    private Integer weight;
    private PreferredFoot preferredFoot;
    private Career career;

    @Min(0)
    private Integer yearsOfExperience;

    @NotNull private Position preferredPosition;

    @NotNull private Position subPosition;
}
