package com.jobconnect.service;

import com.jobconnect.dto.RegisterRequest;
import com.jobconnect.exception.EmailAlreadyExistsException;
import com.jobconnect.model.User;
import com.jobconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    //private final SmsNotificationService smsNotificationService;

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .companyName(request.getCompanyName())
                .build();

        User saved = userRepository.save(user);
        log.info("Registered new {} account: {}", saved.getRole(), saved.getEmail());

        // SMS Notification: registration confirmation (Image 1 requirement)
        //smsNotificationService.notifyRegistrationConfirmed(saved.getPhoneNumber(), saved.getFullName());

        return saved;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No user found with email: " + email));
    }
}
