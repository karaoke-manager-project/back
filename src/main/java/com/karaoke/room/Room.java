package com.karaoke.room;

import com.karaoke.manager.Manager;
import com.karaoke.manager.ManagerType;
import com.karaoke.room.song.Song;
import com.karaoke.room.user.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

@Getter
public class Room {
    private final boolean is_premium;
    private final int MAX_ROOM_SIZE_FREE_USER = 5;
    private int max_room_size;
    private final String manager_id;
    private final String name;
    private final String password;
    private final String code;
    private final List<User> users = new ArrayList<>();
    private final Queue<Song> songs = new ArrayDeque<>();

    private static long nextSongId = 1;
    public Song addSong(String name, User user, String url) {
        Song song = new Song(name, user, url, String.valueOf(nextSongId++));
        songs.offer(song);
        return song;
    }

    public Song nextSong() {
        return songs.poll();
    }

    public void removeSongById(String id) {
        Song song = songs.stream()
                .filter(s -> s.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("There's no song with the provided Id"));
        songs.remove(song);
    }

    public String addUser(@NotBlank String name){
        User user = new User(name);
        if (users.size() >= max_room_size) {
            throw new RuntimeException("Max room size exceeded for this type of account. Upgrade to Premium to enjoy karaoke limitless.");
        }
        users.add(user);
        return user.getId();
    }

    public void kickUser(String id){
        users.removeIf(user -> user.getId().equals(id));
    }

    public void expandRoom(int new_room_size){
        this.max_room_size = new_room_size;
    }

    public User getUserByUUID(String uuid) {
        return users.stream()
                .filter(u -> u.getId().equals(uuid))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("This user doesnt exist in this room"));
    }

    public boolean anyoneHasThisUUID(String uuid){
        if (manager_id.equals(uuid)) {
            return true;
        }
        return users.stream()
                .anyMatch(item -> item.getId().equals(uuid));
    }
    public Room(Manager manager, String name, String code, String password, int max_room_size) {
        this.name = name;
        this.is_premium = manager.getType() != ManagerType.FREE;
        this.code = code;
        this.password = password;
        this.manager_id = manager.getId().toString();
        this.max_room_size = is_premium?max_room_size:MAX_ROOM_SIZE_FREE_USER;
    }

    public RoomResponse toResponse() {
            return new RoomResponse(is_premium, max_room_size, name, password, code, songs);
    }
}
