package dev.mathuias.springdemo.repository;

import dev.mathuias.springdemo.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
