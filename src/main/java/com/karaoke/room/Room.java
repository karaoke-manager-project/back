package com.karaoke.room;

import com.karaoke.manager.Manager;
import com.karaoke.manager.ManagerType;
import com.karaoke.user.User;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Room {
    private final boolean is_premium;
    private final Long MAX_ROOM_SIZE_FREE_USER = 5L;
    private final String name;
    private final String password;
    private final String code;
    private final List<User> users = new ArrayList<>();

    public String addUser(){
        User user = new User();
        if (!is_premium && users.size() >= MAX_ROOM_SIZE_FREE_USER) {
            throw new RuntimeException("Max room size exceeded for this type of account. Upgrade to Premium to enjoy karaoke limitless.");
        }
        users.add(user);
        return user.getId();
    }

    public void kickUser(String id){
        users.removeIf(user -> user.getId().equals(id));
    }

    public Room(Manager manager, String name, String code, String password) {
        this.name = name;
        this.is_premium = manager.getType() != ManagerType.FREE;
        this.code = code;
        this.password = password;
    }
}
