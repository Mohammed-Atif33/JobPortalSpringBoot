package com.atif.jobportal.seed;

import com.atif.jobportal.application.Application;
import com.atif.jobportal.application.ApplicationStatus;
import com.atif.jobportal.entity.JobSeekerProfile;
import com.atif.jobportal.entity.ProfileEducation;
import com.atif.jobportal.entity.ProfileExperience;
import com.atif.jobportal.entity.ProfileProject;
import com.atif.jobportal.entity.User;
import com.atif.jobportal.job.EmploymentType;
import com.atif.jobportal.job.Job;
import com.atif.jobportal.repository.ApplicationRepository;
import com.atif.jobportal.repository.JobRepository;
import com.atif.jobportal.repository.JobSeekerProfileRepository;
import com.atif.jobportal.repository.OAuthAccountRepository;
import com.atif.jobportal.repository.OAuthLoginCodeRepository;
import com.atif.jobportal.repository.UserRepository;
import com.atif.jobportal.security.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Profile("dev & demo-seed")
public class DemoDataSeedService {

    private static final List<RecruiterSeed> RECRUITERS = List.of(
            new RecruiterSeed("Aarav Mehta", "aarav.mehta@careernest.demo"),
            new RecruiterSeed("Maya Iyer", "maya.iyer@careernest.demo")
    );

    private static final List<SeekerSeed> SEEKERS = List.of(
            new SeekerSeed(
                    "Ananya Rao", "ananya.rao@careernest.demo", "+91-90000-10001",
                    "Computer science graduate focused on Java services, REST APIs, and reliable backend systems.",
                    List.of("Java", "Spring Boot", "REST APIs", "MySQL", "Git"),
                    new ProfileEducation("PES University", "B.E.", "Computer Science",
                            "2021", "2025"),
                    List.of(new ProfileExperience("Campus Innovation Lab", "Backend Intern",
                            "Built and tested Spring REST endpoints for a student services platform.",
                            "Jan 2025", "Jun 2025")),
                    new ProfileProject("CampusConnect",
                            "A role-based campus event and registration API.",
                            "Java, Spring Boot, MySQL")
            ),
            new SeekerSeed(
                    "Rohan Kulkarni", "rohan.kulkarni@careernest.demo", "+91-90000-10002",
                    "Full-stack developer interested in accessible interfaces backed by maintainable Java APIs.",
                    List.of("Java", "JavaScript", "React", "Spring Boot", "PostgreSQL"),
                    new ProfileEducation("Savitribai Phule Pune University", "B.Sc.",
                            "Information Technology", "2022", "2025"),
                    List.of(),
                    new ProfileProject("LocalLoop",
                            "A community marketplace with search, profiles, and listing moderation.",
                            "React, Java, Spring Boot, PostgreSQL")
            ),
            new SeekerSeed(
                    "Priya Nair", "priya.nair@careernest.demo", "+91-90000-10003",
                    "Python developer with practical experience in data processing, testing, and API integration.",
                    List.of("Python", "Django", "FastAPI", "PostgreSQL", "Pytest"),
                    new ProfileEducation("Cochin University of Science and Technology",
                            "B.Tech", "Information Technology", "2021", "2025"),
                    List.of(new ProfileExperience("BluePeak Analytics", "Software Trainee",
                            "Automated data validation and contributed to internal Python services.",
                            "Jul 2025", "Dec 2025")),
                    new ProfileProject("Transit Insights",
                            "A service that aggregates and summarizes public transit data.",
                            "Python, FastAPI, PostgreSQL")
            ),
            new SeekerSeed(
                    "Ishaan Verma", "ishaan.verma@careernest.demo", "+91-90000-10004",
                    "Early-career software engineer building dependable web applications and learning cloud deployment.",
                    List.of("Java", "Spring Boot", "Docker", "SQL", "JUnit"),
                    new ProfileEducation("Vellore Institute of Technology", "B.Tech",
                            "Computer Science", "2022", "2026"),
                    List.of(),
                    new ProfileProject("StudyPlanner",
                            "A study planning API with reminders and progress summaries.",
                            "Java, Spring Boot, H2, JUnit")
            ),
            new SeekerSeed(
                    "Kavya Shah", "kavya.shah@careernest.demo", "+91-90000-10005",
                    "Java and database developer experienced with schema design, query tuning, and service integration.",
                    List.of("Java", "SQL", "MySQL", "Hibernate", "Spring Data JPA"),
                    new ProfileEducation("Gujarat Technological University", "B.E.",
                            "Information Technology", "2020", "2024"),
                    List.of(new ProfileExperience("FinEdge Systems", "Junior Developer",
                            "Maintained data access services and wrote regression tests for finance workflows.",
                            "Aug 2024", "Present")),
                    new ProfileProject("Inventory Ledger",
                            "A stock movement ledger with audit history and reporting queries.",
                            "Java, Hibernate, MySQL")
            ),
            new SeekerSeed(
                    "Arjun Das", "arjun.das@careernest.demo", "+91-90000-10006",
                    "Python and backend candidate focused on clean APIs, test automation, and practical problem solving.",
                    List.of("Python", "Flask", "FastAPI", "Redis", "GitHub Actions"),
                    new ProfileEducation("Jadavpur University", "B.E.",
                            "Information Technology", "2021", "2025"),
                    List.of(),
                    new ProfileProject("Helpdesk Metrics",
                            "A reporting tool that groups support tickets and tracks response trends.",
                            "Python, Flask, SQLite")
            ),
            new SeekerSeed(
                    "Neha Patil", "neha.patil@careernest.demo", "+91-90000-10007",
                    "Junior backend engineer with project work in Java, authentication, and relational data modeling.",
                    List.of("Java", "Spring Boot", "JWT", "MySQL", "Maven"),
                    new ProfileEducation("MIT World Peace University", "B.Tech",
                            "Computer Engineering", "2022", "2026"),
                    List.of(new ProfileExperience("Open Source Student Group", "Contributor",
                            "Implemented API validation and documentation improvements in a collaborative project.",
                            "Feb 2025", "Present")),
                    new ProfileProject("VolunteerHub",
                            "A volunteer opportunity API with organizer and participant workflows.",
                            "Java, Spring Boot, MySQL")
            )
    );

    private static final List<JobSeed> JOBS = List.of(
            new JobSeed("Java Backend Developer Intern",
                    "Support the backend team by implementing Spring REST endpoints, writing unit tests, and reviewing database queries. Mentorship and a 12-week learning plan are included.",
                    "Bengaluru, Karnataka (Hybrid)", 25000, 0, EmploymentType.INTERNSHIP, 0),
            new JobSeed("Full Stack Developer Intern",
                    "Build responsive application features with a Java API and modern JavaScript UI. Work with a mentor on accessibility, integration tests, and code reviews.",
                    "Pune, Maharashtra", 22000, 0, EmploymentType.INTERNSHIP, 1),
            new JobSeed("Python Developer Intern",
                    "Assist with Python API development, data validation, and automated tests. This 12-week remote internship includes weekly engineering guidance.",
                    "Remote - India", 20000, 0, EmploymentType.INTERNSHIP, 0),
            new JobSeed("Java Backend Developer",
                    "Develop and maintain Java services, design REST APIs, and improve reliability through testing and observability. Requires practical experience with SQL and collaborative delivery.",
                    "Hyderabad, Telangana", 1000000, 2, EmploymentType.FULL_TIME, 1),
            new JobSeed("Spring Boot Developer",
                    "Design production Spring Boot services, integrate relational data stores, and contribute to deployment and code-review practices.",
                    "Bengaluru, Karnataka", 1200000, 3, EmploymentType.FULL_TIME, 0),
            new JobSeed("Full Stack Java Developer",
                    "Deliver end-to-end features using Java, Spring Boot, and JavaScript. Partner with product and design to build accessible workflows and maintain API quality.",
                    "Pune, Maharashtra (Hybrid)", 1100000, 2, EmploymentType.FULL_TIME, 1),
            new JobSeed("Python Backend Developer",
                    "Build Python services and integrations, improve database-backed workflows, and maintain clear automated test coverage.",
                    "Remote - India", 900000, 1, EmploymentType.FULL_TIME, 0),
            new JobSeed("Software Engineer",
                    "Contribute to the design, implementation, and support of customer-facing software. Candidates should be comfortable with APIs, data structures, and team-based development.",
                    "Chennai, Tamil Nadu", 1200000, 2, EmploymentType.FULL_TIME, 1),
            new JobSeed("Database Developer",
                    "Create and tune relational schemas, write maintainable SQL, and support application teams with data access patterns and migration reviews.",
                    "Hyderabad, Telangana (Hybrid)", 900000, 2, EmploymentType.FULL_TIME, 0),
            new JobSeed("Junior Backend Engineer",
                    "Join a supportive backend team to implement tested API features, fix defects, and learn secure service development with Java.",
                    "Remote - India", 600000, 0, EmploymentType.FULL_TIME, 1)
    );

    private static final int[][] APPLICANTS_BY_JOB = {
            {0, 1, 4}, {0, 2, 3}, {1, 5}, {2, 4, 6}, {0, 3, 5},
            {1, 4, 6}, {2, 5}, {0, 3, 6}, {1, 2, 4}, {3, 5, 6}
    };

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final JobSeekerProfileRepository profileRepository;
    private final OAuthAccountRepository oauthAccountRepository;
    private final OAuthLoginCodeRepository oauthLoginCodeRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeedService(
            UserRepository userRepository,
            JobRepository jobRepository,
            ApplicationRepository applicationRepository,
            JobSeekerProfileRepository profileRepository,
            OAuthAccountRepository oauthAccountRepository,
            OAuthLoginCodeRepository oauthLoginCodeRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.profileRepository = profileRepository;
        this.oauthAccountRepository = oauthAccountRepository;
        this.oauthLoginCodeRepository = oauthLoginCodeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SeedResult resetAndSeed() {
        removeExistingRecords();

        List<DemoLogin> logins = new ArrayList<>();
        List<User> recruiters = new ArrayList<>();
        for (RecruiterSeed seed : RECRUITERS) {
            User recruiter = createUser(seed.name(), seed.email(), UserRole.RECRUITER, logins);
            recruiters.add(userRepository.save(recruiter));
        }
        List<User> seekers = new ArrayList<>();
        for (SeekerSeed seed : SEEKERS) {
            User seeker = createUser(seed.name(), seed.email(), UserRole.JOB_SEEKER, logins);
            seekers.add(userRepository.save(seeker));
        }
        userRepository.flush();

        List<Job> jobs = new ArrayList<>();
        for (JobSeed seed : JOBS) {
            Job job = new Job();
            job.setTitle(seed.title());
            job.setDescription(seed.description());
            job.setLocation(seed.location());
            job.setSalary((double) seed.salary());
            job.setExperience(seed.experience());
            job.setEmploymentType(seed.employmentType());
            job.setRecruiter(recruiters.get(seed.recruiterIndex()));
            jobs.add(jobRepository.save(job));
        }
        jobRepository.flush();

        List<JobSeekerProfile> profiles = new ArrayList<>();
        for (int i = 0; i < SEEKERS.size(); i++) {
            SeekerSeed seed = SEEKERS.get(i);
            JobSeekerProfile profile = new JobSeekerProfile();
            profile.setUser(seekers.get(i));
            profile.setPhoneNumber(seed.phone());
            profile.setProfessionalSummary(seed.summary());
            profile.setSkills(seed.skills());
            profile.setEducation(List.of(seed.education()));
            profile.setExperience(seed.experience());
            profile.setProjects(List.of(seed.project()));
            profiles.add(profile);
        }
        profileRepository.saveAll(profiles);
        profileRepository.flush();

        List<Application> applications = createApplications(jobs, seekers);
        applicationRepository.saveAll(applications);
        applicationRepository.flush();

        verifySeededRecords();
        return new SeedResult(logins, applications.size());
    }

    private void removeExistingRecords() {
        applicationRepository.deleteAllInBatch();
        oauthLoginCodeRepository.deleteAllInBatch();
        oauthAccountRepository.deleteAllInBatch();
        jobRepository.deleteAllInBatch();
        profileRepository.deleteAll();
        userRepository.deleteAllInBatch();
    }

    private User createUser(
            String name,
            String email,
            UserRole role,
            List<DemoLogin> logins) {
        String password = generatePassword(name);
        logins.add(new DemoLogin(name, email, role.name(), password));
        return new User(name, email, passwordEncoder.encode(password), role.name());
    }

    private String generatePassword(String name) {
        String firstName = name.substring(0, name.indexOf(' '));
        return firstName + "Demo" + (Year.now().getValue() % 100) + "!";
    }

    private List<Application> createApplications(List<Job> jobs, List<User> seekers) {
        List<Application> applications = new ArrayList<>();
        ApplicationStatus[] statuses = ApplicationStatus.values();
        int statusIndex = 0;

        for (int jobIndex = 0; jobIndex < APPLICANTS_BY_JOB.length; jobIndex++) {
            for (int seekerIndex : APPLICANTS_BY_JOB[jobIndex]) {
                applications.add(new Application(
                        seekers.get(seekerIndex),
                        jobs.get(jobIndex),
                        statuses[statusIndex++ % statuses.length]
                ));
            }
        }
        return applications;
    }

    private void verifySeededRecords() {
        List<User> users = userRepository.findAll();
        long recruiterCount = users.stream()
                .filter(user -> UserRole.RECRUITER.name().equals(user.getRole()))
                .count();
        long seekerCount = users.stream()
                .filter(user -> UserRole.JOB_SEEKER.name().equals(user.getRole()))
                .count();
        Set<Long> recruiterIds = users.stream()
                .filter(user -> UserRole.RECRUITER.name().equals(user.getRole()))
                .map(User::getId)
                .collect(java.util.stream.Collectors.toSet());
        Set<Long> seekerIds = users.stream()
                .filter(user -> UserRole.JOB_SEEKER.name().equals(user.getRole()))
                .map(User::getId)
                .collect(java.util.stream.Collectors.toSet());
        List<Job> jobs = jobRepository.findAll();
        Set<Long> jobIds = jobs.stream()
                .map(Job::getId)
                .collect(java.util.stream.Collectors.toSet());
        long internships = jobs.stream()
                .filter(job -> EmploymentType.INTERNSHIP == job.getEmploymentType())
                .count();
        long fullTime = jobs.stream()
                .filter(job -> EmploymentType.FULL_TIME == job.getEmploymentType())
                .count();
        List<JobSeekerProfile> profiles = profileRepository.findAll();
        Set<Long> profileUserIds = new HashSet<>();
        profiles.forEach(profile -> profileUserIds.add(profile.getUser().getId()));
        if (!profileUserIds.equals(seekerIds)
                || jobs.stream().anyMatch(job ->
                        job.getRecruiter() == null
                                || !recruiterIds.contains(job.getRecruiter().getId()))) {
            throw new IllegalStateException(
                    "Demo seed verification failed: invalid profile or recruiter relationship."
            );
        }
        List<Application> applications = applicationRepository.findAll();
        Set<String> applicationPairs = new HashSet<>();
        for (Application application : applications) {
            if (application.getUser() == null
                    || application.getJob() == null
                    || !seekerIds.contains(application.getUser().getId())
                    || !jobIds.contains(application.getJob().getId())
                    || !applicationPairs.add(
                            application.getUser().getId() + ":" + application.getJob().getId())) {
                throw new IllegalStateException(
                        "Demo seed verification failed: invalid or duplicate application relationship."
                );
            }
        }

        if (users.size() != 9 || recruiterCount != 2 || seekerCount != 7
                || jobs.size() != 10 || internships != 3 || fullTime != 7
                || profiles.size() != 7 || profileUserIds.size() != 7
                || applications.size() != 28) {
            throw new IllegalStateException(
                    "Demo seed verification failed: the seeded record counts do not match expectations."
            );
        }
    }

    public record DemoLogin(String name, String email, String role, String password) {
    }

    public record SeedResult(List<DemoLogin> logins, int applicationCount) {
    }

    private record RecruiterSeed(String name, String email) {
    }

    private record SeekerSeed(
            String name,
            String email,
            String phone,
            String summary,
            List<String> skills,
            ProfileEducation education,
            List<ProfileExperience> experience,
            ProfileProject project) {
    }

    private record JobSeed(
            String title,
            String description,
            String location,
            int salary,
            int experience,
            EmploymentType employmentType,
            int recruiterIndex) {
    }
}
