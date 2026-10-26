package com.atif.jobportal.controller;

import com.atif.jobportal.dto.JobSeekerProfileRequestDTO;
import com.atif.jobportal.dto.JobSeekerProfileResponseDTO;
import com.atif.jobportal.service.JobSeekerProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/job-seeker/profile")
public class JobSeekerProfileController {

    private final JobSeekerProfileService profileService;

    public JobSeekerProfileController(JobSeekerProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public JobSeekerProfileResponseDTO getProfile(
            Authentication authentication) {
        return profileService.getProfile(authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobSeekerProfileResponseDTO createProfile(
            @Valid @RequestBody JobSeekerProfileRequestDTO request,
            Authentication authentication) {
        return profileService.createProfile(authentication.getName(), request);
    }

    @PutMapping
    public JobSeekerProfileResponseDTO updateProfile(
            @Valid @RequestBody JobSeekerProfileRequestDTO request,
            Authentication authentication) {
        return profileService.updateProfile(authentication.getName(), request);
    }
}
