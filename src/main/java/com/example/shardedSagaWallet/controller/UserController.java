package com.example.shardedSagaWallet.controller;

import com.example.shardedSagaWallet.entities.User;
import com.example.shardedSagaWallet.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user){
        User newUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<User>> getUserById(@PathVariable Long id){
        Optional<User> existedUser = userService.getUserById(id);
        if(existedUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(existedUser);
        }
        return ResponseEntity.status(HttpStatus.OK).body(existedUser);
    }

    @GetMapping("/name")
    public ResponseEntity<List<User>> getUserByName(@RequestParam String name){
        List<User> users = userService.getUserByName(name);
        if(users == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

}
