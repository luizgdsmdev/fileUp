package com.bytebybyte.fileup.Controllers.User;


import com.bytebybyte.fileup.Application.DTOs.Request.User.UserUpdateRequest;
import com.bytebybyte.fileup.Application.DTOs.Response.User.UserResponse;
import com.bytebybyte.fileup.Application.Services.User.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService _userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId){
        return _userService.get(userId);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(@Valid @PathVariable UUID userId,
                                                   @Valid @RequestBody UserUpdateRequest userUpdateRequest){
        return _userService.update(userId, userUpdateRequest);
    }

}
