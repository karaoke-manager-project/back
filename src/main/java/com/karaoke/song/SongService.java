package com.karaoke.song;

import com.karaoke.room.Room;
import com.karaoke.user.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static long nextSongId = 1;

    public SongService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Song addSong(SongRequest request, String roomId) {
        String id = String.valueOf(nextSongId++);
        Room room = (Room) redisTemplate.opsForHash().get("room:", roomId);
        User user = room.getUserByUUID(request.getUserId());
        Song song = new Song(request.getName(), request.getArtistName(), user, request.getUrl(), id);
        String queueQuery = "room:" + roomId + ":songs";
        String mapQuery = "room:" + roomId + ":songs:map";
        redisTemplate.opsForList().rightPush(queueQuery, id);
        redisTemplate.opsForHash().put(mapQuery, id, song);
        return song;
    }

    public String passToNextSong(String roomId) {
        String queueQuery = "room:" + roomId + ":songs";
        String mapQuery = "room:" + roomId + ":songs:map";
        String songId = (String) redisTemplate.opsForList().leftPop(queueQuery);
        Song song = (Song) redisTemplate.opsForHash().get(mapQuery, songId);
        if (song == null) {
            throw new RuntimeException("The song queue is empty");
        }
        return song.url();
    }

    public List<SongResponse> getSongsQueue(String roomId) {
        String queueQuery = "room:" + roomId + ":songs";
        String mapQuery = "room:" + roomId + ":songs:map";
        List<Object> ids = redisTemplate.opsForList().range(queueQuery, 0, -1);
        return ids
                .stream()
                .map(id -> redisTemplate.opsForHash().get(mapQuery, id))
                .map(song -> ((Song) song).toResponse())
                .toList();
    }

    public void removeSong(String roomId, String songId) {
        String queueQuery = "room:" + roomId + ":songs";
        String mapQuery = "room:" + roomId + ":songs:map";
        redisTemplate.opsForList().remove(queueQuery, 0, songId);
        redisTemplate.opsForHash().delete(mapQuery, songId);
    }
}
