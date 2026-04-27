package com.karaoke.room.song;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.hibernate.validator.constraints.UUID;

@Getter
public class SongRequest {
    @NotBlank
    @Size(max=255)
    private String name;

    @NotBlank
    @JsonProperty("user_id")
    @UUID
    private String userId;

    @NotBlank
    @Pattern(
            regexp = "^((?:https?:)?\\/\\/)?((?:www|m)\\.)?((?:youtube\\.com|youtu.be))(\\/(?:[\\w\\-]+\\?v=|embed\\/|v\\/)?)([\\w\\-]+)(\\S+)?$"
    )
    private String url;
}
