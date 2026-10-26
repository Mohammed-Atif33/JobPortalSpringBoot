package com.atif.jobportal.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;

@Embeddable
public class ProfileExperience {

    private String employer;
    private String jobTitle;
    @Column(length = 2000)
    private String description;
    private String startDate;
    private String endDate;

    protected ProfileExperience() {
    }

    public ProfileExperience(
            String employer,
            String jobTitle,
            String description,
            String startDate,
            String endDate) {
        this.employer = employer;
        this.jobTitle = jobTitle;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getEmployer() {
        return employer;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getDescription() {
        return description;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
