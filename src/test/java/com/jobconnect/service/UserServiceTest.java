package com.jobconnect.service;

import com.jobconnect.dto.RegisterRequest;
import com.jobconnect.exception.EmailAlreadyExistsException;
import com.jobconnect.model.Role;
import com.jobconnect.model.User;
import com.jobconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    //private SmsNotificationService smsNotificationService;

    @InjectMocks
    private UserService userService;

    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest();
        request.setFullName("Jane Doe");
        request.setEmail("Jane@Example.com");
        request.setPassword("secret123");
        request.setPhoneNumber("+15551234567");
        request.setRole(Role.JOB_SEEKER);
    }

    @Test
    @DisplayName("registers a new user, hashes password, lower-cases email, and sends SMS confirmation")
    void register_success() {
        when(userRepository.existsByEmail("Jane@Example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.register(request);

        assertThat(saved.getEmail()).isEqualTo("jane@example.com");
        assertThat(saved.getPassword()).isEqualTo("hashed-password");
        assertThat(saved.getRole()).isEqualTo(Role.JOB_SEEKER);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getFullName()).isEqualTo("Jane Doe");

        verify(smsNotificationService).notifyRegistrationConfirmed("+15551234567", "Jane Doe");
    }

    @Test
    @DisplayName("throws EmailAlreadyExistsException when the email is already registered")
    void register_duplicateEmail_throws() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining(request.getEmail());

        verify(userRepository, never()).save(any());
        verifyNoInteractions(smsNotificationService);
    }

    @Test
    @DisplayName("findByEmail returns the matching user")
    void findByEmail_found() {
        User user = User.builder().email("jane@example.com").build();
        when(userRepository.findByEmail("jane@example.com")).thenReturn(java.util.Optional.of(user));

        User result = userService.findByEmail("jane@example.com");

        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("findByEmail throws when no user exists")
    void findByEmail_notFound_throws() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("missing@example.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
