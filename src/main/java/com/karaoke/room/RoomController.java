package com.karaoke.room;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
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
  @Operation(summary = "Criar Salas")
  public Room create(@Valid @RequestBody RoomRequest request) {
    return service.create(request);
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
  public Room update(
      @PathVariable String roomId,
      @RequestBody RoomRequest request) {
    return service.update(roomId, request);
  }

  @DeleteMapping("/{roomId}")
  @Operation(summary = "Deletar sala (e por consequencia deletar todos os seus usuários e músicas)")
  public void delete(@PathVariable String roomId) {
    service.delete(roomId);
  }

  @GetMapping("/{roomId}/info")
  @Operation(summary = "Recebe informações básicas de uma dada sala")
  public RoomInfoResponse getRoomInfo(@PathVariable String roomId) {
    return service.getRoomInfo(roomId);
  }

  // Temporário, para facilitar integração do frontend
  @GetMapping("/{roomId}/{userOrManagerId}/auth")
  @Operation(summary = "Endpoint temporário de autenticação que recebe um Id (de usuário ou manager) e devolve uma Sala completa")
  public RoomResponse getById(
      @PathVariable String roomId,
      @PathVariable String userOrManagerId) {
    return service.getRoomByUUID(roomId, userOrManagerId).toResponse();
  }

  @GetMapping("/manager/{managerId}")
  @Operation(summary = "Endpoint para para pegar a sala pelo Id do Manager")
  public RoomResponse getByManagerId(@PathVariable String managerId) {
    return service.getRoomByManager(managerId).toResponse();
  }
}
