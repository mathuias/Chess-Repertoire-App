package dev.mathuias.chessrepertoire.service;

import dev.mathuias.chessrepertoire.opening.Opening;
import dev.mathuias.chessrepertoire.opening.OpeningRequest;
import dev.mathuias.chessrepertoire.repository.OpeningRepository;
import dev.mathuias.chessrepertoire.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpeningServiceTest {

    @Mock
    private OpeningRepository openingRepository;

    @InjectMocks
    private OpeningService openingService;

    private final User alice = new User("Alice", "Anderson", "alice@example.com", "hash");

    @Test
    void create_savesOpeningWithCorrectOwnerAndFields() {
        OpeningRequest req = new OpeningRequest("Ruy Lopez", "1. e4 e5 2. Nf3 Nc6 3. Bb5", null, "main line");
        when(openingRepository.save(any(Opening.class))).thenAnswer(inv -> inv.getArgument(0));

        openingService.create(alice, req);

        ArgumentCaptor<Opening> captor = ArgumentCaptor.forClass(Opening.class);
        verify(openingRepository).save(captor.capture());
        Opening saved = captor.getValue();
        assertThat(saved.getOwner()).isSameAs(alice);
        assertThat(saved.getName()).isEqualTo("Ruy Lopez");
        assertThat(saved.getPgn()).isEqualTo("1. e4 e5 2. Nf3 Nc6 3. Bb5");
        assertThat(saved.getNotes()).isEqualTo("main line");
    }

    @Test
    void update_mutatesExistingOpeningWhenOwnedByUser() {
        Opening existing = new Opening(alice, "Old", "old pgn", null, null);
        when(openingRepository.findByIdAndOwner(42L, alice)).thenReturn(Optional.of(existing));

        Optional<Opening> result = openingService.update(42L, alice,
                new OpeningRequest("New", "new pgn", "fen", "notes"));

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("New");
        assertThat(result.get().getPgn()).isEqualTo("new pgn");
        assertThat(result.get().getInitialFen()).isEqualTo("fen");
        assertThat(result.get().getNotes()).isEqualTo("notes");
    }

    @Test
    void update_returnsEmptyWhenOpeningNotOwnedByUser() {
        when(openingRepository.findByIdAndOwner(42L, alice)).thenReturn(Optional.empty());

        Optional<Opening> result = openingService.update(42L, alice,
                new OpeningRequest("New", "new pgn", null, null));

        assertThat(result).isEmpty();
    }

    @Test
    void delete_returnsFalseWhenOpeningNotOwnedByUser() {
        when(openingRepository.findByIdAndOwner(42L, alice)).thenReturn(Optional.empty());

        assertThat(openingService.delete(42L, alice)).isFalse();
        verify(openingRepository, never()).delete(any());
    }

    @Test
    void delete_returnsTrueAndDeletesWhenOwned() {
        Opening existing = new Opening(alice, "Caro-Kann", "1. e4 c6", null, null);
        when(openingRepository.findByIdAndOwner(42L, alice)).thenReturn(Optional.of(existing));

        assertThat(openingService.delete(42L, alice)).isTrue();
        verify(openingRepository).delete(existing);
    }
}
