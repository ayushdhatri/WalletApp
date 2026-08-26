package com.example.shardedSagaWallet.services;

import com.example.shardedSagaWallet.entities.User;
import com.example.shardedSagaWallet.repositories.UserRepository;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public User createUser(User user){
        log.info("Creating user : {}", user.getEmail());
        User newUser = this.userRepository.save(user);
        log.info("User created with id {} in database shardwallet{}", newUser.getId(), newUser.getId() % 2 + 1);
        return newUser;
    }
}
