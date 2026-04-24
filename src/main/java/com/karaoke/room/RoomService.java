package com.karaoke.room;

import com.karaoke.manager.Manager;
import com.karaoke.manager.ManagerRepository;

import org.springframework.stereotype.Service;

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

        if (name == null || manager_id == null){
            throw new RuntimeException("Invalid entry");
        }

        Manager manager = managerRepository.findById(manager_id)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        String code = generateCode();
        Room room = new Room(manager, name, code, password);
        rooms.put(code, room);
        return room;
    }

    public String join(String roomId, JoinRoomRequest request){
        Room room = rooms.get(roomId);
        if (room.getPassword() != null && !request.getPassword().equals(room.getPassword())) {
            throw new RuntimeException("Invalid Password");
        }
        return room.addUser();
    }

    public Map<String, Room> getAll() {
        return rooms;
    }
}