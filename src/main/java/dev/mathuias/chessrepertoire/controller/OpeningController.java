package dev.mathuias.chessrepertoire.controller;

import dev.mathuias.chessrepertoire.opening.Opening;
import dev.mathuias.chessrepertoire.opening.OpeningRequest;
import dev.mathuias.chessrepertoire.opening.OpeningResponse;
import dev.mathuias.chessrepertoire.repository.UserRepository;
import dev.mathuias.chessrepertoire.service.OpeningService;
import dev.mathuias.chessrepertoire.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/openings")
public class OpeningController {

    private final OpeningService openingService;
    private final UserRepository userRepository;

    public OpeningController(OpeningService openingService, UserRepository userRepository) {
        this.openingService = openingService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<OpeningResponse> getAll(@AuthenticationPrincipal UserDetails principal) {
        User owner = currentUser(principal);
        return openingService.findAll(owner).stream()
                .map(OpeningResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OpeningResponse> getById(@PathVariable Long id,
                                                   @AuthenticationPrincipal UserDetails principal) {
        User owner = currentUser(principal);
        return openingService.findById(id, owner)
                .map(o -> ResponseEntity.ok(OpeningResponse.from(o)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OpeningResponse> create(@Valid @RequestBody OpeningRequest request,
                                                  @AuthenticationPrincipal UserDetails principal) {
        User owner = currentUser(principal);
        Opening opening = openingService.create(owner, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(OpeningResponse.from(opening));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OpeningResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody OpeningRequest request,
                                                  @AuthenticationPrincipal UserDetails principal) {
        User owner = currentUser(principal);
        return openingService.update(id, owner, request)
                .map(o -> ResponseEntity.ok(OpeningResponse.from(o)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails principal) {
        User owner = currentUser(principal);
        return openingService.delete(id, owner)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private User currentUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated principal not found in user repository: " + principal.getUsername()));
    }
}
