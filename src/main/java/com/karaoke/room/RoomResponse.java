package com.karaoke.room;

public record RoomResponse(boolean is_premium,
                           int max_room_size,
                           String name, String password, String code) {}
