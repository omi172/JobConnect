package com.jobconnect.service;

import com.jobconnect.dto.JobRequest;
import com.jobconnect.exception.ResourceNotFoundException;
import com.jobconnect.exception.UnauthorizedActionException;
import com.jobconnect.model.Job;
import com.jobconnect.model.JobApplication;
import com.jobconnect.model.Role;
import com.jobconnect.model.User;
import com.jobconnect.repository.JobApplicationRepository;
import com.jobconnect.repository.JobRepository;
import com.jobconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobService")
class JobServiceTest {

    @Mock private JobRepository jobRepository;
    @Mock private JobApplicationRepository jobApplicationRepository;
    @Mock private UserRepository userRepository;
   // @Mock private SmsNotificationService smsNotificationService;

    @InjectMocks
    private JobService jobService;

    private User employer;
    private User seeker;
    private Job job;

    @BeforeEach
    void setUp() {
        employer = User.builder()
                .id("emp-1").email("employer@example.com").fullName("Acme HR")
                .phoneNumber("+15550001111").role(Role.EMPLOYER).companyName("Acme Inc.")
                .build();

        seeker = User.builder()
                .id("seek-1").email("seeker@example.com").fullName("Jane Doe")
                .phoneNumber("+15552223333").role(Role.JOB_SEEKER)
                .build();

        job = Job.builder()
                .id("job-1").title("Backend Engineer").description("Build things")
                .location("Bangalore").salary(1500000).deadline(LocalDate.now().plusDays(30))
                .employerId("emp-1").companyName("Acme Inc.")
                .build();
    }

    @Nested
    @DisplayName("Job Posting")
    class Posting {

        @Test
        @DisplayName("creates a job for the employer and sends an SMS alert")
        void postJob_success() {
            JobRequest request = new JobRequest();
            request.setTitle("Backend Engineer");
            request.setDescription("Build things");
            request.setLocation("Bangalore");
            request.setSalary(1500000);
            request.setDeadline(LocalDate.now().plusDays(30));

            when(userRepository.findByEmail("employer@example.com")).thenReturn(Optional.of(employer));
            when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

            Job result = jobService.postJob(request, "employer@example.com");

            assertThat(result.getEmployerId()).isEqualTo("emp-1");
            assertThat(result.getCompanyName()).isEqualTo("Acme Inc.");
            verify(smsNotificationService).notifyJobPosted("+15550001111", "Backend Engineer");
        }

        @Test
        @DisplayName("deleteJob rejects an employer who does not own the job")
        void deleteJob_wrongOwner_throws() {
            User otherEmployer = User.builder().id("emp-2").email("other@example.com").role(Role.EMPLOYER).build();

            when(jobRepository.findById("job-1")).thenReturn(Optional.of(job));
            when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherEmployer));

            assertThatThrownBy(() -> jobService.deleteJob("job-1", "other@example.com"))
                    .isInstanceOf(UnauthorizedActionException.class);

            verify(jobRepository, never()).delete(any());
        }

        @Test
        @DisplayName("updateJob throws ResourceNotFoundException for a missing job id")
        void updateJob_missingJob_throws() {
            when(jobRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> jobService.updateJob("missing", new JobRequest(), "employer@example.com"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Job Search")
    class Search {

        @Test
        @DisplayName("searchJobs with both keyword and location filters by both")
        void searchJobs_keywordAndLocation() {
            when(jobRepository.findByActiveTrueAndTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(
                    "Engineer", "Bangalore")).thenReturn(List.of(job));

            List<Job> results = jobService.searchJobs("Engineer", "Bangalore");

            assertThat(results).containsExactly(job);
            verify(jobRepository).findByActiveTrueAndTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(
                    "Engineer", "Bangalore");
        }

        @Test
        @DisplayName("searchJobs with no filters returns all active jobs")
        void searchJobs_noFilters_returnsAll() {
            when(jobRepository.findByActiveTrue()).thenReturn(List.of(job));

            List<Job> results = jobService.searchJobs(null, "  ");

            assertThat(results).containsExactly(job);
            verify(jobRepository).findByActiveTrue();
        }

        @Test
        @DisplayName("searchJobs with only keyword filters by title")
        void searchJobs_keywordOnly() {
            when(jobRepository.findByActiveTrueAndTitleContainingIgnoreCase("Engineer"))
                    .thenReturn(List.of(job));

            List<Job> results = jobService.searchJobs("Engineer", null);

            assertThat(results).containsExactly(job);
        }
    }

    @Nested
    @DisplayName("Applications")
    class Applications {

        @Test
        @DisplayName("applyToJob creates an application and notifies both seeker and employer via SMS")
        void applyToJob_success() {
            when(jobRepository.findById("job-1")).thenReturn(Optional.of(job));
            when(userRepository.findByEmail("seeker@example.com")).thenReturn(Optional.of(seeker));
            when(jobApplicationRepository.existsByJobIdAndSeekerId("job-1", "seek-1")).thenReturn(false);
            when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userRepository.findById("emp-1")).thenReturn(Optional.of(employer));

            JobApplication result = jobService.applyToJob("job-1", "seeker@example.com");

            assertThat(result.getJobId()).isEqualTo("job-1");
            assertThat(result.getSeekerId()).isEqualTo("seek-1");
            verify(smsNotificationService).notifyApplicationSubmitted("+15552223333", "Backend Engineer");
            verify(smsNotificationService).notifyApplicationReceived("+15550001111", "Backend Engineer");
        }

        @Test
        @DisplayName("applyToJob rejects a duplicate application")
        void applyToJob_duplicate_throws() {
            when(jobRepository.findById("job-1")).thenReturn(Optional.of(job));
            when(userRepository.findByEmail("seeker@example.com")).thenReturn(Optional.of(seeker));
            when(jobApplicationRepository.existsByJobIdAndSeekerId("job-1", "seek-1")).thenReturn(true);

            assertThatThrownBy(() -> jobService.applyToJob("job-1", "seeker@example.com"))
                    .isInstanceOf(IllegalStateException.class);

            verify(jobApplicationRepository, never()).save(any());
            verifyNoInteractions(smsNotificationService);
        }

        @Test
        @DisplayName("getApplicationsForJob enforces employer ownership")
        void getApplicationsForJob_wrongOwner_throws() {
            User otherEmployer = User.builder().id("emp-2").email("other@example.com").role(Role.EMPLOYER).build();

            when(jobRepository.findById("job-1")).thenReturn(Optional.of(job));
            when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherEmployer));

            assertThatThrownBy(() -> jobService.getApplicationsForJob("job-1", "other@example.com"))
                    .isInstanceOf(UnauthorizedActionException.class);
        }
    }
}
