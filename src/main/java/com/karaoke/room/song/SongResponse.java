package com.karaoke.room.song;

import com.karaoke.room.user.UserResponse;

public record SongResponse(String name, UserResponse user, String url, String id) {}
