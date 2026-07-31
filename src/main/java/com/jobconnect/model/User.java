package com.jobconnect.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @NotBlank
    private String fullName;

    @Indexed(unique = true)
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password; // BCrypt-hashed

    private String phoneNumber; // used for SMS notifications, E.164 format e.g. +15551234567

    private Role role;

    private String companyName; // only relevant for EMPLOYER role

    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
