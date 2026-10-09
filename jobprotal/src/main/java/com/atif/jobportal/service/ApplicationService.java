package com.atif.jobportal.service;

import com.atif.jobportal.application.Application;
import com.atif.jobportal.application.ApplicationStatus;
import com.atif.jobportal.dto.ApplicationRequestDTO;
import com.atif.jobportal.dto.ApplicationResponseDTO;
import com.atif.jobportal.dto.ApplicationStatusRequestDTO;
import com.atif.jobportal.entity.User;
import com.atif.jobportal.exception.ApplicationNotFoundException;
import com.atif.jobportal.exception.UserNotFoundException;
import com.atif.jobportal.exception.JobNotFoundException;
import com.atif.jobportal.job.Job;
import com.atif.jobportal.repository.ApplicationRepository;
import com.atif.jobportal.repository.JobRepository;
import com.atif.jobportal.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            UserRepository userRepository,
            JobRepository jobRepository) {

        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

    public ApplicationResponseDTO createApplication(
            ApplicationRequestDTO request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: "
                                        + request.getUserId()
                        )
                );

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found with id: "
                                        + request.getJobId()
                        )
                );

        Application application = new Application();

        application.setUser(user);
        application.setJob(job);

        // New application always starts with APPLIED status
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

    public ApplicationResponseDTO getApplicationById(Long id) {

        Application application = applicationRepository.findById(id)
                .orElseThrow(() ->
                        new ApplicationNotFoundException(
                                "Application not found with id: " + id
                        )
                );

        return convertToResponseDTO(application);
    }

    public ApplicationResponseDTO updateApplicationStatus(
            Long id,
            ApplicationStatusRequestDTO request) {

        Application application = applicationRepository.findById(id)
                .orElseThrow(() ->
                        new ApplicationNotFoundException(
                                "Application not found with id: " + id
                        )
                );

        application.setStatus(request.getStatus());

        Application updatedApplication =
                applicationRepository.save(application);

        return convertToResponseDTO(updatedApplication);
    }

    private ApplicationResponseDTO convertToResponseDTO(
            Application application) {

        return new ApplicationResponseDTO(
                application.getId(),
                application.getUser().getId(),
                application.getJob().getId(),
                application.getStatus().name()
        );
    }
}