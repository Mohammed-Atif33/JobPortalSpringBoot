package com.atif.jobportal.service;

import com.atif.jobportal.dto.JobSeekerProfileRequestDTO;
import com.atif.jobportal.entity.JobSeekerProfile;
import com.atif.jobportal.entity.ProfileEducation;
import com.atif.jobportal.entity.User;
import com.atif.jobportal.repository.JobSeekerProfileRepository;
import com.atif.jobportal.repository.UserRepository;
import com.atif.jobportal.security.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobSeekerProfileServiceTest {

    @Mock
    private JobSeekerProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    private JobSeekerProfileService profileService;
    private User jobSeeker;

    @BeforeEach
    void setUp() {
        profileService = new JobSeekerProfileService(
                profileRepository,
                userRepository);
        jobSeeker = new User(
                "Taylor Jobseeker",
                "taylor@example.com",
                "encoded-password",
                UserRole.JOB_SEEKER.name());
        jobSeeker.setId(12L);
        when(userRepository.findByEmailIgnoreCase("taylor@example.com"))
                .thenReturn(Optional.of(jobSeeker));
    }

    @Test
    void returnsAccountNameAndEmptyProfileWhenProfileDoesNotExist() {
        when(profileRepository.findByUserId(12L)).thenReturn(Optional.empty());

        var response = profileService.getProfile("taylor@example.com");

        assertFalse(response.exists());
        assertEquals("Taylor Jobseeker", response.fullName());
        assertTrue(response.skills().isEmpty());
    }

    @Test
    void createsProfileForAuthenticatedJobSeeker() {
        when(profileRepository.findByUserId(12L)).thenReturn(Optional.empty());
        when(profileRepository.save(any(JobSeekerProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        JobSeekerProfileRequestDTO request = new JobSeekerProfileRequestDTO();
        request.setPhoneNumber("+1 555 010 1234");
        request.setProfessionalSummary("Backend developer");
        request.setSkills(java.util.List.of(" Java ", "Java", "Spring Boot"));

        var response = profileService.createProfile(
                "taylor@example.com",
                request);

        assertTrue(response.exists());
        assertEquals("+1 555 010 1234", response.phoneNumber());
        assertEquals(java.util.List.of("Java", "Spring Boot"), response.skills());
        verify(profileRepository).save(any(JobSeekerProfile.class));
    }

    @Test
    void updatesOnlyTheProfileBelongingToAuthenticatedAccount() {
        JobSeekerProfile profile = new JobSeekerProfile();
        profile.setUser(jobSeeker);
        profile.getEducation().add(
                new ProfileEducation("Old School", "Degree", "", "", ""));
        when(profileRepository.findByUserId(12L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(profile)).thenReturn(profile);
        JobSeekerProfileRequestDTO request = new JobSeekerProfileRequestDTO();
        request.setPhoneNumber("5550101234");

        var response = profileService.updateProfile(
                "taylor@example.com",
                request);

        assertTrue(response.exists());
        assertEquals("5550101234", response.phoneNumber());
        assertTrue(response.education().isEmpty());
        assertEquals(jobSeeker, profile.getUser());
        verify(profileRepository).save(profile);
    }

    @Test
    void rejectsRecruiterEvenIfServiceIsCalledDirectly() {
        jobSeeker.setRole(UserRole.RECRUITER.name());

        assertThrows(
                AccessDeniedException.class,
                () -> profileService.getProfile("taylor@example.com"));
        verify(profileRepository, never()).findByUserId(12L);
    }
}
