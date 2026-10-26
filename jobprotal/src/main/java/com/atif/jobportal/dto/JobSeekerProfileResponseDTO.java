package com.atif.jobportal.dto;

import java.util.List;

public record JobSeekerProfileResponseDTO(
        boolean exists,
        String fullName,
        String phoneNumber,
        String professionalSummary,
        List<String> skills,
        List<JobSeekerProfileRequestDTO.EducationDTO> education,
        List<JobSeekerProfileRequestDTO.ExperienceDTO> experience,
        List<JobSeekerProfileRequestDTO.ProjectDTO> projects) {
}
