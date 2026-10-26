package com.atif.jobportal.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class ProfileProject {

    private String name;

    @jakarta.persistence.Column(length = 2000)
    private String description;

    @jakarta.persistence.Column(length = 1000)
    private String technologies;

    protected ProfileProject() {
    }

    public ProfileProject(
            String name,
            String description,
            String technologies) {
        this.name = name;
        this.description = description;
        this.technologies = technologies;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTechnologies() {
        return technologies;
    }
}
