package com.karaoke.room;

import com.karaoke.manager.Manager;
import com.karaoke.manager.ManagerRepository;

import com.karaoke.manager.ManagerType;
import com.karaoke.room.song.Song;
import com.karaoke.room.song.SongRequest;
import com.karaoke.room.song.SongResponse;
import com.karaoke.room.user.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RoomService {

    private final ManagerRepository managerRepository;
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public RoomService(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public String generateCode() {
        String code;
        do {
            code = randomCode(8);
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
        String name = request.getName();
        UUID manager_id = request.getManager_id();
        String password = request.getPassword();
        int max_room_size = request.getMax_room_size();

        Manager manager = managerRepository.findById(manager_id)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        Date lastPayment = manager.getPremium_last_payment();
        ManagerType type = manager.getType();
        Date finishDate = type.getFinishDate(lastPayment);
        Date now = new Date();
        if (now.after(finishDate)) {
            manager.setType(ManagerType.FREE);
        }
        managerRepository.save(manager);

        String code = generateCode();
        Room room = new Room(manager, name, code, password, max_room_size);
        rooms.put(code, room);
        return room;
    }

    public String join(String roomId, JoinRoomRequest request){
        Room room = rooms.get(roomId);
        String password = request.getPassword();
        password = password == null? "" : password;
        if (!password.equals(room.getPassword())) {
            throw new RuntimeException("Invalid Password");
        }
        return room.addUser(request.getName());
    }

//    public Map<String, Room> getAll() {
//        return rooms;
//    }

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
        User user = getUserFromRoom(request.getUser_id(), roomId);
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
}