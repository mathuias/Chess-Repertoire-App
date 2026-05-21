package dev.mathuias.chessrepertoire.repository;

import dev.mathuias.chessrepertoire.opening.Opening;
import dev.mathuias.chessrepertoire.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(LiquibaseAutoConfiguration.class)
@ActiveProfiles("test")
class OpeningRepositoryTest {

    @Autowired
    private OpeningRepository openingRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findAllByOwner_onlyReturnsOpeningsForThatUser() {
        User alice = userRepository.findByEmail("alice@example.com").orElseThrow();
        User bob = userRepository.findByEmail("bob@example.com").orElseThrow();

        openingRepository.save(new Opening(alice, "Ruy Lopez", "1. e4 e5 2. Nf3 Nc6 3. Bb5", null, null));
        openingRepository.save(new Opening(alice, "Italian Game", "1. e4 e5 2. Nf3 Nc6 3. Bc4", null, null));
        openingRepository.save(new Opening(bob, "Sicilian Defense", "1. e4 c5", null, null));

        List<Opening> aliceOpenings = openingRepository.findAllByOwnerOrderByCreatedAtDesc(alice);
        List<Opening> bobOpenings = openingRepository.findAllByOwnerOrderByCreatedAtDesc(bob);

        assertThat(aliceOpenings)
                .hasSize(2)
                .extracting(Opening::getName)
                .containsExactlyInAnyOrder("Ruy Lopez", "Italian Game");
        assertThat(bobOpenings)
                .hasSize(1)
                .extracting(Opening::getName)
                .containsExactly("Sicilian Defense");
    }

    @Test
    void findByIdAndOwner_returnsEmptyWhenOwnerMismatch() {
        User alice = userRepository.findByEmail("alice@example.com").orElseThrow();
        User bob = userRepository.findByEmail("bob@example.com").orElseThrow();
        Opening aliceOpening = openingRepository.save(
                new Opening(alice, "Caro-Kann", "1. e4 c6", null, null));

        assertThat(openingRepository.findByIdAndOwner(aliceOpening.getId(), alice)).isPresent();
        assertThat(openingRepository.findByIdAndOwner(aliceOpening.getId(), bob)).isEmpty();
    }
}
