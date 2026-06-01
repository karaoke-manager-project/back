package com.karaoke.room;

import com.karaoke.manager.Manager;
import com.karaoke.manager.ManagerRepository;
import com.karaoke.manager.ManagerService;
import com.karaoke.manager.ManagerType;
import com.karaoke.user.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RoomService {

  private final ManagerService managerService;
  private final RedisTemplate<String, Room> redisTemplate;

  public RoomService(ManagerRepository managerRepository, RedisTemplate<String, Room> redisTemplate) {
    this.managerService = new ManagerService(managerRepository);
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

  public Room create(RoomRequest request, String managerId) {
    if (redisTemplate.opsForHash().hasKey("managers:", managerId)) {
      throw new RuntimeException("Você não pode criar mais de uma sala ao mesmo tempo.");
    }
    Manager manager = managerService.getById(UUID.fromString(managerId));
    manager.validateAccountLevel();
    managerService.save(manager);

    String name = request.getName();
    String code = generateCode();
    String password = emptyBlankString(request.getPassword());
    int max_room_size = request.getMax_room_size();
    int timeout_seconds = request.getTimeout_seconds();

    Room room = new Room(
        managerId,
        manager.getType() != ManagerType.FREE,
        name,
        code,
        password,
        max_room_size,
        timeout_seconds);

    redisTemplate.opsForHash().put("room:", code, room);
    redisTemplate.opsForHash().put("managers:", managerId, room);
    return room;
  }

  public void delete(@NotBlank String roomId, String managerId) {
    redisTemplate.opsForHash().delete("room:", roomId);
    redisTemplate.opsForHash().delete("managers:", managerId);
    redisTemplate.delete("room:" + roomId + ":songs");
    redisTemplate.delete("room:" + roomId + ":songs:map");
    redisTemplate.delete("room:" + roomId + ":songNextId");
    redisTemplate.delete("room:" + roomId + ":user:songadded");

    String userQuery = "room:" + roomId + ":user:";
    redisTemplate
        .opsForHash()
        .keys(userQuery)
        .forEach(k -> redisTemplate.delete("user:" + k + ":room:"));

    redisTemplate.delete(userQuery);
  }

  public Room get(String roomId) {
    Room room = (Room) redisTemplate.opsForHash().get("room:", roomId);
    if (room == null) {
      throw new RuntimeException("Essa sala não existe.");
    }
    return room;
  }

  public void authorize(String roomId, String managerId) {
    if (!this.get(roomId)
        .getManagerId()
        .equals(managerId)) {
      throw new RuntimeException("Você não tem permissão de executar essa operação");
    }
  }

  public Room update(@NotBlank String roomId, RoomRequest request, String managerId) {
    Manager manager = managerService.getById(UUID.fromString(managerId));
    manager.validateAccountLevel();

    Room updatedRoom = new Room(
        managerId,
        manager.getType() != ManagerType.FREE,
        request.getName(),
        roomId,
        emptyBlankString(request.getPassword()),
        request.getMax_room_size(),
        request.getTimeout_seconds());

    redisTemplate.opsForHash().put("room:", roomId, updatedRoom);
    redisTemplate.opsForHash().put("managers:", managerId, updatedRoom);

    return updatedRoom;
  }

  public User join(String roomId, JoinRoomRequest request) {
    Room room = get(roomId);

    String password = emptyBlankString(request.getPassword());
    if (!password.equals(room.getPassword()))
      throw new RuntimeException("Senha inválida.");

    User user = new User(request.getName(), UUID.randomUUID().toString());
    String userQuery = "room:" + roomId + ":user:";
    if (redisTemplate.opsForHash().size(userQuery) > room.getMaxRoomSize()) {
      throw new RuntimeException(
          "A sala já alcançou o limite de usuário simultâneos! Peça para o gerente da sala aumentar o tamanho.");
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
    return s.isBlank() ? "" : s;
  }

  public Room getRoomByUserId(@NotBlank String roomId, @NotBlank String userId) {
    String userReversedQuery = "user:" + userId + ":room:";
    Room room = (Room) redisTemplate.opsForHash().get(userReversedQuery, roomId);
    if (room == null)
      throw new RuntimeException("Você não pode acessar essa sala ou essa sala não existe.");
    return room;
  }

  public Room getRoomByManagerId(@NotBlank String managerId) {
    Room room = (Room) redisTemplate.opsForHash().get("managers:", managerId);
    if (room == null)
      throw new RuntimeException("Esse gerente não tem sala.");
    return room;
  }

  public RoomInfoResponse getRoomInfo(@NotBlank String roomId) {
    Room room = (Room) redisTemplate.opsForHash().get("room:", roomId);
    if (room == null)
      throw new RuntimeException("Essa sala não existe.");

    return new RoomInfoResponse(room.getName(), !room.getPassword().isBlank());
  }

}
