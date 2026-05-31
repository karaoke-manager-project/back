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

  public Room create(RoomRequest request) {
    String manager_id = request.getManager_id();
    if (redisTemplate.opsForHash().hasKey("managers:", manager_id)) {
      throw new RuntimeException("Você não pode criar mais de uma sala ao mesmo tempo.");
    }
    Manager manager = managerRepository
        .findById(UUID.fromString(manager_id))
        .orElseThrow(() -> new RuntimeException("Gerente não encontrado."));
    manager.validateAccountLevel();
    managerRepository.save(manager);

    String name = request.getName();
    String code = generateCode();
    String password = emptyBlankString(request.getPassword());
    int max_room_size = request.getMax_room_size();
    int timeout_seconds = request.getTimeout_seconds();

    Room room = new Room(
        manager_id,
        manager.getType() != ManagerType.FREE,
        name,
        code,
        password,
        max_room_size,
        timeout_seconds);

    redisTemplate.opsForHash().put("room:", code, room);
    redisTemplate.opsForHash().put("managers:", manager_id, room);
    return room;
  }

  public void delete(@NotBlank String roomId) {
    Room room = (Room) redisTemplate.opsForHash().get("room:", roomId);
    if (room == null)
      throw new RuntimeException("Essa sala não existe.");

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

  public Room update(@NotBlank String roomId, RoomRequest request) {
    Room room = (Room) redisTemplate.opsForHash().get("room:", roomId);
    if (room == null) {
      throw new RuntimeException("Essa sala não existe.");
    }

    String manager_id = request.getManager_id();
    if (!room.getManagerId().equals(manager_id)) {
      throw new RuntimeException("Você não tem permissão de atualizar essa sala.");
    }
    Manager manager = managerRepository
        .findById(UUID.fromString(manager_id))
        .orElseThrow(() -> new RuntimeException("Gerente não encontrado."));
    manager.validateAccountLevel();

    Room updatedRoom = new Room(
        room.getManagerId(),
        manager.getType() != ManagerType.FREE,
        request.getName(),
        room.getCode(),
        emptyBlankString(request.getPassword()),
        request.getMax_room_size(),
        request.getTimeout_seconds());

    redisTemplate.opsForHash().put("room:", roomId, updatedRoom);
    redisTemplate.opsForHash().put("managers:", room.getManagerId(), updatedRoom);

    return updatedRoom;
  }

  public User join(String roomId, JoinRoomRequest request) {
    Room room = (Room) redisTemplate.opsForHash().get("room:", roomId);
    if (room == null)
      throw new RuntimeException("Essa sala não existe.");

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

  public Room getRoomByUUID(@NotBlank String roomId, @NotBlank String uuid) {
    Room room = (Room) redisTemplate.opsForHash().get("managers:", uuid);
    if (room != null)
      return room;
    String userReversedQuery = "user:" + uuid + ":room:";
    room = (Room) redisTemplate.opsForHash().get(userReversedQuery, roomId);
    if (room == null)
      throw new RuntimeException("Você não pode acessar essa sala ou essa sala não existe.");
    return room;
  }

  public Room getRoomByManager(@NotBlank String managerId) {
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
