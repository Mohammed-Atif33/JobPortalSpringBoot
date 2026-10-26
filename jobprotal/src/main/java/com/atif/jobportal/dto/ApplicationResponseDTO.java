package com.atif.jobportal.dto;

public class ApplicationResponseDTO {

    private String applicantEmail;
    private String applicantName;
    private Long id;
    private Long userId;
    private Long jobId;
    private String status;
    private String jobTitle;
    private String jobLocation;
    private Double salary;
    private Integer experience;
    private String recruiterName;
    private boolean jobAvailable;

    public ApplicationResponseDTO() {
    }

    public ApplicationResponseDTO(
            Long id,
            Long userId,
            Long jobId,
            String status,
            String applicantName,
            String applicantEmail) {

        this.id = id;
        this.userId = userId;
        this.jobId = jobId;
        this.status = status;
        this.applicantName = applicantName;
        this.applicantEmail = applicantEmail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobLocation() {
        return jobLocation;
    }

    public void setJobLocation(String jobLocation) {
        this.jobLocation = jobLocation;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public String getRecruiterName() {
        return recruiterName;
    }

    public void setRecruiterName(String recruiterName) {
        this.recruiterName = recruiterName;
    }

    public boolean isJobAvailable() {
        return jobAvailable;
    }

    public void setJobAvailable(boolean jobAvailable) {
        this.jobAvailable = jobAvailable;
    }

}