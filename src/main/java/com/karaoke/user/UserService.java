package com.karaoke.user;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final RedisTemplate<String, User> redisTemplate;

    public UserService(RedisTemplate<String, User> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void kickUser(String roomId, String userId) {
        String userQuery = "room:" + roomId + ":user:";
        redisTemplate.opsForHash().delete(userQuery, userId);
    }

    public User get(String roomId, String userId) {
        String userQuery = "room:" + roomId + ":user:";
        User user = (User) redisTemplate.opsForHash().get(userQuery, userId);
        if (user == null) throw new RuntimeException("This user doesn't exist");
        return user;
    }

    public List<UserResponse> getAll(String roomId) {
        String userQuery = "room:" + roomId + ":user:";
        return redisTemplate
                .opsForHash()
                .values(userQuery)
                .stream()
                .map(user -> ((User) user).toResponse())
                .toList();
    }
}
