package com.karaoke.room;

import com.karaoke.room.song.SongRequest;
import com.karaoke.room.song.SongResponse;
import com.karaoke.room.user.User;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

@RestController
@RequestMapping("/room")
public class RoomController {
    private final RoomService service;
    private final SimpMessagingTemplate messagingTemplate;

    public RoomController(RoomService service, SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
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
    public RoomResponse getById(
            @PathVariable String roomId,
            @RequestBody GetRoomRequest request
    ) {
        return service.getRoomByUUID(roomId, request.getUuid()).toResponse();
    }

    @GetMapping("/{roomId}/{userId}")
    public User getUser(@PathVariable String roomId,
                        @PathVariable String userId) {
        return service.getUserFromRoom(userId, roomId);
    }

    @PostMapping("/{roomId}/queue")
    public SongResponse addSong(@PathVariable String roomId,
                                @RequestBody SongRequest request) {

        SongResponse response = service.addSongToRoomQueue(request, roomId);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/queue",
                service.getSongsQueue(roomId)
        );

        return response;
    }

    @DeleteMapping("/{roomId}/queue")
    public String passSong(@PathVariable String roomId) {

        String result = service.passToNextSong(roomId);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/queue",
                service.getSongsQueue(roomId)
        );

        return result;
    }

    @GetMapping("/{roomId}/queue")
    public List<SongResponse> getQueue(@PathVariable String roomId) {
        return service.getSongsQueue(roomId);
    }

    @DeleteMapping("/{roomId}/queue/{songId}")
    public void removeSong(@PathVariable String roomId, @PathVariable String songId) {
        service.removeSong(roomId, songId);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/queue",
                service.getSongsQueue(roomId)
        );
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