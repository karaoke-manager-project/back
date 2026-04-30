package com.karaoke.config;

import com.karaoke.room.Room;
import com.karaoke.song.Song;
import com.karaoke.user.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Room> roomRedisTemplate(RedisConnectionFactory factory) {

        RedisTemplate<String, Room> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        ObjectMapper mapper = new ObjectMapper();

        JacksonJsonRedisSerializer<Room> serializer =
                new JacksonJsonRedisSerializer<>(mapper, Room.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.setDefaultSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, Song> songRedisTemplate(RedisConnectionFactory factory) {

        RedisTemplate<String, Song> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        ObjectMapper mapper = new ObjectMapper();

        JacksonJsonRedisSerializer<Song> serializer =
                new JacksonJsonRedisSerializer<>(mapper, Song.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.setDefaultSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, User> userRedisTemplate(RedisConnectionFactory factory) {

        RedisTemplate<String, User> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        ObjectMapper mapper = new ObjectMapper();

        JacksonJsonRedisSerializer<User> serializer =
                new JacksonJsonRedisSerializer<>(mapper, User.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.setDefaultSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}