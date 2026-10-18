package com.atif.jobportal.repository;

import com.atif.jobportal.job.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByRecruiterId(Long recruiterId);

    boolean existsByIdAndRecruiterId(Long id, Long recruiterId);
}