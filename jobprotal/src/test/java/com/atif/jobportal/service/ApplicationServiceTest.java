package com.atif.jobportal.service;

import com.atif.jobportal.application.Application;
import com.atif.jobportal.application.ApplicationStatus;
import com.atif.jobportal.dto.ApplicationRequestDTO;
import com.atif.jobportal.dto.ApplicationResponseDTO;
import com.atif.jobportal.entity.User;
import com.atif.jobportal.job.Job;
import com.atif.jobportal.repository.ApplicationRepository;
import com.atif.jobportal.repository.JobRepository;
import com.atif.jobportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void rejectsDuplicateApplications() {
        User applicant = new User();
        applicant.setId(7L);
        Job job = new Job();
        job.setId(12L);
        ApplicationRequestDTO request = new ApplicationRequestDTO();
        request.setJobId(12L);

        when(userRepository.findByEmail("seeker@example.com"))
                .thenReturn(Optional.of(applicant));
        when(jobRepository.findById(12L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByUserIdAndJobId(7L, 12L))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> applicationService.createApplication(
                        request, "seeker@example.com")
        );

        assertEquals(409, exception.getStatusCode().value());
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void preventsRecruiterFromReadingAnotherRecruitersApplication() {
        User recruiter = new User();
        recruiter.setId(20L);
        User otherRecruiter = new User();
        otherRecruiter.setId(21L);
        Job job = new Job();
        job.setId(12L);
        job.setRecruiter(otherRecruiter);
        Application application = new Application();
        application.setJob(job);

        when(userRepository.findByEmail("recruiter@example.com"))
                .thenReturn(Optional.of(recruiter));
        when(applicationRepository.findWithApplicantAndJobById(3L))
                .thenReturn(Optional.of(application));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> applicationService.getApplicationById(
                        3L, "recruiter@example.com")
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void jobSeekerGetsOnlyApplicationsScopedToAuthenticatedUser() {
        User applicant = new User(
                "Job Seeker",
                "seeker@example.com",
                "encoded",
                "JOB_SEEKER");
        applicant.setId(7L);
        User recruiter = new User(
                "Recruiter",
                "recruiter@example.com",
                "encoded",
                "RECRUITER");
        Job job = new Job("Java Developer", "Backend role", "Pune", 600000.0, 2);
        job.setId(12L);
        job.setRecruiter(recruiter);
        Application application = new Application(
                applicant,
                job,
                ApplicationStatus.APPLIED);
        application.setId(4L);

        when(userRepository.findByEmail("seeker@example.com"))
                .thenReturn(Optional.of(applicant));
        when(applicationRepository.findByUserId(7L))
                .thenReturn(java.util.List.of(application));

        java.util.List<ApplicationResponseDTO> results =
                applicationService.getApplicationsForJobSeeker(
                        "seeker@example.com");

        assertEquals(1, results.size());
        ApplicationResponseDTO response = results.getFirst();
        assertEquals(7L, response.getUserId());
        assertEquals(12L, response.getJobId());
        assertEquals("Java Developer", response.getJobTitle());
        assertEquals("Pune", response.getJobLocation());
        assertEquals(600000.0, response.getSalary());
        assertEquals(2, response.getExperience());
        assertEquals("Recruiter", response.getRecruiterName());
        assertEquals("APPLIED", response.getStatus());
        assertEquals(true, response.isJobAvailable());
        verify(applicationRepository).findByUserId(7L);
    }

    @Test
    void responseHandlesMissingOptionalJobDetails() {
        User applicant = new User(
                "Job Seeker",
                "seeker@example.com",
                "encoded",
                "JOB_SEEKER");
        applicant.setId(7L);
        Job job = new Job();
        job.setId(12L);
        Application application = new Application(
                applicant,
                job,
                ApplicationStatus.SHORTLISTED);

        when(userRepository.findByEmail("seeker@example.com"))
                .thenReturn(Optional.of(applicant));
        when(applicationRepository.findByUserId(7L))
                .thenReturn(java.util.List.of(application));

        ApplicationResponseDTO response = applicationService
                .getApplicationsForJobSeeker("seeker@example.com")
                .getFirst();

        assertEquals(true, response.isJobAvailable());
        assertEquals(null, response.getJobTitle());
        assertEquals(null, response.getJobLocation());
        assertEquals(null, response.getSalary());
        assertEquals(null, response.getExperience());
        assertEquals(null, response.getRecruiterName());
        assertEquals("SHORTLISTED", response.getStatus());
    }
}