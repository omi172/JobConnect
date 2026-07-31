package com.jobconnect.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "jobs")
public class Job {

    @Id
    private String id;

    @NotBlank
    @Indexed
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    @Indexed
    private String location;

    private double salary;

    @NotNull
    private LocalDate deadline;

    @NotBlank
    private String employerId; // reference to User._id (role EMPLOYER)

    private String companyName;

    @Builder.Default
    private Instant postedAt = Instant.now();

    @Builder.Default
    private boolean active = true;
}
