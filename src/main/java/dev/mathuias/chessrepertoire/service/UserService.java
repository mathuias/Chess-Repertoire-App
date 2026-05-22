package dev.mathuias.chessrepertoire.service;

import dev.mathuias.chessrepertoire.repository.UserRepository;
import dev.mathuias.chessrepertoire.user.User;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        log.info("Finding user with id {}", id);
        return userRepository.findById(id);
    }
}
