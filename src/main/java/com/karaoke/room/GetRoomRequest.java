package com.karaoke.room;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class GetRoomRequest {
    @NotBlank
    private String uuid;
}
