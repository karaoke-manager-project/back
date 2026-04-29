package com.karaoke.room;

import com.karaoke.manager.Manager;
import com.karaoke.manager.ManagerRepository;

import com.karaoke.room.song.Song;
import com.karaoke.room.song.SongRequest;
import com.karaoke.room.song.SongResponse;
import com.karaoke.room.user.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RoomService {

    private final ManagerRepository managerRepository;
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final HashSet<String> managers = new HashSet<>();

    public RoomService(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public String generateCode() {
        String code;
        do {
            int CODE_LENGTH = 8;
            code = randomCode(CODE_LENGTH);
        } while (rooms.containsKey(code));
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
        if (managers.contains(manager_id)) {
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
        rooms.put(code, room);
        managers.add(manager_id);
        return room;
    }

    public void delete(String roomId){
        rooms.remove(roomId);
    }

    public String emptyBlankString(String s) {
        return s.isBlank()? "" : s;
    }
    public String join(String roomId, JoinRoomRequest request){
        Room room = rooms.get(roomId);
        String password = emptyBlankString(request.getPassword());;
        if (!password.equals(room.getPassword())) {
            throw new RuntimeException("Invalid Password");
        }
        return room.addUser(request.getName());
    }

    public Room getRoomByUUID(String roomId, @NotBlank String uuid) {
       Room room = rooms.get(roomId);
       if (room.anyoneHasThisUUID(uuid)) return room;
       throw new RuntimeException("You cannot acess this room!");
    }

    public User getUserFromRoom(String userId, String roomId) {
        Room room = rooms.get(roomId);
        return room.getUserByUUID(userId);
    }

    public SongResponse addSongToRoomQueue(SongRequest request, String roomId) {
        User user = getUserFromRoom(request.getUserId(), roomId);
        Room room = rooms.get(roomId);
        return room.addSong(request.getName(), user, request.getUrl()).toResponse();
    }

    public String passToNextSong(String roomId) {
        Room room = rooms.get(roomId);
        return room.nextSong().url();
    }

    public List<SongResponse> getSongsQueue(String roomId) {
        Room room = rooms.get(roomId);
        return room
                .getSongs()
                .stream()
                .map(Song::toResponse)
                .toList();
    }

    public void removeSong(String roomId, String songId) {
        Room room = rooms.get(roomId);
        room.removeSongById(songId);
    }

    public void kickUser(String roomId, String userId) {
        Room room = rooms.get(roomId);
        room.kickUser(userId);
    }

    public RoomInfoResponse getRoomInfo(String roomId) {
        Room room = rooms.get(roomId);
        return new RoomInfoResponse(room.getName(), !room.getPassword().isBlank());
    }
}