package com.karaoke.room;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.hibernate.validator.constraints.UUID;

@Getter
public class GetRoomRequest {
    @NotBlank
    @UUID
    private String uuid;
}
