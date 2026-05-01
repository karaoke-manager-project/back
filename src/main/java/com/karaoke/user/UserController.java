package com.karaoke.user;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room/{roomId}")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Recebe todos os usuários de uma sala")
    public List<UserResponse> getAll(@PathVariable String roomId) {
        return service.getAll(roomId);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Recebe um usuário de uma sala")
    public User getUser(@PathVariable String roomId,
                        @PathVariable String userId) {
        return service.get(roomId, userId);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Expulsa um usuário de uma sala")
    public void kickUser(@PathVariable String roomId, @PathVariable String userId) {
        service.kickUser(roomId, userId);
    }

}



