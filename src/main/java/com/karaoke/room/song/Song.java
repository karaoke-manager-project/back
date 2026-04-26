package com.karaoke.room.song;

import com.karaoke.room.user.User;

public record Song(String name, User user, String url, String id) {
    public SongResponse toResponse() {
        return new SongResponse(name, user.toResponse(), url, id);
    }
}
