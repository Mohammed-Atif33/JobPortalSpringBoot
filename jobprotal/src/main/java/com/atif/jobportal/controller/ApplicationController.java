package com.atif.jobportal.controller;

import com.atif.jobportal.dto.ApplicationRequestDTO;
import com.atif.jobportal.dto.ApplicationResponseDTO;
import com.atif.jobportal.dto.ApplicationStatusRequestDTO;
import com.atif.jobportal.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(
            ApplicationService applicationService) {

        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponseDTO> createApplication(
            @Valid @RequestBody ApplicationRequestDTO request,
            Authentication authentication) {

        String email = authentication.getName();

        ApplicationResponseDTO response =
                applicationService.createApplication(request, email);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public List<ApplicationResponseDTO> getAllApplications(
            Authentication authentication) {
        return applicationService.getApplicationsForRecruiter(
                authentication.getName()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponseDTO> getApplicationById(
            @PathVariable Long id) {

        ApplicationResponseDTO response =
                applicationService.getApplicationById(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ApplicationResponseDTO updateApplicationStatus(
            @PathVariable Long id,
            @RequestBody ApplicationStatusRequestDTO request,
            Authentication authentication) {

        return applicationService.updateApplicationStatus(
                id,
                request,
                authentication.getName()
        );
    }
}