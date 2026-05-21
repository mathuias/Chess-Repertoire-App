package dev.mathuias.springdemo.repository;

import dev.mathuias.springdemo.user.User;
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
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findAll_returnsThreeLiquibaseSeedUsers() {
        List<User> users = userRepository.findAll();

        assertThat(users)
                .hasSize(3)
                .extracting(User::getFirstname, User::getLastname)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("Alice", "Anderson"),
                        org.assertj.core.groups.Tuple.tuple("Bob", "Brown"),
                        org.assertj.core.groups.Tuple.tuple("Charlie", "Clark"));
    }

    @Test
    void findById_returnsUserWhenPresent() {
        Long anyId = userRepository.findAll().get(0).getId();

        assertThat(userRepository.findById(anyId)).isPresent();
    }

    @Test
    void findById_returnsEmptyWhenAbsent() {
        assertThat(userRepository.findById(9_999L)).isEmpty();
    }
}
