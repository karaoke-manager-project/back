package com.karaoke.room;

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

    @PostMapping("/{roomId}/join")
    public String join(
            @PathVariable String roomId,
            @RequestBody JoinRoomRequest request
    ) {
        return service.join(roomId, request).getId();
    }
    @DeleteMapping("/{roomId}")
    public void delete(@PathVariable String roomId) {
        service.delete(roomId);
    }

    @GetMapping("/{roomId}/info")
    public RoomInfoResponse getRoomInfo(@PathVariable String roomId) {
        return service.getRoomInfo(roomId);
    }

 // Temporário, para facilitar integração do frontend
    @GetMapping("/{roomId}/{userOrManagerId}/auth")
    public RoomResponse getById(
            @PathVariable String roomId,
            @PathVariable String userOrManagerId
    ) {
        return service.getRoomByUUID(roomId, userOrManagerId).toResponse();
    }

}