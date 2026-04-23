package com.karaoke.room;

import com.karaoke.manager.Manager;
import com.karaoke.manager.ManagerType;

import java.util.UUID;

public class Room {
    private boolean is_premium;
    private String name;
    private String password;
    private UUID id;

    public Room() {}

    public Room(Manager manager, String name) {
        this.name = name;
        this.is_premium = manager.getType() != ManagerType.FREE;
        this.id = UUID.randomUUID();
    }
    public Room(Manager manager, String name, String password) {
        this(manager, name);
        this.password = password;
    }
}
