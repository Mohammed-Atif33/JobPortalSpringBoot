package com.atif.jobportal.service;

import com.atif.jobportal.dto.JobSeekerProfileRequestDTO;
import com.atif.jobportal.dto.JobSeekerProfileResponseDTO;
import com.atif.jobportal.entity.JobSeekerProfile;
import com.atif.jobportal.entity.ProfileEducation;
import com.atif.jobportal.entity.ProfileExperience;
import com.atif.jobportal.entity.ProfileProject;
import com.atif.jobportal.entity.User;
import com.atif.jobportal.exception.JobSeekerProfileNotFoundException;
import com.atif.jobportal.exception.UserNotFoundException;
import com.atif.jobportal.repository.JobSeekerProfileRepository;
import com.atif.jobportal.repository.UserRepository;
import com.atif.jobportal.security.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class JobSeekerProfileService {

    private final JobSeekerProfileRepository profileRepository;
    private final UserRepository userRepository;

    public JobSeekerProfileService(
            JobSeekerProfileRepository profileRepository,
            UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public JobSeekerProfileResponseDTO getProfile(String email) {
        User user = findJobSeeker(email);
        return profileRepository.findByUserId(user.getId())
                .map(profile -> toResponse(profile, true))
                .orElseGet(() -> emptyProfile(user));
    }

    @Transactional
    public JobSeekerProfileResponseDTO createProfile(
            String email,
            JobSeekerProfileRequestDTO request) {
        User user = findJobSeeker(email);
        if (profileRepository.findByUserId(user.getId()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A profile already exists. Update the existing profile instead.");
        }

        JobSeekerProfile profile = new JobSeekerProfile();
        profile.setUser(user);
        applyRequest(profile, request);
        return toResponse(profileRepository.save(profile), true);
    }

    @Transactional
    public JobSeekerProfileResponseDTO updateProfile(
            String email,
            JobSeekerProfileRequestDTO request) {
        User user = findJobSeeker(email);
        JobSeekerProfile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new JobSeekerProfileNotFoundException(
                        "Create your profile before updating it."));
        applyRequest(profile, request);
        return toResponse(profileRepository.save(profile), true);
    }

    private User findJobSeeker(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "Authenticated user account was not found."));
        if (!Objects.equals(user.getRole(), UserRole.JOB_SEEKER.name())) {
            throw new AccessDeniedException(
                    "Only job seekers can manage a job seeker profile.");
        }
        return user;
    }

    private void applyRequest(
            JobSeekerProfile profile,
            JobSeekerProfileRequestDTO request) {
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setProfessionalSummary(request.getProfessionalSummary());
        profile.getSkills().clear();
        if (request.getSkills() != null) {
            profile.getSkills().addAll(request.getSkills().stream()
                    .map(String::trim)
                    .filter(skill -> !skill.isEmpty())
                    .distinct()
                    .toList());
        }

        profile.getEducation().clear();
        if (request.getEducation() != null) {
            profile.getEducation().addAll(request.getEducation().stream()
                    .map(entry -> new ProfileEducation(
                            entry.getInstitution().trim(),
                            entry.getQualification().trim(),
                            clean(entry.getFieldOfStudy()),
                            clean(entry.getStartDate()),
                            clean(entry.getEndDate())))
                    .toList());
        }

        profile.getExperience().clear();
        if (request.getExperience() != null) {
            profile.getExperience().addAll(request.getExperience().stream()
                    .map(entry -> new ProfileExperience(
                            entry.getEmployer().trim(),
                            entry.getJobTitle().trim(),
                            clean(entry.getDescription()),
                            clean(entry.getStartDate()),
                            clean(entry.getEndDate())))
                    .toList());
        }

        profile.getProjects().clear();
        if (request.getProjects() != null) {
            profile.getProjects().addAll(request.getProjects().stream()
                    .map(entry -> new ProfileProject(
                            entry.getName().trim(),
                            entry.getDescription().trim(),
                            clean(entry.getTechnologies())))
                    .toList());
        }
    }

    private JobSeekerProfileResponseDTO toResponse(
            JobSeekerProfile profile,
            boolean exists) {
        return new JobSeekerProfileResponseDTO(
                exists,
                profile.getUser().getName(),
                profile.getPhoneNumber(),
                profile.getProfessionalSummary(),
                List.copyOf(profile.getSkills()),
                profile.getEducation().stream()
                        .map(entry -> {
                            var dto = new JobSeekerProfileRequestDTO.EducationDTO();
                            dto.setInstitution(entry.getInstitution());
                            dto.setQualification(entry.getQualification());
                            dto.setFieldOfStudy(entry.getFieldOfStudy());
                            dto.setStartDate(entry.getStartDate());
                            dto.setEndDate(entry.getEndDate());
                            return dto;
                        })
                        .toList(),
                profile.getExperience().stream()
                        .map(entry -> {
                            var dto = new JobSeekerProfileRequestDTO.ExperienceDTO();
                            dto.setEmployer(entry.getEmployer());
                            dto.setJobTitle(entry.getJobTitle());
                            dto.setDescription(entry.getDescription());
                            dto.setStartDate(entry.getStartDate());
                            dto.setEndDate(entry.getEndDate());
                            return dto;
                        })
                        .toList(),
                profile.getProjects().stream()
                        .map(entry -> {
                            var dto = new JobSeekerProfileRequestDTO.ProjectDTO();
                            dto.setName(entry.getName());
                            dto.setDescription(entry.getDescription());
                            dto.setTechnologies(entry.getTechnologies());
                            return dto;
                        })
                        .toList());
    }

    private JobSeekerProfileResponseDTO emptyProfile(User user) {
        return new JobSeekerProfileResponseDTO(
                false,
                user.getName(),
                "",
                "",
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
