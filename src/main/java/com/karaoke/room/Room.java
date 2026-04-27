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
    private final boolean isPremium;
    private final int MAX_ROOM_SIZE_FREE_USER = 5;
    private int maxRoomSize;
    private final String managerId;
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
        if (users.size() >= maxRoomSize) {
            throw new RuntimeException("Max room size exceeded for this type of account. Upgrade to Premium to enjoy karaoke limitless.");
        }
        users.add(user);
        return user.getId();
    }

    public void kickUser(String id){
        users.removeIf(user -> user.getId().equals(id));
    }

    public void expandRoom(int new_room_size){
        this.maxRoomSize = new_room_size;
    }

    public User getUserByUUID(String uuid) {
        return users.stream()
                .filter(u -> u.getId().equals(uuid))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("This user doesnt exist in this room"));
    }

    public boolean anyoneHasThisUUID(String uuid){
        if (managerId.equals(uuid)) {
            return true;
        }
        return users.stream()
                .anyMatch(item -> item.getId().equals(uuid));
    }
    public Room(Manager manager, String name, String code, String password, int maxRoomSize) {
        this.name = name;
        this.isPremium = manager.getType() != ManagerType.FREE;
        this.code = code;
        this.password = password;
        this.managerId = manager.getId().toString();
        if (!isPremium && maxRoomSize > MAX_ROOM_SIZE_FREE_USER) {
            throw new RuntimeException("Your plan don't cover a room that big, upgrade your plan for unlimited sized rooms");
        }
        this.maxRoomSize = maxRoomSize;
    }

    public RoomResponse toResponse() {
            return new RoomResponse(isPremium, maxRoomSize, name, password, code, songs);
    }
}
