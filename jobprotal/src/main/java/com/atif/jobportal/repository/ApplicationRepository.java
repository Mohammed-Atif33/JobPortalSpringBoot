package com.atif.jobportal.repository;

import com.atif.jobportal.application.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository
        extends JpaRepository<Application, Long> {

    long countByJobId(Long jobId);

    @EntityGraph(attributePaths = {"user", "job", "job.recruiter"})
    List<Application> findByJobRecruiterId(Long recruiterId);

    @EntityGraph(attributePaths = {"user", "job", "job.recruiter"})
    List<Application> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "job", "job.recruiter"})
    Optional<Application> findWithApplicantAndJobById(Long id);

    boolean existsByUserIdAndJobId(Long userId, Long jobId);
}