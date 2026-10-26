package com.atif.jobportal.service;

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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private JobService jobService;

    @Test
    void emptySearchReturnsAllJobs() {
        User recruiter = new User("CareerNest", "recruiter@example.com", "", "RECRUITER");
        Job job = new Job("Java Developer", "Build services", "Pune", 800000.0, 2);
        job.setRecruiter(recruiter);
        when(jobRepository.findAll()).thenReturn(List.of(job));

        var results = jobService.getAllJobs("  ");

        assertEquals(1, results.size());
        assertEquals("CareerNest", results.getFirst().getRecruiterName());
        verify(jobRepository).findAll();
        verify(jobRepository, never()).searchAvailableJobs(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void nonEmptySearchUsesSearchRepositoryQuery() {
        when(jobRepository.searchAvailableJobs("Pune"))
                .thenReturn(List.of());

        var results = jobService.getAllJobs(" Pune ");

        assertEquals(List.of(), results);
        verify(jobRepository).searchAvailableJobs("Pune");
        verify(jobRepository, never()).findAll();
    }
}
