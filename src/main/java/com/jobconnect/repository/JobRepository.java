package com.jobconnect.repository;

import com.jobconnect.model.Job;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface JobRepository extends MongoRepository<Job, String> {

    List<Job> findByActiveTrue();

    List<Job> findByEmployerId(String employerId);

    List<Job> findByActiveTrueAndLocationContainingIgnoreCase(String location);

    List<Job> findByActiveTrueAndTitleContainingIgnoreCase(String keyword);

    List<Job> findByActiveTrueAndTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(
            String keyword, String location);
}
