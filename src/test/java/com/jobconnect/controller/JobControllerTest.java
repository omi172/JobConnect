package com.jobconnect.controller;

import com.jobconnect.config.JwtAuthFilter;
import com.jobconnect.config.SecurityConfig;
import com.jobconnect.model.Job;
import com.jobconnect.service.CustomUserDetailsService;
import com.jobconnect.service.JobService;
import com.jobconnect.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Slice test for the MVC layer: verifies routing, view names, and that
 * our custom SecurityConfig enforces role-based access on protected endpoints.
 * SecurityConfig + JwtAuthFilter are imported explicitly since @WebMvcTest does
 * not pick up user-defined @Configuration classes by default; JwtAuthFilter's
 * own dependencies (JwtUtil, CustomUserDetailsService) are mocked so no real
 * JWT parsing or DB lookups happen in this slice.
 */
@WebMvcTest(controllers = JobController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private Job sampleJob() {
        return Job.builder()
                .id("job-1").title("Backend Engineer").description("desc")
                .location("Bangalore").salary(1000000)
                .deadline(LocalDate.now().plusDays(10))
                .employerId("emp-1").companyName("Acme Inc.")
                .build();
    }

    @Test
    void listJobs_isPubliclyAccessible() throws Exception {
        when(jobService.getAllActiveJobs()).thenReturn(List.of(sampleJob()));

        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk())
                .andExpect(view().name("jobs/list"));
    }

    @Test
    void postJobForm_withoutAuthentication_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/jobs/post"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "seeker@example.com", roles = "JOB_SEEKER")
    void postJobForm_asJobSeeker_isForbidden() throws Exception {
        mockMvc.perform(get("/jobs/post"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "employer@example.com", roles = "EMPLOYER")
    void postJobForm_asEmployer_isAllowed() throws Exception {
        mockMvc.perform(get("/jobs/post"))
                .andExpect(status().isOk())
                .andExpect(view().name("jobs/post"));
    }

    @Test
    @WithMockUser(username = "employer@example.com", roles = "EMPLOYER")
    void postJob_asEmployer_withInvalidData_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/jobs/post")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("title", "")
                        .param("description", "")
                        .param("location", "")
                        .param("salary", "-5"))
                .andExpect(status().isOk())
                .andExpect(view().name("jobs/post"));
    }

    @Test
    @WithMockUser(username = "seeker@example.com", roles = "JOB_SEEKER")
    void applyToJob_asJobSeeker_isAllowed() throws Exception {
        when(jobService.applyToJob(any(), any())).thenReturn(
                com.jobconnect.model.JobApplication.builder().id("app-1").build());

        mockMvc.perform(post("/jobs/apply/job-1")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "employer@example.com", roles = "EMPLOYER")
    void applyToJob_asEmployer_isForbidden() throws Exception {
        mockMvc.perform(post("/jobs/apply/job-1")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
    }
}
