package com.jobconnect.service;

import com.jobconnect.dto.JobRequest;
import com.jobconnect.exception.ResourceNotFoundException;
import com.jobconnect.exception.UnauthorizedActionException;
import com.jobconnect.model.Job;
import com.jobconnect.model.JobApplication;
import com.jobconnect.model.User;
import com.jobconnect.repository.JobApplicationRepository;
import com.jobconnect.repository.JobRepository;
import com.jobconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;
//    private final SmsNotificationService smsNotificationService;

    // ---------- Job Posting (Employer) ----------

    public Job postJob(JobRequest request, String employerEmail) {
        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found: " + employerEmail));

        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .salary(request.getSalary())
                .deadline(request.getDeadline())
                .employerId(employer.getId())
                .companyName(employer.getCompanyName())
                .build();

        Job saved = jobRepository.save(job);
        log.info("Employer {} posted job '{}'", employerEmail, saved.getTitle());

        // SMS Notification: job posting alert (Image 1 requirement)
       // smsNotificationService.notifyJobPosted(employer.getPhoneNumber(), saved.getTitle());

        return saved;
    }

    public Job updateJob(String jobId, JobRequest request, String employerEmail) {
        Job job = getJobOrThrow(jobId);
        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found: " + employerEmail));

        assertOwnership(job, employer);

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setDeadline(request.getDeadline());

        return jobRepository.save(job);
    }

    public void deleteJob(String jobId, String employerEmail) {
        Job job = getJobOrThrow(jobId);
        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found: " + employerEmail));

        assertOwnership(job, employer);
        jobRepository.delete(job);
        log.info("Employer {} deleted job '{}'", employerEmail, job.getTitle());
    }

    public List<Job> getJobsByEmployer(String employerEmail) {
        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found: " + employerEmail));
        return jobRepository.findByEmployerId(employer.getId());
    }

    private void assertOwnership(Job job, User employer) {
        if (!job.getEmployerId().equals(employer.getId())) {
            throw new UnauthorizedActionException("You do not have permission to modify this job posting.");
        }
    }

    // ---------- Job Search (Seeker) ----------

    public List<Job> getAllActiveJobs() {
        return jobRepository.findByActiveTrue();
    }

    public Job getJobById(String jobId) {
        return getJobOrThrow(jobId);
    }

    public List<Job> searchJobs(String keyword, String location) {
        boolean hasKeyword = StringUtils.hasText(keyword);
        boolean hasLocation = StringUtils.hasText(location);

        if (hasKeyword && hasLocation) {
            return jobRepository.findByActiveTrueAndTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(keyword, location);
        } else if (hasKeyword) {
            return jobRepository.findByActiveTrueAndTitleContainingIgnoreCase(keyword);
        } else if (hasLocation) {
            return jobRepository.findByActiveTrueAndLocationContainingIgnoreCase(location);
        }
        return jobRepository.findByActiveTrue();
    }

    private Job getJobOrThrow(String jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));
    }

    // ---------- Applications ----------

    public JobApplication applyToJob(String jobId, String seekerEmail) {
        Job job = getJobOrThrow(jobId);
        User seeker = userRepository.findByEmail(seekerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found: " + seekerEmail));

        if (jobApplicationRepository.existsByJobIdAndSeekerId(jobId, seeker.getId())) {
            throw new IllegalStateException("You have already applied to this job.");
        }

        JobApplication application = JobApplication.builder()
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .seekerId(seeker.getId())
                .seekerName(seeker.getFullName())
                .employerId(job.getEmployerId())
                .build();

        JobApplication saved = jobApplicationRepository.save(application);

        // SMS Notification: application confirmation to seeker + alert to employer (Image 1 requirement)
//        smsNotificationService.notifyApplicationSubmitted(seeker.getPhoneNumber(), job.getTitle());
//        userRepository.findById(job.getEmployerId())
//                .ifPresent(employer -> smsNotificationService.notifyApplicationReceived(employer.getPhoneNumber(), job.getTitle()));

        return saved;
    }

    public List<JobApplication> getApplicationsForSeeker(String seekerEmail) {
        User seeker = userRepository.findByEmail(seekerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found: " + seekerEmail));
        return jobApplicationRepository.findBySeekerId(seeker.getId());
    }

    public List<JobApplication> getApplicationsForJob(String jobId, String employerEmail) {
        Job job = getJobOrThrow(jobId);
        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found: " + employerEmail));
        assertOwnership(job, employer);
        return jobApplicationRepository.findByJobId(jobId);
    }
}
