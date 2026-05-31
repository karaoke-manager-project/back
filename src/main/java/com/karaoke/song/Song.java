package com.karaoke.song;

import com.karaoke.user.User;

public record Song(String name, String artistName, User user, String url, String id) {
  public SongResponse toResponse() {
    return new SongResponse(name, artistName, user.toResponse(), url, id);
  }
}
