
const API_BASE_URL = "http://localhost:8080/api";

function showMessage(elementId, message, type = "error") {
    const element = document.getElementById(elementId);
    if (!element) return;

    element.textContent = message;
    element.className = `form-message ${type}`;
}

async function readResponse(response) {
    const data = await response.json().catch(() => ({}));
    if (data.detail && !data.message) {
        data.message = data.detail;
    }
    return data;
}

function logout() {
    sessionStorage.removeItem("jwtToken");
    sessionStorage.removeItem("userRole");
    window.location.href = "index.html";
}

function redirectAfterLogin(data) {
    if (!data.token) {
        showMessage("login-message", "Login response did not contain a token.");
        return;
    }

    sessionStorage.setItem("jwtToken", data.token);

    const role = String(data.role || "")
        .replace(/^ROLE_/, "")
        .toUpperCase();
    sessionStorage.setItem("userRole", role);

    window.location.href = role === "RECRUITER"
        ? "recruiter-dashboard.html"
        : "jobs.html";
}

async function completeGithubLogin() {
    const loginMessage = document.getElementById("login-message");
    if (!loginMessage) return;

    const callback = new URLSearchParams(window.location.hash.slice(1));
    const code = callback.get("oauth_code");
    const error = callback.get("oauth_error");
    if (!code && !error) return;

    window.history.replaceState(
        {},
        document.title,
        window.location.pathname + window.location.search
    );

    const errors = {
        denied: "GitHub authorization was canceled.",
        missing_email: "Add a verified email address to GitHub and try again.",
        email_lookup_failed: "CareerNest could not verify your GitHub email. Please try again.",
        account_conflict: "An account already uses your verified GitHub email. Sign in with email and password instead.",
        not_configured: "GitHub sign-in is not configured on the server yet.",
        failed: "GitHub sign-in could not be completed. Please try again."
    };
    if (error) {
        showMessage(
            "login-message",
            errors[error] || errors.failed
        );
        return;
    }

    const githubButton = document.querySelector(".github-login");
    if (githubButton) githubButton.setAttribute("aria-disabled", "true");
    showMessage("login-message", "Completing GitHub sign-in...", "info");

    try {
        const response = await fetch(`${API_BASE_URL}/auth/oauth/exchange`, {
            method: "POST",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ code })
        });
        const data = await readResponse(response);

        if (!response.ok) {
            showMessage(
                "login-message",
                data.message || "GitHub sign-in code is invalid or expired."
            );
            return;
        }

        redirectAfterLogin(data);
    } catch (error) {
        console.error("GitHub login error:", error);
        showMessage("login-message", "Unable to connect to the server.");
    } finally {
        if (githubButton) githubButton.removeAttribute("aria-disabled");
    }
}

// ==================== REGISTRATION ====================

const registerForm = document.getElementById("register-form");

if (registerForm) {
    registerForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const name = document.getElementById("register-name").value.trim();
        const email = document.getElementById("register-email").value.trim();
        const password = document.getElementById("register-password").value;

        if (!name || !email || !password) {
            showMessage("register-message", "Please fill in all fields.");
            return;
        }

        if (password.length < 8) {
            showMessage("register-message", "Password must be at least 8 characters long.");
            return;
        }

        const button = registerForm.querySelector('button[type="submit"]');
        button.disabled = true;
        showMessage("register-message", "Creating your account...", "info");

        try {
            const response = await fetch(`${API_BASE_URL}/users`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name, email, password })
            });

            const data = await readResponse(response);

            if (!response.ok) {
                const errors = data.errors
                    ? Object.values(data.errors).join(" ")
                    : data.message;

                showMessage("register-message", errors || "Registration failed.");
                return;
            }

            registerForm.reset();
            showMessage("register-message", "Registration successful! You can now log in.", "success");
        } catch (error) {
            console.error("Registration error:", error);
            showMessage("register-message", "Unable to connect to the server.");
        } finally {
            button.disabled = false;
        }
    });
}

// ==================== LOGIN ====================

const loginForm = document.getElementById("login-form");

if (loginForm) {
    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const email = document.getElementById("login-email").value.trim();
        const password = document.getElementById("login-password").value;

        if (!email || !password) {
            showMessage("login-message", "Please enter your email and password.");
            return;
        }

        const button = loginForm.querySelector('button[type="submit"]');
        button.disabled = true;
        showMessage("login-message", "Logging in...", "info");

        try {
            const response = await fetch(`${API_BASE_URL}/auth/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password })
            });

            const data = await readResponse(response);

            if (!response.ok) {
                showMessage("login-message", data.message || "Login failed. Check your credentials.");
                return;
            }

            if (!data.token) {
                showMessage("login-message", "Login response did not contain a token.");
                return;
            }

            redirectAfterLogin(data);

        } catch (error) {
            console.error("Login error:", error);
            showMessage("login-message", "Unable to connect to the server.");
        } finally {
            button.disabled = false;
        }
    });
}

completeGithubLogin();

// ==================== PUBLIC JOB LIST ====================

async function loadJobs() {
    const jobList = document.getElementById("job-list");
    if (!jobList) return;

    jobList.textContent = "Loading jobs...";

    try {
        const searchInput = document.getElementById("job-search-input");
        const query = searchInput ? searchInput.value.trim() : "";
        const searchMessage = document.getElementById("job-search-message");
        if (searchMessage) searchMessage.textContent = "";
        const url = new URL(`${API_BASE_URL}/jobs`);
        if (query) url.searchParams.set("search", query);

        const response = await fetch(url);
        const jobs = await readResponse(response);

        if (!response.ok) {
            jobList.textContent = "Unable to load jobs.";
            return;
        }

        if (!Array.isArray(jobs) || jobs.length === 0) {
            jobList.textContent = query
                ? "No jobs match your search. Try a different title, recruiter, or location."
                : "No jobs are available right now.";
            return;
        }

        jobList.replaceChildren();

        jobs.forEach((job) => {
            const card = document.createElement("article");
            card.className = "job-card";
            card.id = `job-${job.id}`;

            const title = document.createElement("h3");
            title.textContent = job.title || "Untitled job";

            const description = document.createElement("p");
            description.textContent = job.description || "No description provided.";

            const details = document.createElement("p");
            details.textContent =
                `Location: ${job.location || "Not specified"} | ` +
                `Salary: ${job.salary ?? "Not specified"} | ` +
                `Experience: ${job.experience ?? "Not specified"}`;

            if (job.recruiterName) {
                const company = document.createElement("p");
                company.textContent = `Recruiter: ${job.recruiterName}`;
                card.append(title, company, description, details);
            } else {
                card.append(title, description, details);
            }

            const applyButton = document.createElement("button");
            applyButton.type = "button";
            applyButton.textContent = "Apply Now";
            applyButton.addEventListener("click", () => applyForJob(job.id));

            card.appendChild(applyButton);
            jobList.appendChild(card);
        });
    } catch (error) {
        console.error("Job loading error:", error);
        jobList.textContent = "Unable to connect to the server.";
    }
}

document.getElementById("job-search-form")?.addEventListener("submit", event => {
    event.preventDefault();
    loadJobs();
});

document.getElementById("clear-job-search")?.addEventListener("click", () => {
    const searchInput = document.getElementById("job-search-input");
    if (searchInput) searchInput.value = "";
    loadJobs();
});

// ==================== APPLY FOR JOB ====================

async function applyForJob(jobId) {
    const token = sessionStorage.getItem("jwtToken");

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/applications`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ jobId })
        });

        const data = await readResponse(response);

        if (response.status === 401) {
            sessionStorage.removeItem("jwtToken");
                    sessionStorage.removeItem("userRole");
            alert("Your session has expired. Please log in again.");
            window.location.href = "login.html";
            return;
        }

        if (response.status === 403) {
            showApplyMessage("Your account does not have permission to apply.");
            return;
        }

        if (!response.ok) {
            showApplyMessage(data.message || "Unable to submit your application.");
            return;
        }

        showApplyMessage("Application submitted successfully!", "success");
    } catch (error) {
        console.error("Application error:", error);
        showApplyMessage("Unable to connect to the server.");
    }
}

function showApplyMessage(message, type = "error") {
    let element = document.getElementById("apply-message");

    if (!element) {
        element = document.createElement("p");
        element.id = "apply-message";
        element.setAttribute("role", "status");
        element.setAttribute("aria-live", "polite");

        const jobList = document.getElementById("job-list");
        if (jobList) {
            jobList.before(element);
        } else {
            document.body.appendChild(element);
        }
    }

    element.textContent = message;
    element.className = `form-message ${type}`;
}

// ==================== RECRUITER DASHBOARD ====================

const createJobForm = document.getElementById("createJobForm");

if (createJobForm) {
    if (!sessionStorage.getItem("jwtToken")) {
        window.location.href = "login.html";
    } else {
        loadMyJobs();
    }

    createJobForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const token = sessionStorage.getItem("jwtToken");
        if (!token) {
            window.location.href = "login.html";
            return;
        }

        const job = {
            title: document.getElementById("title").value.trim(),
            description: document.getElementById("description").value.trim(),
            location: document.getElementById("location").value.trim(),
            salary: Number(document.getElementById("salary").value),
            experience: Number(document.getElementById("experience").value)
        };

        if (
            !job.title ||
            !job.description ||
            !job.location ||
            document.getElementById("salary").value === "" ||
            document.getElementById("experience").value === "" ||
            !Number.isFinite(job.salary) ||
            !Number.isInteger(job.experience) ||
            job.salary < 0 ||
            job.experience < 0
        ) {
            showMessage("formMessage", "Please enter valid details in all fields.");
            return;
        }

        const button = createJobForm.querySelector('button[type="submit"]');
        button.disabled = true;
        showMessage("formMessage", "Creating job...", "info");

        try {
            const response = await fetch(`${API_BASE_URL}/jobs`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify(job)
            });

            const data = await readResponse(response);

            if (response.status === 401) {
                sessionStorage.removeItem("jwtToken");
                window.location.href = "login.html";
                return;
            }

            if (!response.ok) {
                showMessage(
                    "formMessage",
                    response.status === 403
                        ? "Only recruiters can create jobs."
                        : data.message || "Unable to create job."
                );
                return;
            }

            createJobForm.reset();
            showMessage("formMessage", "Job created successfully!", "success");
            await loadMyJobs();

        } catch (error) {
            console.error("Create job error:", error);
            showMessage("formMessage", "Unable to connect to the server.");
        } finally {
            button.disabled = false;
        }
    });
}

async function loadMyJobs() {
    const jobsList = document.getElementById("myJobsList");
    if (!jobsList) return;

    const token = sessionStorage.getItem("jwtToken");
    if (!token) {
        window.location.href = "login.html";
        return;
    }

    const message = document.getElementById("jobsMessage");
    if (message) message.textContent = "Loading your jobs...";

    jobsList.replaceChildren();

    try {
        const response = await fetch(`${API_BASE_URL}/jobs/my`, {
            headers: { "Authorization": `Bearer ${token}` }
        });

        const jobs = await readResponse(response);

        if (response.status === 401) {
            sessionStorage.removeItem("jwtToken");
            window.location.href = "login.html";
            return;
        }

        if (!response.ok) {
            if (message) {
                message.textContent = response.status === 403
                    ? "Only recruiters can access this dashboard."
                    : "Unable to load your jobs.";
            }
            return;
        }

        if (!Array.isArray(jobs) || jobs.length === 0) {
            document.getElementById("totalJobs").textContent = "0";
            if (message) message.textContent = "You haven't posted any jobs yet.";
            return;
        }

        document.getElementById("totalJobs").textContent = jobs.length;
        if (message) message.textContent = "";

        jobs.forEach((job) => {
            const card = document.createElement("article");
            card.className = "job-card";

            const title = document.createElement("h3");
            title.textContent = job.title || "Untitled job";

            const description = document.createElement("p");
            description.textContent = job.description || "No description provided.";

            const details = document.createElement("p");
            details.textContent =
                `Location: ${job.location || "Not specified"} | ` +
                `Salary: ${job.salary ?? "Not specified"} | ` +
                `Experience: ${job.experience ?? "Not specified"} years`;

            const editButton = document.createElement("button");
            editButton.type = "button";
            editButton.textContent = "Edit";
            editButton.addEventListener("click", () => editJob(job));

            const deleteButton = document.createElement("button");
            deleteButton.type = "button";
            deleteButton.textContent = "Delete";
            deleteButton.addEventListener("click", () => deleteJob(job.id));

            card.append(title, description, details, editButton, deleteButton);
            jobsList.appendChild(card);
        });

    } catch (error) {
        console.error("Recruiter jobs loading error:", error);
        if (message) message.textContent = "Unable to connect to the server.";
    }
}

// ==================== EDIT JOB ====================

async function editJob(job) {
    const newTitle = prompt("Enter job title:", job.title || "");
    if (newTitle === null) return;

    const title = newTitle.trim();
    if (!title) {
        alert("Job title cannot be empty.");
        return;
    }

    const newDescription = prompt(
        "Enter job description:",
        job.description || ""
    );
    if (newDescription === null) return;

    const description = newDescription.trim();
    if (!description) {
        alert("Job description cannot be empty.");
        return;
    }

    const newLocation = prompt("Enter job location:", job.location || "");
    if (newLocation === null) return;

    const location = newLocation.trim();
    if (!location) {
        alert("Job location cannot be empty.");
        return;
    }

    const salaryInput = prompt("Enter salary:", String(job.salary ?? ""));
    if (salaryInput === null) return;

    const salary = Number(salaryInput);
    if (salaryInput.trim() === "" || !Number.isFinite(salary) || salary < 0) {
        alert("Please enter a valid non-negative salary.");
        return;
    }

    const experienceInput = prompt(
        "Enter experience in years:",
        String(job.experience ?? 0)
    );
    if (experienceInput === null) return;

    const experience = Number(experienceInput);
    if (
        experienceInput.trim() === "" ||
        !Number.isInteger(experience) ||
        experience < 0
    ) {
        alert("Experience must be a non-negative whole number.");
        return;
    }

    const token = sessionStorage.getItem("jwtToken");
    if (!token) {
        window.location.href = "login.html";
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/jobs/${job.id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({
                title,
                description,
                location,
                salary,
                experience
            })
        });

        const data = await readResponse(response);

        if (response.status === 401) {
            sessionStorage.removeItem("jwtToken");
            window.location.href = "login.html";
            return;
        }

        if (!response.ok) {
            alert(data.message || `Unable to update job (HTTP ${response.status}).`);
            return;
        }

        alert("Job updated successfully!");
        await loadMyJobs();

    } catch (error) {
        console.error("Update job error:", error);
        alert("Unable to connect to the server.");
    }
}

// ==================== DELETE JOB ====================

async function deleteJob(jobId) {
    const confirmed = confirm(
        "Are you sure you want to delete this job? This action cannot be undone."
    );

    if (!confirmed) return;

    const token = sessionStorage.getItem("jwtToken");
    if (!token) {
        window.location.href = "login.html";
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/jobs/${jobId}`, {
            method: "DELETE",
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (response.status === 401) {
            sessionStorage.removeItem("jwtToken");
            window.location.href = "login.html";
            return;
        }

        if (response.status === 403) {
            alert("You can only delete your own jobs.");
            return;
        }

        if (response.status === 409) {
            const data = await response.json();
            alert(data.message || "Cannot delete this job because applications are linked to it.");
            return;
        }

        if (response.status === 204 || response.ok) {
            alert("Job deleted successfully!");
            await loadMyJobs();
            return;
        }

        const data = await readResponse(response);
        alert(data.message || `Unable to delete job (HTTP ${response.status}).`);

    } catch (error) {
        console.error("Delete job error:", error);
        alert(
            "Unable to delete this job. It may have applications linked to it."
        );
    }
}

// ==================== NAVIGATION ====================

function updateNavigation() {
    const navLinks = document.querySelector(".nav-links");
    if (!navLinks) return;

    const token = sessionStorage.getItem("jwtToken");
    const userRole = sessionStorage.getItem("userRole");
    const loginLink = document.getElementById("login-link");
    const registerLink = document.getElementById("register-link");
    const profileLink = document.getElementById("profile-nav-link");
    const existingLogout = document.getElementById("logout-button");

    if (profileLink) {
        profileLink.hidden = !token || userRole !== "JOB_SEEKER";
    }

    if (token) {
        if (loginLink) loginLink.remove();
        if (registerLink) registerLink.remove();

        if (!existingLogout) {
            const logoutButton = document.createElement("button");
            logoutButton.id = "logout-button";
            logoutButton.type = "button";
            logoutButton.textContent = "Logout";
            logoutButton.addEventListener("click", logout);
            navLinks.appendChild(logoutButton);
        }
    } else if (existingLogout) {
        existingLogout.remove();
    }
}

const dashboardLogoutButton = document.getElementById("logoutBtn");

if (dashboardLogoutButton) {
    dashboardLogoutButton.addEventListener("click", logout);
}

const applicationsList = document.getElementById("applicationsList");
const candidateProfileView = document.getElementById("candidate-profile-view");
const candidateProfileMessage = document.getElementById("candidate-profile-message");
const candidateProfileContent = document.getElementById("candidate-profile-content");
const backToApplicationsButton = document.getElementById("back-to-applications-button");

if (backToApplicationsButton) {
    backToApplicationsButton.addEventListener("click", () => {
        candidateProfileView.hidden = true;
        applicationsList.hidden = false;
    });
}

document.getElementById("loadApplicationsBtn")?.addEventListener("click", async () => {
    const token = sessionStorage.getItem("jwtToken");
    const message = document.getElementById("applicationsMessage");

    candidateProfileView.hidden = true;
    applicationsList.hidden = false;
    applicationsList.replaceChildren();
    message.textContent = "Loading applications...";

    try {
        const response = await fetch(`${API_BASE_URL}/applications`, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error(`Failed to load applications (${response.status})`);
        }

        const applications = await response.json();

        if (applications.length === 0) {
            message.textContent = "No applications found.";
            return;
        }

        message.textContent = `${applications.length} application(s) found.`;

        applications.forEach(application => {
            const card = document.createElement("div");
            card.className = "application-card";

            const heading = document.createElement("h3");
            heading.textContent = `Application #${application.id}`;

            const details = document.createElement("p");
            details.textContent =
                `Job ID: ${application.jobId} | Applicant ID: ${application.userId}`;

            const applicantInfo = document.createElement("p");
            applicantInfo.textContent =
                `Name: ${application.applicantName || "Not available"} | Email: ${application.applicantEmail || "Not available"}`;

            const label = document.createElement("label");
            label.textContent = "Application status: ";

            const select = document.createElement("select");

            ["APPLIED", "SHORTLISTED", "REJECTED", "SELECTED"].forEach(value => {
                const option = document.createElement("option");
                option.value = value;
                option.textContent = value;
                select.appendChild(option);
            });

            select.value = application.status;

            const updateButton = document.createElement("button");
            updateButton.type = "button";
            updateButton.textContent = "Update Status";

            const viewProfileButton = document.createElement("button");
            viewProfileButton.type = "button";
            viewProfileButton.className = "candidate-profile-button";
            viewProfileButton.textContent = "View Profile";
            viewProfileButton.addEventListener("click", async () => {
                applicationsList.hidden = true;
                candidateProfileView.hidden = false;
                candidateProfileContent.replaceChildren();
                candidateProfileMessage.textContent = "Loading candidate profile...";
                viewProfileButton.disabled = true;

                const loading = document.createElement("div");
                loading.className = "loading-state";
                const spinner = document.createElement("span");
                spinner.className = "loading-spinner";
                spinner.setAttribute("aria-hidden", "true");
                loading.appendChild(spinner);
                candidateProfileContent.appendChild(loading);

                try {
                    const response = await fetch(
                        `${API_BASE_URL}/applications/${application.id}/profile`,
                        {
                            headers: {
                                Authorization: `Bearer ${sessionStorage.getItem("jwtToken")}`
                            }
                        }
                    );
                    const profile = await response.json().catch(() => null);
                    if (!response.ok) {
                        throw new Error(
                            profile?.message
                                || profile?.detail
                                || `Unable to load candidate profile (${response.status}).`
                        );
                    }

                    candidateProfileContent.replaceChildren();
                    const name = document.createElement("h3");
                    name.className = "candidate-profile-name";
                    name.textContent = profile.fullName || application.applicantName || "Applicant";
                    candidateProfileContent.appendChild(name);

                    if (!profile.exists) {
                        candidateProfileMessage.textContent =
                            "This applicant has not created a profile yet.";
                        return;
                    }

                    candidateProfileMessage.textContent = "";
                    const appendSection = (title, entries, emptyText, renderEntry) => {
                        const section = document.createElement("section");
                        section.className = "candidate-profile-section";
                        const heading = document.createElement("h4");
                        heading.textContent = title;
                        section.appendChild(heading);
                        if (!entries.length) {
                            const empty = document.createElement("p");
                            empty.className = "candidate-profile-empty";
                            empty.textContent = emptyText;
                            section.appendChild(empty);
                        } else {
                            entries.forEach(entry => section.appendChild(renderEntry(entry)));
                        }
                        candidateProfileContent.appendChild(section);
                    };
                    const textBlock = (text, className = "") => {
                        const paragraph = document.createElement("p");
                        paragraph.className = className;
                        paragraph.textContent = text;
                        return paragraph;
                    };
                    const entryCard = () => {
                        const article = document.createElement("article");
                        article.className = "candidate-profile-card";
                        return article;
                    };
                    const formatDates = entry =>
                        [entry.startDate, entry.endDate].filter(Boolean).join(" – ");

                    appendSection(
                        "Professional Summary",
                        profile.professionalSummary ? [profile.professionalSummary] : [],
                        "No professional summary provided.",
                        summary => textBlock(summary, "candidate-profile-summary")
                    );
                    appendSection(
                        "Skills",
                        profile.skills || [],
                        "No skills added.",
                        skill => {
                            const chip = document.createElement("span");
                            chip.className = "candidate-profile-skill";
                            chip.textContent = skill;
                            return chip;
                        }
                    );
                    appendSection(
                        "Education",
                        profile.education || [],
                        "No education details added.",
                        entry => {
                            const card = entryCard();
                            const heading = document.createElement("h5");
                            heading.textContent = entry.qualification || "Education";
                            card.append(heading);
                            if (entry.institution) card.appendChild(textBlock(entry.institution));
                            if (entry.fieldOfStudy) card.appendChild(textBlock(entry.fieldOfStudy));
                            const dates = formatDates(entry);
                            if (dates) card.appendChild(textBlock(dates, "candidate-profile-meta"));
                            return card;
                        }
                    );
                    appendSection(
                        "Work Experience",
                        profile.experience || [],
                        "No work experience or internships added.",
                        entry => {
                            const card = entryCard();
                            const heading = document.createElement("h5");
                            heading.textContent = entry.jobTitle || "Experience";
                            card.append(heading);
                            if (entry.employer) card.appendChild(textBlock(entry.employer));
                            const dates = formatDates(entry);
                            if (dates) card.appendChild(textBlock(dates, "candidate-profile-meta"));
                            if (entry.description) card.appendChild(textBlock(entry.description));
                            return card;
                        }
                    );
                    appendSection(
                        "Projects",
                        profile.projects || [],
                        "No projects added.",
                        entry => {
                            const card = entryCard();
                            const heading = document.createElement("h5");
                            heading.textContent = entry.name || "Project";
                            card.append(heading);
                            if (entry.description) card.appendChild(textBlock(entry.description));
                            if (entry.technologies) {
                                card.appendChild(textBlock(
                                    `Technologies: ${entry.technologies}`,
                                    "candidate-profile-meta"
                                ));
                            }
                            return card;
                        }
                    );
                } catch (error) {
                    candidateProfileContent.replaceChildren();
                    candidateProfileMessage.textContent = error.message;
                } finally {
                    viewProfileButton.disabled = false;
                }
            });

            const resultMessage = document.createElement("p");

            updateButton.addEventListener("click", async () => {
                updateButton.disabled = true;
                resultMessage.textContent = "Updating...";

                try {
                    const response = await fetch(
                        `${API_BASE_URL}/applications/${application.id}/status`,
                        {
                            method: "PUT",
                            headers: {
                                "Content-Type": "application/json",
                                Authorization: `Bearer ${sessionStorage.getItem("jwtToken")}`
                            },
                            body: JSON.stringify({ status: select.value })
                        }
                    );

                    const result = await response.json().catch(() => null);

                    if (!response.ok) {
                        throw new Error(
                            result?.message || `Update failed (${response.status})`
                        );
                    }

                    resultMessage.textContent = "Status updated successfully!";
                } catch (error) {
                    resultMessage.textContent = error.message;
                } finally {
                    updateButton.disabled = false;
                }
            });

            label.appendChild(select);
            card.append(
                heading,
                details,
                applicantInfo,
                viewProfileButton,
                label,
                updateButton,
                resultMessage
            );
            applicationsList.appendChild(card);
        });
    } catch (error) {
        message.textContent = error.message;
    }
});

const myApplicationsSection = document.getElementById("my-applications-section");
if (myApplicationsSection && sessionStorage.getItem("userRole") === "JOB_SEEKER") {
    myApplicationsSection.hidden = false;
}

document.getElementById("loadMyApplicationsBtn")?.addEventListener("click", async () => {
    const token = sessionStorage.getItem("jwtToken");
    const message = document.getElementById("myApplicationsMessage");
    const list = document.getElementById("myApplicationsList");
    if (!token) {
        window.location.href = "login.html";
        return;
    }

    list.replaceChildren();
    message.textContent = "Loading your applications...";
    const loading = document.createElement("div");
    loading.className = "loading-state";
    const spinner = document.createElement("span");
    spinner.className = "loading-spinner";
    spinner.setAttribute("aria-hidden", "true");
    loading.appendChild(spinner);
    list.appendChild(loading);

    try {
        const response = await fetch(`${API_BASE_URL}/applications/my`, {
            headers: { Authorization: `Bearer ${token}` }
        });
        const applications = await readResponse(response);

        if (response.status === 401) {
            sessionStorage.removeItem("jwtToken");
            sessionStorage.removeItem("userRole");
            window.location.href = "login.html";
            return;
        }

        if (!response.ok) {
            list.replaceChildren();
            message.textContent = applications.message || "Unable to load your applications.";
            return;
        }

        list.replaceChildren();
        if (!Array.isArray(applications)) {
            message.textContent = "Unable to load your applications.";
            return;
        }

        if (applications.length === 0) {
            message.textContent = "You have not applied to any jobs yet. Browse the latest opportunities above to get started.";
            return;
        }

        message.textContent = `${applications.length} application(s)`;
        applications.forEach((application) => {
            const card = document.createElement("article");
            card.className = "application-card";

            const header = document.createElement("div");
            header.className = "application-card-header";
            const heading = document.createElement("h3");
            const jobAvailable = application.jobAvailable === true;
            heading.textContent = jobAvailable
                ? application.jobTitle || "Untitled job"
                : "Job details unavailable";
            header.appendChild(heading);

            const statusValue = String(application.status || "").toUpperCase();
            const knownStatuses = ["APPLIED", "SHORTLISTED", "SELECTED", "REJECTED"];
            const statusBadge = document.createElement("span");
            statusBadge.className = `application-status-badge ${
                knownStatuses.includes(statusValue)
                    ? `status-${statusValue.toLowerCase()}`
                    : "status-unknown"
            }`;
            statusBadge.textContent = statusValue || "Status unavailable";
            statusBadge.setAttribute("aria-label", `Application status: ${statusBadge.textContent}`);
            header.appendChild(statusBadge);
            card.appendChild(header);

            if (!jobAvailable) {
                const unavailable = document.createElement("p");
                unavailable.className = "application-unavailable";
                unavailable.textContent = "This job is no longer available.";
                card.appendChild(unavailable);
            } else {
                const details = document.createElement("dl");
                details.className = "application-details";

                const addDetail = (label, value) => {
                    if (value == null || String(value).trim() === "") return;
                    const item = document.createElement("div");
                    const term = document.createElement("dt");
                    term.textContent = label;
                    const description = document.createElement("dd");
                    description.textContent = value;
                    item.append(term, description);
                    details.appendChild(item);
                };

                addDetail("Company / recruiter", application.recruiterName);
                addDetail("Location", application.jobLocation);
                if (application.salary != null) {
                    addDetail("Salary", Number(application.salary).toLocaleString("en-IN"));
                }
                if (application.experience != null) {
                    addDetail(
                        "Experience",
                        `${application.experience} ${
                            application.experience === 1 ? "year" : "years"
                        }`
                    );
                }

                if (details.childElementCount > 0) {
                    card.appendChild(details);
                }

                const missingJobDetails = [
                    application.recruiterName,
                    application.jobLocation,
                    application.salary,
                    application.experience
                ].some((value) => value == null || String(value).trim() === "");
                if (missingJobDetails) {
                    const missingNote = document.createElement("p");
                    missingNote.className = "application-optional-note";
                    missingNote.textContent = "Some job details were not provided.";
                    card.appendChild(missingNote);
                }

                const unavailableDetails = document.createElement("p");
                unavailableDetails.className = "application-optional-note";
                unavailableDetails.textContent =
                    "Application date and employment type are not stored for applications.";
                card.appendChild(unavailableDetails);
            }

            if (jobAvailable && application.jobId != null) {
                const actions = document.createElement("div");
                actions.className = "application-actions";
                const viewJob = document.createElement("a");
                viewJob.className = "btn-secondary btn-small";
                viewJob.href = `jobs.html#job-${encodeURIComponent(application.jobId)}`;
                viewJob.textContent = "View Job Details";
                actions.appendChild(viewJob);
                card.appendChild(actions);
            }
            list.appendChild(card);
        });
    } catch (error) {
        console.error("My applications loading error:", error);
        list.replaceChildren();
        message.textContent = "Unable to connect to the server.";
    }
});

// ==================== JOB SEEKER PROFILE ====================

const profileForm = document.getElementById("profile-form");

if (profileForm) {
    const profileView = document.getElementById("profile-view");
    const profileViewContent = document.getElementById("profile-view-content");
    const profileLoading = document.getElementById("profile-loading");
    const profileStatus = document.getElementById("profile-status");
    const educationEntries = document.getElementById("education-entries");
    const experienceEntries = document.getElementById("experience-entries");
    const projectEntries = document.getElementById("project-entries");
    const profileState = { exists: false, data: null };
    let profileFieldId = 0;

    function profileElement(tag, text, className) {
        const element = document.createElement(tag);
        if (text) element.textContent = text;
        if (className) element.className = className;
        return element;
    }

    function profileSection(title, entries, createEntry) {
        const section = profileElement("section", "", "profile-view-section");
        section.appendChild(profileElement("h3", title));
        if (!entries.length) {
            section.appendChild(profileElement(
                "p",
                "No details added yet.",
                "profile-empty-text"
            ));
        } else {
            entries.forEach((entry) => section.appendChild(createEntry(entry)));
        }
        return section;
    }

    function renderProfileView() {
        const data = profileState.data;
        profileViewContent.replaceChildren();

        const personal = profileElement("section", "", "profile-view-section");
        personal.appendChild(profileElement("h3", "Personal Information"));
        const personalGrid = profileElement("dl", "", "profile-personal-grid");
        [
            ["Full name", data.fullName || "Not available"],
            ["Phone number", data.phoneNumber || "Not provided"]
        ].forEach(([label, value]) => {
            const item = profileElement("div", "", "profile-personal-item");
            item.append(
                profileElement("dt", label),
                profileElement("dd", value)
            );
            personalGrid.appendChild(item);
        });
        personal.appendChild(personalGrid);
        profileViewContent.appendChild(personal);

        const summary = profileElement("section", "", "profile-view-section");
        summary.appendChild(profileElement("h3", "Professional Summary"));
        const summaryText = data.professionalSummary
            ? profileElement("p", data.professionalSummary, "profile-summary-text")
            : profileElement("p", "No summary added yet.", "profile-empty-text");
        summary.appendChild(summaryText);
        profileViewContent.appendChild(summary);

        const skillsSection = profileElement("section", "", "profile-view-section");
        skillsSection.appendChild(profileElement("h3", "Skills"));
        if ((data.skills || []).length) {
            const skillsList = profileElement("div", "", "profile-skills-list");
            (data.skills || []).forEach((skill) => {
                skillsList.appendChild(profileElement("span", skill, "profile-skill"));
            });
            skillsSection.appendChild(skillsList);
        } else {
            skillsSection.appendChild(profileElement(
                "p",
                "No details added yet.",
                "profile-empty-text"
            ));
        }
        profileViewContent.appendChild(skillsSection);

        profileViewContent.appendChild(profileSection(
            "Education",
            data.education || [],
            (entry) => {
                const card = profileElement("article", "", "profile-detail-card");
                card.appendChild(profileElement("h4", entry.qualification));
                card.appendChild(profileElement("p", entry.institution));
                if (entry.fieldOfStudy) card.appendChild(profileElement("p", entry.fieldOfStudy));
                const dates = [entry.startDate, entry.endDate].filter(Boolean).join(" – ");
                if (dates) card.appendChild(profileElement("p", dates, "profile-detail-meta"));
                return card;
            }
        ));

        profileViewContent.appendChild(profileSection(
            "Work Experience",
            data.experience || [],
            (entry) => {
                const card = profileElement("article", "", "profile-detail-card");
                card.appendChild(profileElement("h4", entry.jobTitle));
                card.appendChild(profileElement("p", entry.employer));
                const dates = [entry.startDate, entry.endDate].filter(Boolean).join(" – ");
                if (dates) card.appendChild(profileElement("p", dates, "profile-detail-meta"));
                if (entry.description) card.appendChild(profileElement("p", entry.description));
                return card;
            }
        ));

        profileViewContent.appendChild(profileSection(
            "Projects",
            data.projects || [],
            (entry) => {
                const card = profileElement("article", "", "profile-detail-card");
                card.appendChild(profileElement("h4", entry.name));
                card.appendChild(profileElement("p", entry.description));
                if (entry.technologies) {
                    card.appendChild(profileElement(
                        "p",
                        `Technologies: ${entry.technologies}`,
                        "profile-detail-meta"
                    ));
                }
                return card;
            }
        ));

        document.getElementById("profile-heading").textContent =
            data.fullName ? `${data.fullName}'s profile` : "Your profile";
        document.getElementById("edit-profile-button").textContent =
            profileState.exists ? "Edit profile" : "Create profile";
    }

    function createProfileField(labelText, field, value, type = "text", required = false) {
        const wrapper = profileElement("div", "", "field-group");
        const id = `profile-${field}-${++profileFieldId}`;
        const label = profileElement("label", labelText);
        label.htmlFor = id;
        const input = type === "textarea"
            ? document.createElement("textarea")
            : document.createElement("input");
        input.id = id;
        input.dataset.profileField = field;
        input.value = value || "";
        if (type === "textarea") {
            input.rows = field === "description" ? 3 : 2;
            input.maxLength = field === "technologies" ? 1000 : 2000;
        } else {
            input.type = type;
            const maxLengths = {
                institution: 160,
                qualification: 120,
                fieldOfStudy: 120,
                startDate: 20,
                endDate: 20,
                employer: 160,
                jobTitle: 120,
                name: 160
            };
            if (type === "text") input.maxLength = maxLengths[field] || 200;
        }
        if (required) input.required = true;
        wrapper.append(label, input);
        return wrapper;
    }

    function addProfileEntry(container, type, values = {}) {
        const names = {
            education: {
                title: "Education entry",
                fields: [
                    ["Institution", "institution", "text", true],
                    ["Qualification", "qualification", "text", true],
                    ["Field of study", "fieldOfStudy", "text", false],
                    ["Start date", "startDate", "text", false],
                    ["End date", "endDate", "text", false]
                ]
            },
            experience: {
                title: "Experience entry",
                fields: [
                    ["Employer", "employer", "text", true],
                    ["Job title", "jobTitle", "text", true],
                    ["Description", "description", "textarea", false],
                    ["Start date", "startDate", "text", false],
                    ["End date", "endDate", "text", false]
                ]
            },
            project: {
                title: "Project entry",
                fields: [
                    ["Project name", "name", "text", true],
                    ["Description", "description", "textarea", true],
                    ["Technologies used (one per line)", "technologies", "textarea", false]
                ]
            }
        }[type];
        const entry = profileElement("fieldset", "", "profile-entry");
        entry.dataset.profileEntry = type;
        const heading = profileElement("legend", names.title);
        const fields = profileElement("div", "", "profile-entry-grid");
        names.fields.forEach(([label, field, inputType, required]) => {
            fields.appendChild(createProfileField(
                label,
                field,
                values[field],
                inputType,
                required
            ));
        });
        const remove = profileElement("button", "Remove", "btn-secondary btn-small profile-remove-entry");
        remove.type = "button";
        remove.addEventListener("click", () => entry.remove());
        entry.append(heading, fields, remove);
        container.appendChild(entry);
    }

    function fillProfileForm(data) {
        document.getElementById("profile-full-name").value = data.fullName || "";
        document.getElementById("profile-phone").value = data.phoneNumber || "";
        document.getElementById("profile-summary").value = data.professionalSummary || "";
        document.getElementById("profile-skills").value = (data.skills || []).join("\n");
        [
            [educationEntries, "education", data.education || []],
            [experienceEntries, "experience", data.experience || []],
            [projectEntries, "project", data.projects || []]
        ].forEach(([container, type, entries]) => {
            container.replaceChildren();
            entries.forEach((entry) => addProfileEntry(container, type, entry));
        });
    }

    function setProfileEditing(editing) {
        profileView.hidden = editing;
        profileForm.hidden = !editing;
        document.getElementById("edit-profile-button").hidden = editing;
        document.getElementById("cancel-profile-edit-button").hidden = !editing;
        document.getElementById("save-profile-button").hidden = !editing;
    }

    function readProfileEntries(container) {
        return Array.from(container.querySelectorAll("[data-profile-entry]"))
            .map((entry) => Object.fromEntries(
                Array.from(entry.querySelectorAll("[data-profile-field]"))
                    .map((field) => [field.dataset.profileField, field.value.trim()])
            ));
    }

    function profileRequestBody() {
        return {
            phoneNumber: document.getElementById("profile-phone").value.trim(),
            professionalSummary: document.getElementById("profile-summary").value.trim(),
            skills: document.getElementById("profile-skills").value
                .split(/\r?\n/)
                .map((skill) => skill.trim())
                .filter(Boolean),
            education: readProfileEntries(educationEntries),
            experience: readProfileEntries(experienceEntries),
            projects: readProfileEntries(projectEntries)
        };
    }

    function handleProfileAuthFailure(response) {
        if (response.status !== 401) return false;
        sessionStorage.removeItem("jwtToken");
        sessionStorage.removeItem("userRole");
        window.location.href = "login.html";
        return true;
    }

    document.getElementById("edit-profile-button").addEventListener("click", () => {
        fillProfileForm(profileState.data);
        setProfileEditing(true);
        profileStatus.textContent = "";
    });

    document.getElementById("cancel-profile-edit-button").addEventListener("click", () => {
        fillProfileForm(profileState.data);
        setProfileEditing(false);
        profileStatus.textContent = "";
    });

    document.getElementById("add-education-button").addEventListener("click", () => {
        addProfileEntry(educationEntries, "education");
    });
    document.getElementById("add-experience-button").addEventListener("click", () => {
        addProfileEntry(experienceEntries, "experience");
    });
    document.getElementById("add-project-button").addEventListener("click", () => {
        addProfileEntry(projectEntries, "project");
    });

    profileForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const token = sessionStorage.getItem("jwtToken");
        if (!token) {
            window.location.href = "login.html";
            return;
        }

        const saveButton = document.getElementById("save-profile-button");
        saveButton.disabled = true;
        showMessage("profile-status", "Saving your profile...", "info");
        try {
            const response = await fetch(`${API_BASE_URL}/job-seeker/profile`, {
                method: profileState.exists ? "PUT" : "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(profileRequestBody())
            });
            const data = await readResponse(response);
            if (handleProfileAuthFailure(response)) return;
            if (!response.ok) {
                const details = data.errors
                    ? Object.values(data.errors).join(" ")
                    : data.message;
                showMessage(
                    "profile-status",
                    details || "Unable to save your profile."
                );
                return;
            }

            profileState.exists = true;
            profileState.data = data;
            renderProfileView();
            fillProfileForm(data);
            setProfileEditing(false);
            showMessage("profile-status", "Your profile was saved.", "success");
        } catch (error) {
            console.error("Profile save error:", error);
            showMessage("profile-status", "Unable to connect to the server.");
        } finally {
            saveButton.disabled = false;
        }
    });

    async function loadJobSeekerProfile() {
        const token = sessionStorage.getItem("jwtToken");
        if (!token) {
            window.location.href = "login.html";
            return;
        }
        if (sessionStorage.getItem("userRole") !== "JOB_SEEKER") {
            window.location.href = "recruiter-dashboard.html";
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/job-seeker/profile`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            const data = await readResponse(response);
            if (handleProfileAuthFailure(response)) return;
            if (!response.ok) {
                showMessage(
                    "profile-status",
                    data.message || "Unable to load your profile."
                );
                return;
            }

            profileState.exists = data.exists === true;
            profileState.data = data;
            renderProfileView();
            fillProfileForm(data);
            profileLoading.hidden = true;
            if (profileState.exists) {
                profileView.hidden = false;
                setProfileEditing(false);
            } else {
                showMessage(
                    "profile-status",
                    "You don't have a profile yet. Add your details to create one.",
                    "info"
                );
                setProfileEditing(true);
            }
        } catch (error) {
            console.error("Profile loading error:", error);
            profileLoading.hidden = true;
            showMessage("profile-status", "Unable to connect to the server.");
        }
    }

    loadJobSeekerProfile();
}

// ==================== INITIALIZE ====================

loadJobs();
updateNavigation();