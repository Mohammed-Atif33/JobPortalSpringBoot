package com.atif.jobportal.controller;

import com.atif.jobportal.dto.ApplicationRequestDTO;
import com.atif.jobportal.dto.ApplicationResponseDTO;
import com.atif.jobportal.dto.ApplicationStatusRequestDTO;
import com.atif.jobportal.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @Valid @RequestBody ApplicationRequestDTO request) {

        ApplicationResponseDTO response =
                applicationService.createApplication(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponseDTO>> getAllApplications() {

        List<ApplicationResponseDTO> applications =
                applicationService.getAllApplications();

        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponseDTO> getApplicationById(
            @PathVariable Long id) {

        ApplicationResponseDTO response =
                applicationService.getApplicationById(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApplicationResponseDTO> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusRequestDTO request) {

        ApplicationResponseDTO response =
                applicationService.updateApplicationStatus(id, request);

        return ResponseEntity.ok(response);
    }
}