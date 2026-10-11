package com.atif.jobportal.dto;

import jakarta.validation.constraints.NotNull;

public class ApplicationRequestDTO {

    @NotNull(message = "Job ID is required")
    private Long jobId;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }
}