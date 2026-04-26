package com.karaoke.manager;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ManagerService {
    private final ManagerRepository repository;

    public ManagerService(ManagerRepository repository) {
        this.repository = repository;
    }

    public Manager create(ManagerRequest request) {
        Manager manager = new Manager();
        String email = request.getEmail();
        if (repository.existsByEmail(email)) {
            throw new RuntimeException("Already exists a user with that email");
        }
        manager.setEmail(request.getEmail());
        manager.setType(ManagerType.FREE);
        return repository.save(manager);
    }

    public Manager upgrade(Manager manager, ManagerType type) {
        if (type == ManagerType.FREE) {
            throw new RuntimeException("Invalid Parameters: You can't upgrade account to FREE");
        }
        manager.setPremium_last_payment(Date.from(Instant.now()));
        manager.setType(type);
        repository.save(manager);
        return manager;
    }

    public List<Manager> getAll() {
        return repository.findAll();
    }

    public Manager getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
    }

    public Manager getByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
    }
}
