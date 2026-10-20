package com.atif.jobportal.repository;

import com.atif.jobportal.application.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository
        extends JpaRepository<Application, Long> {

    long countByJobId(Long jobId);
    List<Application> findByJobRecruiterId(Long recruiterId);
}