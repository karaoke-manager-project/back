package com.karaoke.room;

import com.karaoke.user.UserResponse;

public record EmojiResponse(UserResponse userResponse, String emoji) {
}
