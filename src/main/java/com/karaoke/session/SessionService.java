package com.karaoke.session;

import com.karaoke.user.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SessionService {
    RedisTemplate<User, String> redisTemplate;
}
