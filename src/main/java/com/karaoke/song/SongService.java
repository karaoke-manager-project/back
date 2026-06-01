package com.karaoke.song;

import com.karaoke.user.User;
import com.karaoke.user.UserService;
import com.karaoke.room.Room;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SongService {
  private final RedisTemplate<String, Song> redisTemplate;
  private final UserService userService;
  private final RedisTemplate<String, String> redisStringTemplate;
  private final RedisTemplate<String, Room> redisRoomTemplate;

  public SongService(RedisTemplate<String, Song> redisTemplate, RedisTemplate<String, User> redisUserTemplate,
      RedisTemplate<String, String> redisStringTemplate, RedisTemplate<String, Room> redisRoomTemplate) {
    this.redisTemplate = redisTemplate;
    this.userService = new UserService(redisUserTemplate);
    this.redisStringTemplate = redisStringTemplate;
    this.redisRoomTemplate = redisRoomTemplate;
  }

  public Song addSong(SongRequest request, String roomId, String userId) {
    User user = userService.get(roomId, userId);

    String idQuery = "room:" + roomId + ":songNextId";
    String songId = String.valueOf(redisStringTemplate.opsForValue().increment(idQuery));

    long remainingTime = getRemainingTime(userId, roomId);
    if (remainingTime > 0) {
      throw new RuntimeException(
          "Calma! Você ainda não pode criar uma música, espere %d segundos".formatted(remainingTime));
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

  private int getTimeoutByRoom(String roomId) {
    Room room = (Room) redisRoomTemplate
        .opsForHash()
        .get("room:", roomId);

    return room.getTimeoutSeconds();
  }

  private String getTimeLastAddedUserSong(String userId, String roomId) {
    String key = "room:" + roomId + ":user:songadded";
    return (String) redisStringTemplate
        .opsForHash()
        .get(key, userId);
  }

  public long getRemainingTime(String userId, String roomId) {
    String lastAdded = getTimeLastAddedUserSong(userId, roomId);
    if (lastAdded == null) {
      return 0;
    }
    int timeoutSeconds = getTimeoutByRoom(roomId);

    if (timeoutSeconds == 0) {
      return 0;
    }

    long now = System.currentTimeMillis();
    long last = Long.valueOf(lastAdded);
    long diff = now - last;
    long timeoutMillis = timeoutSeconds * 1000L;
    return (timeoutMillis - diff) / 1000L;
  }

  public String passToNextSong(String roomId) {
    String queueQuery = "room:" + roomId + ":songs";
    String mapQuery = "room:" + roomId + ":songs:map";
    String songId = String.valueOf(redisStringTemplate.opsForList().leftPop(queueQuery));
    if (songId == null) {
      throw new RuntimeException("A fila de músicas está vazia!");
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
