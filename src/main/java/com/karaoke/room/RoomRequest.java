package com.karaoke.room;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RoomRequest {
    @NotBlank
    private UUID manager_id;
    private int max_room_size;
    @NotBlank
    private String name;
    private String password;
}
