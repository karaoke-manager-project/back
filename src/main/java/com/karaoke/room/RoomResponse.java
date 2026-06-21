package com.karaoke.room;

public record RoomResponse(Boolean is_premium,
                           int max_room_size,
                           String name, String password, String code, int timeoutSeconds) {}
