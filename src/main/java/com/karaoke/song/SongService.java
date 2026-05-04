package com.karaoke.song;

import com.karaoke.user.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongService {
    private final RedisTemplate<String, Song> redisTemplate;
    private final RedisTemplate<String, User> redisUserTemplate;
    private final RedisTemplate<String, String> redisStringTemplate;

    public SongService(RedisTemplate<String, Song> redisTemplate, RedisTemplate<String, User> redisUserTemplate, RedisTemplate<String, String> redisStringTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisUserTemplate = redisUserTemplate;
        this.redisStringTemplate = redisStringTemplate;
    }

    public Song addSong(SongRequest request, String roomId) {
        String idQuery = "room:" + roomId + ":songNextId";
        String songId = String.valueOf(redisStringTemplate.opsForValue().increment(idQuery));

        String userQuery = "room:" + roomId + ":user:";
        User user = (User) redisUserTemplate.opsForHash().get(userQuery, request.getUserId());
        if (user == null) throw new RuntimeException("This user doesn't exist");

        Song song = new Song(request.getName(), request.getArtistName(), user, request.getUrl(), songId);
        String queueQuery = "room:" + roomId + ":songs";
        String mapQuery = "room:" + roomId + ":songs:map";
        redisStringTemplate.opsForList().rightPush(queueQuery, songId);
        redisTemplate.opsForHash().put(mapQuery, songId, song);
        return song;
    }

    public String passToNextSong(String roomId) {
        String queueQuery = "room:" + roomId + ":songs";
        String mapQuery = "room:" + roomId + ":songs:map";
        String songId = String.valueOf(redisStringTemplate.opsForList().leftPop(queueQuery));
        if (songId == null) {
            throw new RuntimeException("The song queue is empty");
        }
        Song song = (Song) redisTemplate.opsForHash().get(mapQuery, songId);

        redisTemplate.opsForHash().delete(mapQuery, songId);
        return song.url();
    }

    public List<SongResponse> getSongsQueue(String roomId) {
        String queueQuery = "room:" + roomId + ":songs";
        String mapQuery = "room:" + roomId + ":songs:map";
        List<String> ids = redisStringTemplate.opsForList().range(queueQuery, 0, -1);
        return ids
                .stream()
                .map(id -> (Song) redisTemplate.opsForHash().get(mapQuery, id))
                .map(Song::toResponse)
                .toList();
    }

    public void removeSong(String roomId, String songId) {
        String queueQuery = "room:" + roomId + ":songs";
        String mapQuery = "room:" + roomId + ":songs:map";
        redisStringTemplate.opsForList().remove(queueQuery, 0, songId);
        redisTemplate.opsForHash().delete(mapQuery, songId);
    }
}
