package com.karaoke.user;

import lombok.Getter;

import java.util.UUID;

@Getter
public class User {
    private final String id;
    public User() {
        this.id = UUID.randomUUID().toString();
    }
}
