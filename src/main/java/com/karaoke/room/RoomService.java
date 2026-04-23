package com.karaoke.room;

import com.karaoke.manager.Manager;
import com.karaoke.manager.ManagerType;

public class RoomService {
    ManagerType roomType;
    public RoomService() {}
    public RoomService(Manager manager) {
        roomType = manager.getType();
    }
}
