package com.karaoke.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RoomRequest {
  @NotNull
  private int max_room_size;

  @NotBlank
  @Size(max = 255)
  private String name;

  @Size(max = 255)
  private String password;

  private int timeout_seconds;
}
