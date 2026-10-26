package com.atif.jobportal.dto;

import java.util.List;

public record RecruiterCandidateProfileResponseDTO(
        boolean exists,
        String fullName,
        String professionalSummary,
        List<String> skills,
        List<Education> education,
        List<Experience> experience,
        List<Project> projects) {

    public record Education(
            String institution,
            String qualification,
            String fieldOfStudy,
            String startDate,
            String endDate) {
    }

    public record Experience(
            String employer,
            String jobTitle,
            String description,
            String startDate,
            String endDate) {
    }

    public record Project(
            String name,
            String description,
            String technologies) {
    }
}
