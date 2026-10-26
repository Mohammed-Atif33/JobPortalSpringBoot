package com.atif.jobportal.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class ProfileEducation {

    private String institution;
    private String qualification;
    private String fieldOfStudy;
    private String startDate;
    private String endDate;

    protected ProfileEducation() {
    }

    public ProfileEducation(
            String institution,
            String qualification,
            String fieldOfStudy,
            String startDate,
            String endDate) {
        this.institution = institution;
        this.qualification = qualification;
        this.fieldOfStudy = fieldOfStudy;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getInstitution() {
        return institution;
    }

    public String getQualification() {
        return qualification;
    }

    public String getFieldOfStudy() {
        return fieldOfStudy;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
