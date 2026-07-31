package com.jobconnect.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDate;

@Data
public class JobRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @PositiveOrZero(message = "Salary must be zero or positive")
    private double salary;

    @NotNull(message = "Deadline is required")
    @Future(message = "Deadline must be in the future")
    private LocalDate deadline;
}
