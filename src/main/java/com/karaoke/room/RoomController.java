package com.karaoke.room;

import com.karaoke.user.User;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/room")
public class RoomController {

    private final RoomService service;

    public RoomController(RoomService service) {
        this.service = service;
    }

    @PostMapping
    public Room create(@Valid @RequestBody RoomRequest request) {
        return service.create(request);
    }
    @PostMapping("/{roomId}/join")
    public String join(
            @PathVariable String roomId,
            @RequestBody JoinRoomRequest request
    ) {
        return service.join(roomId, request);
    }
    @GetMapping("/{roomId}")
    public Room getById(
            @PathVariable String roomId,
            @RequestBody GetRoomRequest request
    ) {
        return service.getRoomByUUID(roomId, request.getUuid());
    }

    @GetMapping("/{roomId}/{userId}")
    public User getUser(@PathVariable String roomId,
                        @PathVariable String userId) {
        return service.getUserFromRoom(userId, roomId);
    }
//    @GetMapping
//    public Map<String, Room> getAll() {
//        return service.getAll();
//    }
}