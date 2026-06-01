package com.karaoke.manager;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.karaoke.Util;

@RestController
@RequestMapping("/manager")
class ManagerController {

  private final ManagerService service;

  public ManagerController(ManagerService service) {
    this.service = service;
  }

  @PostMapping
  @Operation(summary = "Criar novo manager")
  public ManagerResponse create(@Valid @RequestBody ManagerRequest request) {
    return service.create(request).toResponse();
  }

  @GetMapping
  @Operation(summary = "Ver manager pelo seu ID")
  @SecurityRequirement(name = "bearerAuth")
  public ManagerResponse getById(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    String managerId = Util.extractToken(authHeader);
    return service.getById(UUID.fromString(managerId)).toResponse();
  }

  @PostMapping("/login")
  @Operation(summary = "Endpoint de login, mande o email e senha receba o ID do seu usuário")
  public ManagerResponse login(@Valid @RequestBody ManagerRequest request) {
    return service.auth(request).toResponse();
  }

  @PatchMapping("/upgrade/{id}/{type}")
  @Operation(summary = "Endpoint temporário para melhorar a conta de um manager (de FREE para PREMIUM)")
  @SecurityRequirement(name = "bearerAuth")
  public ManagerResponse upgradeType(
      @PathVariable ManagerType type,
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    String managerId = Util.extractToken(authHeader);
    return service.upgrade(service.getById(UUID.fromString(managerId)), type).toResponse();
  }
}
