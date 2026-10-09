package com.atif.jobportal.dto;

public class ApplicationResponseDTO {

    private Long id;
    private Long userId;
    private Long jobId;
    private String status;

    public ApplicationResponseDTO() {
    }

    public ApplicationResponseDTO(
            Long id,
            Long userId,
            Long jobId,
            String status) {

        this.id = id;
        this.userId = userId;
        this.jobId = jobId;
        this.status = status;
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
}