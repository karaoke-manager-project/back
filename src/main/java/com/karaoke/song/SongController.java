package com.karaoke.song;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/queue/room")
public class SongController {
    private final SimpMessagingTemplate messagingTemplate;
    private final SongService service;

    public SongController(SimpMessagingTemplate messagingTemplate, SongService service) {
        this.messagingTemplate = messagingTemplate;
        this.service = service;
    }

    @PostMapping("/{roomId}/queue")
    public SongResponse addSong(@PathVariable String roomId,
                                @RequestBody SongRequest request) {

        SongResponse response = service.addSong(request, roomId).toResponse();

        messagingTemplate.convertAndSend(
                "/topic/queue/room/" + roomId,
                service.getSongsQueue(roomId)
        );

        return response;
    }

    @DeleteMapping("/{roomId}/pass")
    public String passSong(@PathVariable String roomId) {

        String result = service.passToNextSong(roomId);

        messagingTemplate.convertAndSend(
                "/topic/queue/room/" + roomId,
                service.getSongsQueue(roomId)
        );

        return result;
    }

    @GetMapping("/{roomId}")
    public List<SongResponse> getQueue(@PathVariable String roomId) {
        return service.getSongsQueue(roomId);
    }

    @DeleteMapping("/{roomId}/{songId}")
    public void removeSong(@PathVariable String roomId, @PathVariable String songId) {
        service.removeSong(roomId, songId);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/queue",
                service.getSongsQueue(roomId)
        );
    }

}
