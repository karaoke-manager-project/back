package com.karaoke.manager;

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
    public Manager create(@Valid @RequestBody ManagerRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<Manager> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Manager getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @GetMapping
    public Manager getByEmail(@Valid @RequestBody ManagerRequest request) {
        return service.getByEmail(request.getEmail());
    }
}