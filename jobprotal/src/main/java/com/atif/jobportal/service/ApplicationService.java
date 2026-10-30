package com.atif.jobportal.service;

import com.atif.jobportal.application.Application;
import com.atif.jobportal.application.ApplicationStatus;
import com.atif.jobportal.dto.ApplicationRequestDTO;
import com.atif.jobportal.dto.ApplicationResponseDTO;
import com.atif.jobportal.dto.ApplicationStatusRequestDTO;
import com.atif.jobportal.dto.RecruiterCandidateProfileResponseDTO;
import com.atif.jobportal.entity.User;
import com.atif.jobportal.exception.ApplicationNotFoundException;
import com.atif.jobportal.exception.UserNotFoundException;
import com.atif.jobportal.exception.JobNotFoundException;
import com.atif.jobportal.job.Job;
import com.atif.jobportal.repository.ApplicationRepository;
import com.atif.jobportal.repository.JobRepository;
import com.atif.jobportal.repository.UserRepository;
import com.atif.jobportal.security.UserRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobSeekerProfileService profileService;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            UserRepository userRepository,
            JobRepository jobRepository,
            JobSeekerProfileService profileService) {

        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.profileService = profileService;
    }

    @Transactional
    public ApplicationResponseDTO createApplication(
            ApplicationRequestDTO request,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + email
                        )
                );

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found with id: " + request.getJobId()
                        )
                );

        if (applicationRepository.existsByUserIdAndJobId(
                user.getId(), job.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have already applied for this job."
            );
        }

        Application application = new Application();

        application.setUser(user);
        application.setJob(job);
        application.setStatus(ApplicationStatus.APPLIED);

        Application savedApplication =
                applicationRepository.save(application);

        return convertToResponseDTO(savedApplication);
    }

    public List<ApplicationResponseDTO> getAllApplications() {

        return applicationRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public ApplicationResponseDTO getApplicationById(Long id, String email) {

        User recruiter = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Recruiter not found with email: " + email
                        )
                );
        Application application = applicationRepository
                .findWithApplicantAndJobById(id)
                .orElseThrow(() ->
                        new ApplicationNotFoundException(
                                "Application not found with id: " + id
                        )
                );

        if (application.getJob().getRecruiter() == null
                || !application.getJob().getRecruiter().getId()
                .equals(recruiter.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only view applications for your own jobs."
            );
        }

        return convertToResponseDTO(application);
    }

    @Transactional(readOnly = true)
    public RecruiterCandidateProfileResponseDTO getCandidateProfile(
            Long applicationId,
            String email) {
        User recruiter = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Recruiter not found with email: " + email
                        )
                );
        if (!UserRole.RECRUITER.name().equals(recruiter.getRole())) {
            throw new AccessDeniedException(
                    "Only recruiters can view applicant profiles.");
        }

        Application application = applicationRepository
                .findWithApplicantAndJobById(applicationId)
                .orElseThrow(() ->
                        new ApplicationNotFoundException(
                                "Application not found with id: " + applicationId
                        )
                );

        Job job = application.getJob();
        if (job == null
                || job.getRecruiter() == null
                || !job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only view profiles for applicants to your own jobs."
            );
        }

        User applicant = application.getUser();
        return profileService.getRecruiterProfile(
                applicant.getId(),
                applicant.getName());
    }

    public List<ApplicationResponseDTO> getApplicationsForJobSeeker(
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + email
                        )
                );

        return applicationRepository.findByUserId(user.getId())
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public List<ApplicationResponseDTO> getApplicationsForRecruiter(
            String email) {

        User recruiter = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Recruiter not found with email: " + email
                        )
                );

        return applicationRepository
                .findByJobRecruiterId(recruiter.getId())
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional
    public ApplicationResponseDTO updateApplicationStatus(
            Long id,
            ApplicationStatusRequestDTO request,
            String email) {

        User recruiter = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Recruiter not found with email: " + email
                        )
                );

        Application application = applicationRepository.findById(id)
                .orElseThrow(() ->
                        new ApplicationNotFoundException(
                                "Application not found with id: " + id
                        )
                );
        if (application.getJob().getRecruiter() == null
                || !application.getJob().getRecruiter().getId()
                .equals(recruiter.getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "You can only update applications for your own jobs."
            );
        }

        application.setStatus(
                request.getStatus()
        );

        Application updatedApplication =
                applicationRepository.save(application);

        return convertToResponseDTO(updatedApplication);
    }

    private ApplicationResponseDTO convertToResponseDTO(
            Application application) {

        ApplicationResponseDTO response = new ApplicationResponseDTO(
                application.getId(),
                application.getUser().getId(),
                application.getJob().getId(),
                application.getStatus().name(),
                application.getUser().getName(),
                application.getUser().getEmail()
        );

        Job job = application.getJob();
        if (job != null) {
            response.setJobAvailable(true);
            response.setJobTitle(job.getTitle());
            response.setJobLocation(job.getLocation());
            response.setSalary(job.getSalary());
            response.setExperience(job.getExperience());
            response.setEmploymentType(job.getEmploymentType());
            if (job.getRecruiter() != null) {
                response.setRecruiterName(job.getRecruiter().getName());
            }
        }
        return response;
    }
}