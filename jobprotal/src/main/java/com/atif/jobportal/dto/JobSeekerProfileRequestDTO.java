package com.atif.jobportal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class JobSeekerProfileRequestDTO {

    @Size(max = 30, message = "Phone number must be at most 30 characters")
    @Pattern(
            regexp = "^$|^[+()0-9 .-]{7,30}$",
            message = "Enter a valid phone number")
    private String phoneNumber = "";

    @Size(max = 5000, message = "Professional summary must be at most 5000 characters")
    private String professionalSummary = "";

    @Size(max = 30, message = "You can add at most 30 skills")
    private List<@NotNull @NotBlank(message = "Skill cannot be blank")
            @Size(max = 80, message = "Skill must be at most 80 characters")
            String> skills = new ArrayList<>();

    @Size(max = 20, message = "You can add at most 20 education entries")
    private List<@NotNull @Valid EducationDTO> education = new ArrayList<>();

    @Size(max = 20, message = "You can add at most 20 experience entries")
    private List<@NotNull @Valid ExperienceDTO> experience = new ArrayList<>();

    @Size(max = 20, message = "You can add at most 20 projects")
    private List<@NotNull @Valid ProjectDTO> projects = new ArrayList<>();

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfessionalSummary() {
        return professionalSummary;
    }

    public void setProfessionalSummary(String professionalSummary) {
        this.professionalSummary = professionalSummary;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<EducationDTO> getEducation() {
        return education;
    }

    public void setEducation(List<EducationDTO> education) {
        this.education = education;
    }

    public List<ExperienceDTO> getExperience() {
        return experience;
    }

    public void setExperience(List<ExperienceDTO> experience) {
        this.experience = experience;
    }

    public List<ProjectDTO> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectDTO> projects) {
        this.projects = projects;
    }

    public static class EducationDTO {

        @NotBlank(message = "Institution is required")
        @Size(max = 160, message = "Institution must be at most 160 characters")
        private String institution;

        @NotBlank(message = "Qualification is required")
        @Size(max = 120, message = "Qualification must be at most 120 characters")
        private String qualification;

        @Size(max = 120, message = "Field of study must be at most 120 characters")
        private String fieldOfStudy = "";

        @Size(max = 20, message = "Start date must be at most 20 characters")
        private String startDate = "";

        @Size(max = 20, message = "End date must be at most 20 characters")
        private String endDate = "";

        public String getInstitution() {
            return institution;
        }

        public void setInstitution(String institution) {
            this.institution = institution;
        }

        public String getQualification() {
            return qualification;
        }

        public void setQualification(String qualification) {
            this.qualification = qualification;
        }

        public String getFieldOfStudy() {
            return fieldOfStudy;
        }

        public void setFieldOfStudy(String fieldOfStudy) {
            this.fieldOfStudy = fieldOfStudy;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }

    public static class ExperienceDTO {

        @NotBlank(message = "Employer is required")
        @Size(max = 160, message = "Employer must be at most 160 characters")
        private String employer;

        @NotBlank(message = "Job title is required")
        @Size(max = 120, message = "Job title must be at most 120 characters")
        private String jobTitle;

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        private String description = "";

        @Size(max = 20, message = "Start date must be at most 20 characters")
        private String startDate = "";

        @Size(max = 20, message = "End date must be at most 20 characters")
        private String endDate = "";

        public String getEmployer() {
            return employer;
        }

        public void setEmployer(String employer) {
            this.employer = employer;
        }

        public String getJobTitle() {
            return jobTitle;
        }

        public void setJobTitle(String jobTitle) {
            this.jobTitle = jobTitle;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }

    public static class ProjectDTO {

        @NotBlank(message = "Project name is required")
        @Size(max = 160, message = "Project name must be at most 160 characters")
        private String name;

        @NotBlank(message = "Project description is required")
        @Size(max = 2000, message = "Project description must be at most 2000 characters")
        private String description;

        @Size(max = 1000, message = "Technologies must be at most 1000 characters")
        private String technologies = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getTechnologies() {
            return technologies;
        }

        public void setTechnologies(String technologies) {
            this.technologies = technologies;
        }
    }
}
