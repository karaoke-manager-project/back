package com.karaoke.song;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import com.karaoke.Util;
import com.karaoke.room.RoomService;

import java.util.List;

@RestController
@RequestMapping("/queue/room")
public class SongController {
  private final SimpMessagingTemplate messagingTemplate;
  private final SongService service;
  private final RoomService roomService;

  public SongController(SimpMessagingTemplate messagingTemplate, SongService service, RoomService roomService) {
    this.messagingTemplate = messagingTemplate;
    this.service = service;
    this.roomService = roomService;
  }

  @PostMapping("/{roomId}/queue")
  @Operation(summary = "Adicionar música à fila de uma sala")
  @SecurityRequirement(name = "bearerAuth")
  public SongResponse addSong(
      @PathVariable String roomId,
      @RequestBody SongRequest request,
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {

    String userId = Util.extractToken(authHeader);
    SongResponse response = service.addSong(request, roomId, userId).toResponse();

    messagingTemplate.convertAndSend(
        "/topic/queue/room/" + roomId,
        service.getSongsQueue(roomId));

    return response;
  }

  @DeleteMapping("/{roomId}/pass")
  @Operation(summary = "Passe a música da fila e receba o link para tocar a música")
  @SecurityRequirement(name = "bearerAuth")
  public String passSong(
      @PathVariable String roomId,
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {

    String managerId = Util.extractToken(authHeader);
    roomService.authorize(roomId, managerId);
    String result = service.passToNextSong(roomId);

    messagingTemplate.convertAndSend(
        "/topic/queue/room/" + roomId,
        service.getSongsQueue(roomId));

    messagingTemplate.convertAndSend(
        "/topic/queue/room/" + roomId + "/url",
        result);

    messagingTemplate.convertAndSend(
        "/topic/queue/room/" + roomId + "/history",
        result);
    return result;
  }

  @GetMapping("/{roomId}")
  @Operation(summary = "Recebe a fila completa de músicas")
  public List<SongResponse> getQueue(@PathVariable String roomId) {
    return service.getSongsQueue(roomId);
  }

  @GetMapping("/{roomId}/history/{start}/{end}")
  @Operation(summary = "Recebe a lista de últimas músicas tocadas")
  public List<SongResponse> getQueue(
      @PathVariable String roomId,
      @PathVariable int start,
      @PathVariable int end) {
    return service.getSongsHistory(roomId, start, end);
  }

  @DeleteMapping("/{roomId}/{songId}")
  @Operation(summary = "Remove uma música da fila")
  @SecurityRequirement(name = "bearerAuth")
  public void removeSong(
      @PathVariable String roomId,
      @PathVariable String songId,
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {

    String managerId = Util.extractToken(authHeader);
    roomService.authorize(roomId, managerId);
    service.removeSong(roomId, songId);

    messagingTemplate.convertAndSend(
        "/topic/queue/room" + roomId,
        service.getSongsQueue(roomId));
  }

}
