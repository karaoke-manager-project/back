package com.karaoke.user;

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
    public List<UserResponse> getAll(@PathVariable String roomId) {
        return service.getAll(roomId);
    }

    @GetMapping("/{userId}")
    public User getUser(@PathVariable String roomId,
                        @PathVariable String userId) {
        return service.get(roomId, userId);
    }

    @DeleteMapping("/{userId}")
    public void kickUser(@PathVariable String roomId, @PathVariable String userId) {
        service.kickUser(roomId, userId);
    }

}



