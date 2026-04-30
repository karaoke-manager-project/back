package com.karaoke.user;

import lombok.Getter;

import java.util.UUID;

@Getter
public class User {
    private final String id;
    private String name;
    public User() {
        this.id = UUID.randomUUID().toString();
    }
    public User(String name) {
        this();
        this.name = name;
    }
    public UserResponse toResponse() {
        return new UserResponse(name);
    }
}
