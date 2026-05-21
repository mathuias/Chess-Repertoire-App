package dev.mathuias.chessrepertoire.repository;

import dev.mathuias.chessrepertoire.opening.Opening;
import dev.mathuias.chessrepertoire.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OpeningRepository extends JpaRepository<Opening, Long> {

    List<Opening> findAllByOwnerOrderByCreatedAtDesc(User owner);

    Optional<Opening> findByIdAndOwner(Long id, User owner);
}
