package com.atif.jobportal.dto;

import com.atif.jobportal.application.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public class ApplicationStatusRequestDTO {

    @NotNull(message = "Status cannot be null")
    private ApplicationStatus status;

    public ApplicationStatusRequestDTO() {
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}