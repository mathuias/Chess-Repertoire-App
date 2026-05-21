package dev.mathuias.chessrepertoire.service;

import dev.mathuias.chessrepertoire.opening.Opening;
import dev.mathuias.chessrepertoire.opening.OpeningRequest;
import dev.mathuias.chessrepertoire.repository.OpeningRepository;
import dev.mathuias.chessrepertoire.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OpeningService {

    private final OpeningRepository openingRepository;

    public OpeningService(OpeningRepository openingRepository) {
        this.openingRepository = openingRepository;
    }

    @Transactional(readOnly = true)
    public List<Opening> findAll(User owner) {
        return openingRepository.findAllByOwnerOrderByCreatedAtDesc(owner);
    }

    @Transactional(readOnly = true)
    public Optional<Opening> findById(Long id, User owner) {
        return openingRepository.findByIdAndOwner(id, owner);
    }

    public Opening create(User owner, OpeningRequest request) {
        Opening opening = new Opening(
                owner,
                request.name(),
                request.pgn(),
                request.initialFen(),
                request.notes()
        );
        return openingRepository.save(opening);
    }

    public Optional<Opening> update(Long id, User owner, OpeningRequest request) {
        return openingRepository.findByIdAndOwner(id, owner).map(opening -> {
            opening.setName(request.name());
            opening.setPgn(request.pgn());
            opening.setInitialFen(request.initialFen());
            opening.setNotes(request.notes());
            return opening;
        });
    }

    public boolean delete(Long id, User owner) {
        return openingRepository.findByIdAndOwner(id, owner)
                .map(opening -> {
                    openingRepository.delete(opening);
                    return true;
                })
                .orElse(false);
    }
}
