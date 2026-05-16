package com.karaoke.room;

import com.karaoke.manager.Manager;
import com.karaoke.manager.ManagerRepository;

import com.karaoke.manager.ManagerType;
import com.karaoke.user.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RoomService {

    private final ManagerRepository managerRepository;
    private final RedisTemplate<String, Room> redisTemplate;
    public RoomService(ManagerRepository managerRepository, RedisTemplate<String, Room> redisTemplate) {
        this.managerRepository = managerRepository;
        this.redisTemplate = redisTemplate;
    }

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public String generateCode() {
        String code;
        do {
            int CODE_LENGTH = 8;
            code = randomCode(CODE_LENGTH);
        } while (redisTemplate.opsForHash().hasKey("room:", code));
        return code;
    }

    private String randomCode(int length) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(LETTERS.length());
            sb.append(LETTERS.charAt(index));
        }

        return sb.toString();
    }

    public Room create(RoomRequest request){
        String manager_id = request.getManager_id();
        if (redisTemplate.opsForHash().hasKey("managers:", manager_id)) {
            throw new RuntimeException("Same manager can't create more than one room at a time");
        }
        Manager manager =
                managerRepository
                .findById(UUID.fromString(manager_id))
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        manager.validateAccountLevel();
        managerRepository.save(manager);

        String name = request.getName();
        String code = generateCode();
        String password = emptyBlankString(request.getPassword());
        int max_room_size = request.getMax_room_size();

        Room room = new Room(manager_id, manager.getType() != ManagerType.FREE, name, code, password, max_room_size);
        redisTemplate.opsForHash().put("room:", code, room);
        redisTemplate.opsForHash().put("managers:", manager_id, room);
        return room;
    }

    public void delete(@NotBlank String roomId){
        Room room = (Room) redisTemplate.opsForHash().get("room:", roomId);
        if (room == null) throw new RuntimeException("This room doesn't exist");

        redisTemplate.opsForHash().delete("room:", roomId);
        redisTemplate.opsForHash().delete("managers:", room.getManagerId());
        redisTemplate.delete("room:" + roomId + ":songs");
        redisTemplate.delete("room:" + roomId + ":songs:map");

        String userQuery = "room:" + roomId + ":user:";
        redisTemplate
                .opsForHash()
                .keys(userQuery)
                .forEach(k -> redisTemplate.delete("user:" + k + ":room:"));

        redisTemplate.delete(userQuery);
    }

    public User join(String roomId, JoinRoomRequest request) {
        Room room = (Room) redisTemplate.opsForHash().get("room:", roomId);
        if (room == null) throw new RuntimeException("This room doesn't exist");

        String password = emptyBlankString(request.getPassword());
        if (!password.equals(room.getPassword())) throw new RuntimeException("Invalid Password");

        User user = new User(request.getName());
        String userQuery = "room:" + roomId + ":user:";
        if (redisTemplate.opsForHash().size(userQuery) > room.getMaxRoomSize()) {
            throw new RuntimeException("Max room size exceeded for this type of account. Upgrade to Premium to enjoy karaoke limitless.");
        }

        String userId = user.getId();
        redisTemplate.opsForHash().put(userQuery, userId, user);
        String userReversedQuery = "user:" + userId + ":room:";
        redisTemplate.opsForHash().put(userReversedQuery, roomId, room);
        return user;
    }

    public String emptyBlankString(String s) {
        if (s == null) {
            return "";
        }
        return s.isBlank()? "" : s;
    }


    public Room getRoomByUUID(@NotBlank String roomId, @NotBlank String uuid) {
        Room room = (Room) redisTemplate.opsForHash().get("managers:", uuid);
        if (room != null) return room;
        String userReversedQuery = "user:" + uuid + ":room:";
        room = (Room) redisTemplate.opsForHash().get(userReversedQuery, roomId);
        if (room == null) throw new RuntimeException("You cannot access this room or this room does not existi!");
        return room;
    }

    public Room getRoomByManager(@NotBlank String managerId) {
        Room room = (Room) redisTemplate.opsForHash().get("managers:", managerId);
        if (room == null) throw new RuntimeException("This manager has no room");
        return room;
    }

    public RoomInfoResponse getRoomInfo(@NotBlank String roomId) {
        Room room = (Room) redisTemplate.opsForHash().get("room:", roomId);
        if (room == null) throw new RuntimeException("This room doesn't exist");

        return new RoomInfoResponse(room.getName(), !room.getPassword().isBlank());
    }

}