package com.karaoke.manager;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

  @GetMapping("/{id}")
  @Operation(summary = "Ver manager pelo seu ID")
  public ManagerResponse getById(@PathVariable UUID id) {
    return service.getById(id).toResponse();
  }

  @PostMapping("/login")
  @Operation(summary = "Endpoint de login, mande o email e senha receba o ID do seu usuário")
  public ManagerResponse login(@Valid @RequestBody ManagerRequest request) {
    return service.auth(request).toResponse();
  }

  @PatchMapping("/upgrade/{id}/{type}")
  @Operation(summary = "Endpoint temporário para melhorar a conta de um manager (de FREE para PREMIUM)")
  public ManagerResponse upgradeType(@PathVariable UUID id, @PathVariable ManagerType type) {
    return service.upgrade(service.getById(id), type).toResponse();
  }
}
