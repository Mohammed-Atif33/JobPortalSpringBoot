package com.atif.jobportal.repository;

import com.atif.jobportal.job.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
}