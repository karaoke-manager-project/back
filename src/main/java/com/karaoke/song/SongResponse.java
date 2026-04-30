package com.karaoke.song;

import com.karaoke.user.UserResponse;

public record SongResponse(String name, String artistName, UserResponse user, String url, String id) {}
