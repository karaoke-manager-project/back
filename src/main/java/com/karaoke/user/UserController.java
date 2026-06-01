package com.karaoke.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.karaoke.Util;

@RestController
@RequestMapping("/room/{roomId}/user")
public class UserController {
  private final UserService service;

  public UserController(UserService service) {
    this.service = service;
  }

  @GetMapping("/all")
  @Operation(summary = "Recebe todos os usuários de uma sala")
  public List<UserResponse> getAll(@PathVariable String roomId) {
    return service.getAll(roomId);
  }

  @GetMapping
  @Operation(summary = "Recebe um usuário de uma sala")
  @SecurityRequirement(name = "bearerAuth")
  public User getUser(
      @PathVariable String roomId,
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    String userId = Util.extractToken(authHeader);
    return service.get(roomId, userId);
  }

  @DeleteMapping("/{userId}")
  @Operation(summary = "Expulsa um usuário de uma sala")
  public void kickUser(@PathVariable String roomId, @PathVariable String userId) {
    service.kickUser(roomId, userId);
  }

}
