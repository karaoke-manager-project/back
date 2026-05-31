package com.karaoke.user;

import lombok.Getter;

import java.util.UUID;

@Getter
public class User {
  private final String id;
  private String name;

  public User(String name, String id) {
    this.name = name;
    this.id = id;
  }

  public UserResponse toResponse() {
    return new UserResponse(name);
  }
}
