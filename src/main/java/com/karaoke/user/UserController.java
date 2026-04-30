package com.karaoke.user;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room/{roomId}/{userId}")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }
    @GetMapping
    public User getUser(@PathVariable String roomId,
                        @PathVariable String userId) {
        return service.get(roomId, userId);
    }

    @DeleteMapping
    public void kickUser(@PathVariable String roomId, @PathVariable String userId) {
        service.kickUser(roomId, userId);
    }

}



