package com.karaoke.song;

import com.karaoke.user.User;
import com.karaoke.room.Room;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SongService {
  private final RedisTemplate<String, Song> redisTemplate;
  private final RedisTemplate<String, User> redisUserTemplate;
  private final RedisTemplate<String, String> redisStringTemplate;
  private final RedisTemplate<String, Room> redisRoomTemplate;

  public SongService(RedisTemplate<String, Song> redisTemplate, RedisTemplate<String, User> redisUserTemplate,
      RedisTemplate<String, String> redisStringTemplate, RedisTemplate<String, Room> redisRoomTemplate) {
    this.redisTemplate = redisTemplate;
    this.redisUserTemplate = redisUserTemplate;
    this.redisStringTemplate = redisStringTemplate;
    this.redisRoomTemplate = redisRoomTemplate;
  }

  public Song addSong(SongRequest request, String roomId) {
    String idQuery = "room:" + roomId + ":songNextId";
    String songId = String.valueOf(redisStringTemplate.opsForValue().increment(idQuery));

    String userQuery = "room:" + roomId + ":user:";
    User user = (User) redisUserTemplate.opsForHash().get(userQuery, request.getUserId());

    if (user == null)
      throw new RuntimeException("This user doesn't exist");

    String userId = user.getId();
    System.out.println(userId);
    if (!validTimeout(userId, roomId)) {
      throw new RuntimeException("Wait! You can't create a song yet");
    }

    Song song = new Song(request.getName(), request.getArtistName(), user, request.getUrl(), songId);

    String lastTimeSongAddedQuery = "room:" + roomId + ":user:songadded";
    Long now = System.currentTimeMillis();
    redisStringTemplate.opsForHash().put(lastTimeSongAddedQuery, userId, now.toString());

    String queueQuery = "room:" + roomId + ":songs";
    String mapQuery = "room:" + roomId + ":songs:map";
    redisStringTemplate.opsForList().rightPush(queueQuery, songId);
    redisTemplate.opsForHash().put(mapQuery, songId, song);
    return song;
  }

  public boolean validTimeout(String userId, String roomId) {
    String key = "room:" + roomId + ":user:songadded";

    String lastAdded = (String) redisStringTemplate
        .opsForHash()
        .get(key, userId);

    System.out.println(lastAdded);
    if (lastAdded == null) {
      return true;
    }

    Room room = (Room) redisRoomTemplate
        .opsForHash()
        .get("room:", roomId);

    int timeoutSeconds = room.getTimeoutSeconds();
    if (timeoutSeconds == 0) {
      return true;
    }

    long now = System.currentTimeMillis();
    long last = Long.valueOf(lastAdded);
    long diff = now - last;
    long timeoutMillis = timeoutSeconds * 1000L;
    if (diff < timeoutMillis) {
      return false;
    }

    return true;
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
