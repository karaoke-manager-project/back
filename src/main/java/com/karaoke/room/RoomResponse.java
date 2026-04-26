package com.karaoke.room;

import com.karaoke.room.song.Song;

import java.util.Queue;

public record RoomResponse(boolean is_premium,
                           int max_room_size,
                           String name, String password, String code, Queue<Song> songs) {}
