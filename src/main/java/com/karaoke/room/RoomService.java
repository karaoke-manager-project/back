package com.karaoke.room;

import com.karaoke.manager.Manager;
import com.karaoke.manager.ManagerRepository;

import com.karaoke.song.Song;
import com.karaoke.song.SongRequest;
import com.karaoke.song.SongResponse;
import com.karaoke.song.SongService;
import com.karaoke.user.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RoomService {

    private final ManagerRepository managerRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    public RoomService(ManagerRepository managerRepository, RedisTemplate<String, Object> redisTemplate) {
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
        if (redisTemplate.hasKey("managers:" + manager_id)) {
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

        Room room = new Room(manager, name, code, password, max_room_size);
        redisTemplate.opsForHash().put("room:", code, room);
        String managersKey = "managers:" + manager_id;
        redisTemplate.opsForSet().add(managersKey, manager_id);
        return room;
    }

    public void delete(String roomId){
        Room room = (Room) redisTemplate.opsForHash().get("room:", roomId);
        if (room == null) {
            throw new RuntimeException("This room doesn't exist");
        }
        redisTemplate.opsForHash().delete("room:", roomId);
        redisTemplate.delete("managers:" + room.getManagerId());
    }

    public String emptyBlankString(String s) {
        return s.isBlank()? "" : s;
    }
    public String join(String roomId, JoinRoomRequest request){
        Room room = (Room) redisTemplate.opsForHash().get("room:", roomId);
        if (room == null) {
            throw new RuntimeException("This room doesn't exist");
        }
        String password = emptyBlankString(request.getPassword());;
        if (!password.equals(room.getPassword())) {
            throw new RuntimeException("Invalid Password");
        }
        return room.addUser(request.getName());
    }

    public Room getRoomByUUID(String roomId, @NotBlank String uuid) {
        Room room = (Room) redisTemplate.opsForHash().get("room:", roomId);
        if (room == null) {
            throw new RuntimeException("This room doesn't exist");
        }
       if (room.anyoneHasThisUUID(uuid)) return room;
       throw new RuntimeException("You cannot acess this room!");
    }

    public User getUserFromRoom(String userId, String roomId) {
        Room room = (Room) redisTemplate.opsForHash().get("room:", roomId);
        if (room == null) {
            throw new RuntimeException("This room doesn't exist");
        }
        return room.getUserByUUID(userId);
    }

    public void kickUser(String roomId, String userId) {
        Room room = (Room) redisTemplate.opsForHash().get("room:", roomId);
        if (room == null) {
            throw new RuntimeException("This room doesn't exist");
        }
        room.kickUser(userId);
    }

    public RoomInfoResponse getRoomInfo(String roomId) {
        Room room = (Room) redisTemplate.opsForHash().get("room:", roomId);
        if (room == null) {
            throw new RuntimeException("This room doesn't exist");
        }
        return new RoomInfoResponse(room.getName(), !room.getPassword().isBlank());
    }
}