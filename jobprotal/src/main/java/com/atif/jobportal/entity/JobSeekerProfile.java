package com.atif.jobportal.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "job_seeker_profiles",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_job_seeker_profile_user",
                columnNames = "user_id"))
public class JobSeekerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 30)
    private String phoneNumber;

    @Column(length = 5000)
    private String professionalSummary;

    @ElementCollection
    @CollectionTable(
            name = "job_seeker_profile_skills",
            joinColumns = @JoinColumn(name = "profile_id"))
    @OrderColumn(name = "display_order")
    @Column(name = "skill", nullable = false, length = 80)
    private List<String> skills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "job_seeker_profile_education",
            joinColumns = @JoinColumn(name = "profile_id"))
    @OrderColumn(name = "display_order")
    @Embedded
    private List<ProfileEducation> education = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "job_seeker_profile_experience",
            joinColumns = @JoinColumn(name = "profile_id"))
    @OrderColumn(name = "display_order")
    @Embedded
    private List<ProfileExperience> experience = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "job_seeker_profile_projects",
            joinColumns = @JoinColumn(name = "profile_id"))
    @OrderColumn(name = "display_order")
    @Embedded
    private List<ProfileProject> projects = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

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

    public List<ProfileEducation> getEducation() {
        return education;
    }

    public void setEducation(List<ProfileEducation> education) {
        this.education = education;
    }

    public List<ProfileExperience> getExperience() {
        return experience;
    }

    public void setExperience(List<ProfileExperience> experience) {
        this.experience = experience;
    }

    public List<ProfileProject> getProjects() {
        return projects;
    }

    public void setProjects(List<ProfileProject> projects) {
        this.projects = projects;
    }
}
