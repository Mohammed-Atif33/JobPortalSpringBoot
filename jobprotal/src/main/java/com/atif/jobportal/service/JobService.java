package com.atif.jobportal.service;

import com.atif.jobportal.dto.JobRequestDTO;
import com.atif.jobportal.dto.JobResponseDTO;
import com.atif.jobportal.entity.User;
import com.atif.jobportal.exception.JobNotFoundException;
import com.atif.jobportal.job.Job;
import com.atif.jobportal.repository.JobRepository;
import com.atif.jobportal.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.atif.jobportal.repository.ApplicationRepository;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    public JobService(
            JobRepository jobRepository,
            UserRepository userRepository, ApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
    }

    public JobResponseDTO createJob(JobRequestDTO request) {

        User recruiter = getCurrentRecruiter();

        Job job = new Job();
        updateJobFields(job, request);
        job.setRecruiter(recruiter);

        Job savedJob = jobRepository.save(job);

        return convertToResponseDTO(savedJob);
    }

    @Transactional(readOnly = true)
    public List<JobResponseDTO> getAllJobs(String search) {

        String query = search == null ? "" : search.trim();
        List<Job> jobs = query.isEmpty()
                ? jobRepository.findAll()
                : jobRepository.searchAvailableJobs(query);

        return jobs
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public List<JobResponseDTO> getMyJobs() {

        User recruiter = getCurrentRecruiter();

        return jobRepository.findByRecruiterId(recruiter.getId())
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public JobResponseDTO getJobById(Long id) {

        Job job = findJobById(id);

        return convertToResponseDTO(job);
    }

    public JobResponseDTO updateJob(
            Long id,
            JobRequestDTO request) {

        User recruiter = getCurrentRecruiter();
        Job job = findJobById(id);

        verifyOwnership(job, recruiter);

        updateJobFields(job, request);

        Job updatedJob = jobRepository.save(job);

        return convertToResponseDTO(updatedJob);
    }

    public void deleteJob(Long id) {

        User recruiter = getCurrentRecruiter();
        Job job = findJobById(id);

        verifyOwnership(job, recruiter);

        Long jobId = job.getId();
        long applicationCount = applicationRepository.countByJobId(jobId);

        if (applicationCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete this job because " + applicationCount
                            + " application(s) are linked to it."
            );
        }

        jobRepository.delete(job);
    }

    private User getCurrentRecruiter() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException(
                    "Authentication is required");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Authenticated user not found"));

        if (!"RECRUITER".equals(user.getRole())) {
            throw new AccessDeniedException(
                    "Only recruiters can manage jobs");
        }

        return user;
    }

    private void verifyOwnership(Job job, User recruiter) {

        if (job.getRecruiter() == null
                || !job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException(
                    "You can only manage your own jobs");
        }
    }

    private Job findJobById(Long id) {

        return jobRepository.findById(id)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found with id: " + id
                        )
                );
    }

    private void updateJobFields(
            Job job,
            JobRequestDTO request) {

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setExperience(request.getExperience());
        job.setEmploymentType(request.getEmploymentType());
    }

    private JobResponseDTO convertToResponseDTO(Job job) {

        JobResponseDTO response = new JobResponseDTO(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getSalary(),
                job.getExperience(),
                job.getEmploymentType()
        );
        if (job.getRecruiter() != null) {
            response.setRecruiterName(job.getRecruiter().getName());
        }
        return response;
    }
}
