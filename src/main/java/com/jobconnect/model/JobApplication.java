package com.jobconnect.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "job_applications")
public class JobApplication {

    @Id
    private String id;

    private String jobId;

    private String jobTitle;

    private String seekerId;

    private String seekerName;

    private String employerId;

    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Builder.Default
    private Instant appliedAt = Instant.now();

    public enum ApplicationStatus {
        APPLIED, REVIEWED, SHORTLISTED, REJECTED
    }
}
