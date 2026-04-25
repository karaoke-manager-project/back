package com.karaoke.room;

public class RoomResponse {
    public boolean is_premium;
    public int max_room_size;
    public String name;
    public String password;
    public String code;

    public RoomResponse(boolean is_premium, int max_room_size, String name, String password, String code) {
        this.is_premium = is_premium;
        this.max_room_size = max_room_size;
        this.name = name;
        this.password = password;
        this.code = code;
    }
}
