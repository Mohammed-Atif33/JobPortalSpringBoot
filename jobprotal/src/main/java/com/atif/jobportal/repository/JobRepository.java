package com.atif.jobportal.repository;

import com.atif.jobportal.job.Job;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    @Override
    @EntityGraph(attributePaths = "recruiter")
    List<Job> findAll();

    List<Job> findByRecruiterId(Long recruiterId);

    boolean existsByIdAndRecruiterId(Long id, Long recruiterId);

    @Query("""
            SELECT job
            FROM Job job
            LEFT JOIN FETCH job.recruiter recruiter
            WHERE LOWER(COALESCE(job.title, '')) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(COALESCE(job.location, '')) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(COALESCE(recruiter.name, '')) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    List<Job> searchAvailableJobs(@Param("query") String query);
}