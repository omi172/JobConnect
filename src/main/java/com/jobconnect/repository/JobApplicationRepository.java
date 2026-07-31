package com.jobconnect.repository;

import com.jobconnect.model.JobApplication;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface JobApplicationRepository extends MongoRepository<JobApplication, String> {
    List<JobApplication> findBySeekerId(String seekerId);
    List<JobApplication> findByJobId(String jobId);
    List<JobApplication> findByEmployerId(String employerId);
    boolean existsByJobIdAndSeekerId(String jobId, String seekerId);
}
