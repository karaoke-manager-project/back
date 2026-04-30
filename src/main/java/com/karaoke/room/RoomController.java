package com.karaoke.room;

import com.karaoke.user.User;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/{roomId}")
    public void delete(@PathVariable String roomId) {
        service.delete(roomId);
    }
    @GetMapping("/{roomId}/info")
    public RoomInfoResponse getRoomInfo(@PathVariable String roomId) {
        return service.getRoomInfo(roomId);
    }
    @PostMapping("/{roomId}/join")
    public String join(
            @PathVariable String roomId,
            @RequestBody JoinRoomRequest request
    ) {
        return service.join(roomId, request);
    }
 // Temporário, para facilitar integração do frontend
    @GetMapping("/{roomId}/{userOrManagerUuid}/auth")
    public RoomResponse getById(
            @PathVariable String roomId,
            @PathVariable String userOrManagerUuid
    ) {
        return service.getRoomByUUID(roomId, userOrManagerUuid).toResponse();
    }

    @GetMapping("/{roomId}/{userId}")
    public User getUser(@PathVariable String roomId,
                        @PathVariable String userId) {
        return service.getUserFromRoom(userId, roomId);
    }

    @DeleteMapping("/{roomId}/{userId}")
    public void kickUser(@PathVariable String roomId, @PathVariable String userId) {
        service.kickUser(roomId, userId);
    }
//    @GetMapping
//    public Map<String, Room> getAll() {
//        return service.getAll();
//    }
}