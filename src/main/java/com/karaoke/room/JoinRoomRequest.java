package com.karaoke.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class JoinRoomRequest {
    @NotBlank
    @Size(max=255)
    private String name;
    @Size(max=255)
    private String password;
}
