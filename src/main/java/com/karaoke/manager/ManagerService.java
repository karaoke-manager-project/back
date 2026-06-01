package com.karaoke.manager;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ManagerService {
  private final ManagerRepository repository;
  BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  public ManagerService(ManagerRepository repository) {
    this.repository = repository;
  }

  public Manager create(ManagerRequest request) {
    Manager manager = new Manager();
    String email = request.getEmail();
    if (repository.existsByEmail(email)) {
      throw new RuntimeException("Já existe um usuário com esse email.");
    }
    manager.setEmail(request.getEmail());
    manager.setPasswordHash(encoder.encode(request.getPassword()));
    manager.setType(ManagerType.FREE);
    return repository.save(manager);
  }

  public Manager upgrade(Manager manager, ManagerType type) {
    if (type == ManagerType.FREE) {
      throw new RuntimeException("Parâmetros inválidos: Você não pode melhorar o sistema para FREE.");
    }
    manager.setPremium_last_payment(Date.from(Instant.now()));
    manager.setType(type);
    repository.save(manager);
    return manager;
  }

  public Manager auth(ManagerRequest request) {
    Manager manager = getByEmail(request.getEmail());
    if (!encoder.matches(request.getPassword(), manager.getPasswordHash())) {
      throw new RuntimeException("Senha incorreta.");
    }
    return manager;
  }

  public void save(Manager manager) {
    repository.save(manager);
  }

  public Manager getById(UUID id) {
    return repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Gerente não encontrado."));
  }

  public Manager getByEmail(String email) {
    return repository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("Gerente não encontrado."));
  }
}
