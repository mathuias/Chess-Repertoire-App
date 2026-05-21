package dev.mathuias.springdemo.service;

import dev.mathuias.springdemo.repository.UserRepository;
import dev.mathuias.springdemo.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findAll_delegatesToRepository() {
        User alice = new User("Alice", "Anderson", "alice@example.com", "hash");
        User bob = new User("Bob", "Brown", "bob@example.com", "hash");
        when(userRepository.findAll()).thenReturn(List.of(alice, bob));

        List<User> result = userService.findAll();

        assertThat(result).containsExactly(alice, bob);
        verify(userRepository).findAll();
    }

    @Test
    void findById_returnsUserWhenRepositoryFindsIt() {
        User alice = new User("Alice", "Anderson", "alice@example.com", "hash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));

        Optional<User> result = userService.findById(1L);

        assertThat(result).contains(alice);
    }

    @Test
    void findById_returnsEmptyWhenRepositoryReturnsEmpty() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(99L);

        assertThat(result).isEmpty();
    }
}
