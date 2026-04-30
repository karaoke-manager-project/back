package com.karaoke.room;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Room {
    private Boolean isPremium;
    private final int MAX_ROOM_SIZE_FREE_USER = 5;
    private int maxRoomSize;
    private final String managerId;
    private final String name;
    private final String password;
    private final String code;

    public Room(String managerId, Boolean isPremium, String name, String code, String password, int maxRoomSize) {
        this.name = name;
        this.isPremium = isPremium;
        this.code = code;
        this.password = password;
        this.managerId = managerId;
        if (!isPremium && maxRoomSize > MAX_ROOM_SIZE_FREE_USER) {
            throw new RuntimeException("Your plan don't cover a room that big, upgrade your plan for unlimited sized rooms");
        }
        this.maxRoomSize = (maxRoomSize <= 0)? 1: maxRoomSize;
    }

    public RoomResponse toResponse() {
            return new RoomResponse(isPremium, maxRoomSize, name, password, code);
    }
}
