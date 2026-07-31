package com.jobconnect.controller;

import com.jobconnect.dto.JobRequest;
import com.jobconnect.model.Job;
import com.jobconnect.model.JobApplication;
import com.jobconnect.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    // ---------- Job Search (Seeker) — publicly browsable ----------

    @GetMapping
    public String listJobs(Model model) {
        model.addAttribute("jobs", jobService.getAllActiveJobs());
        return "jobs/list";
    }

    @GetMapping("/search")
    public String searchJobs(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String location,
                              Model model) {
        List<Job> results = jobService.searchJobs(keyword, location);
        model.addAttribute("jobs", results);
        model.addAttribute("keyword", keyword);
        model.addAttribute("location", location);
        return "jobs/list";
    }

    @GetMapping("/view/{id}")
    public String viewJob(@PathVariable String id, Model model) {
        model.addAttribute("job", jobService.getJobById(id));
        return "jobs/view";
    }

    @PostMapping("/apply/{id}")
    public String applyToJob(@PathVariable String id, Authentication authentication, Model model) {
        try {
            jobService.applyToJob(id, authentication.getName());
            return "redirect:/seeker/applications?applied";
        } catch (IllegalStateException ex) {
            model.addAttribute("job", jobService.getJobById(id));
            model.addAttribute("errorMessage", ex.getMessage());
            return "jobs/view";
        }
    }

    // ---------- Job Posting (Employer) ----------

    @GetMapping("/post")
    public String postJobForm(Model model) {
        model.addAttribute("jobRequest", new JobRequest());
        return "jobs/post";
    }

    @PostMapping("/post")
    public String postJob(@Valid @ModelAttribute("jobRequest") JobRequest request,
                           BindingResult bindingResult,
                           Authentication authentication,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "jobs/post";
        }
        jobService.postJob(request, authentication.getName());
        return "redirect:/employer/jobs?posted";
    }

    @GetMapping("/edit/{id}")
    public String editJobForm(@PathVariable String id, Model model) {
        Job job = jobService.getJobById(id);
        JobRequest request = new JobRequest();
        request.setTitle(job.getTitle());
        request.setDescription(job.getDescription());
        request.setLocation(job.getLocation());
        request.setSalary(job.getSalary());
        request.setDeadline(job.getDeadline());
        model.addAttribute("jobRequest", request);
        model.addAttribute("jobId", id);
        return "jobs/edit";
    }

    @PostMapping("/edit/{id}")
    public String editJob(@PathVariable String id,
                           @Valid @ModelAttribute("jobRequest") JobRequest request,
                           BindingResult bindingResult,
                           Authentication authentication,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("jobId", id);
            return "jobs/edit";
        }
        jobService.updateJob(id, request, authentication.getName());
        return "redirect:/employer/jobs?updated";
    }

    @PostMapping("/delete/{id}")
    public String deleteJob(@PathVariable String id, Authentication authentication) {
        jobService.deleteJob(id, authentication.getName());
        return "redirect:/employer/jobs?deleted";
    }

    // ---------- Employer / Seeker dashboards ----------

    @GetMapping("/employer/jobs")
    public String employerJobs(Authentication authentication, Model model) {
        model.addAttribute("jobs", jobService.getJobsByEmployer(authentication.getName()));
        return "jobs/employer-dashboard";
    }

    @GetMapping("/employer/applications/{jobId}")
    public String applicationsForJob(@PathVariable String jobId, Authentication authentication, Model model) {
        List<JobApplication> applications = jobService.getApplicationsForJob(jobId, authentication.getName());
        model.addAttribute("applications", applications);
        model.addAttribute("job", jobService.getJobById(jobId));
        return "jobs/applications";
    }

    @GetMapping("/seeker/applications")
    public String seekerApplications(Authentication authentication, Model model) {
        model.addAttribute("applications", jobService.getApplicationsForSeeker(authentication.getName()));
        return "jobs/my-applications";
    }
}
