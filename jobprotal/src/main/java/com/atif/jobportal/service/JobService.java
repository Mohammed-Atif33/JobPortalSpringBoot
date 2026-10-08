package com.atif.jobportal.service;

import com.atif.jobportal.dto.JobRequestDTO;
import com.atif.jobportal.dto.JobResponseDTO;
import com.atif.jobportal.exception.JobNotFoundException;
import com.atif.jobportal.job.Job;
import com.atif.jobportal.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public JobResponseDTO createJob(JobRequestDTO request) {

        Job job = new Job();

        updateJobFields(job, request);

        Job savedJob = jobRepository.save(job);

        return convertToResponseDTO(savedJob);
    }

    public List<JobResponseDTO> getAllJobs() {

        return jobRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public JobResponseDTO getJobById(Long id) {

        Job job = findJobById(id);

        return convertToResponseDTO(job);
    }

    public JobResponseDTO updateJob(
            Long id,
            JobRequestDTO request) {

        Job job = findJobById(id);

        updateJobFields(job, request);

        Job updatedJob = jobRepository.save(job);

        return convertToResponseDTO(updatedJob);
    }

    public void deleteJob(Long id) {

        Job job = findJobById(id);

        jobRepository.delete(job);
    }

    private Job findJobById(Long id) {

        return jobRepository.findById(id)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found with id: " + id
                        )
                );
    }

    private void updateJobFields(
            Job job,
            JobRequestDTO request) {

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setExperience(request.getExperience());
    }

    private JobResponseDTO convertToResponseDTO(Job job) {

        return new JobResponseDTO(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getSalary(),
                job.getExperience()
        );
    }
}