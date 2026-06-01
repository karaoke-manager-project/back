package com.karaoke.room;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.karaoke.Util;

@RestController
@RequestMapping("/room")
public class RoomController {
  private final RoomService service;

  public RoomController(RoomService service) {
    this.service = service;
  }

  @PostMapping
  @Operation(summary = "Criar Salas")
  @SecurityRequirement(name = "bearerAuth")
  public Room create(
      @Valid @RequestBody RoomRequest request,
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    String managerId = Util.extractToken(authHeader);
    return service.create(request, managerId);
  }

  @PostMapping("/{roomId}/join")
  @Operation(summary = "Criar novo usuário")
  public String join(
      @PathVariable String roomId,
      @RequestBody JoinRoomRequest request) {
    return service.join(roomId, request).getId();
  }

  @PutMapping("/{roomId}")
  @Operation(summary = "Editar sala")
  @SecurityRequirement(name = "bearerAuth")
  public Room update(
      @PathVariable String roomId,
      @RequestBody RoomRequest request,
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    String managerId = Util.extractToken(authHeader);
    service.authorize(roomId, managerId);
    return service.update(roomId, request, managerId);
  }

  @DeleteMapping("/{roomId}")
  @Operation(summary = "Deletar sala (e por consequencia deletar todos os seus usuários e músicas)")
  @SecurityRequirement(name = "bearerAuth")
  public void delete(
      @PathVariable String roomId,
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    String managerId = Util.extractToken(authHeader);
    service.authorize(roomId, managerId);
    service.delete(roomId, managerId);
  }

  @GetMapping("/{roomId}/info")
  @Operation(summary = "Recebe informações básicas de uma dada sala")
  public RoomInfoResponse getRoomInfo(@PathVariable String roomId) {
    return service.getRoomInfo(roomId);
  }

  @GetMapping("/{roomId}")
  @Operation(summary = "Recebe uma sala a partir do código da Sala")
  @SecurityRequirement(name = "bearerAuth")
  public RoomResponse getRoomByUser(
      @PathVariable String roomId,
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    String uuid = Util.extractToken(authHeader);
    return service.getRoomByUserId(roomId, uuid).toResponse();
  }

  @GetMapping("/manager")
  @Operation(summary = "Recebe uma sala a partir do Id do manager")
  @SecurityRequirement(name = "bearerAuth")
  public RoomResponse getRoomByManager(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    String managerId = Util.extractToken(authHeader);
    return service.getRoomByManagerId(managerId).toResponse();
  }

}
