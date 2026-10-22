package com.atif.jobportal.service;

import com.atif.jobportal.application.Application;
import com.atif.jobportal.dto.ApplicationRequestDTO;
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
        when(applicationRepository.findById(3L))
                .thenReturn(Optional.of(application));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> applicationService.getApplicationById(
                        3L, "recruiter@example.com")
        );

        assertEquals(403, exception.getStatusCode().value());
    }
}