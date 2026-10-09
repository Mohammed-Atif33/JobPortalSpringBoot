package com.atif.jobportal.repository;

import com.atif.jobportal.application.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository
        extends JpaRepository<Application, Long> {
}