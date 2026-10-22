
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

            sessionStorage.setItem("jwtToken", data.token);

            const role = String(data.role || "")
                .replace(/^ROLE_/, "")
                .toUpperCase();
            sessionStorage.setItem("userRole", role);

            window.location.href = role === "RECRUITER"
                ? "recruiter-dashboard.html"
                : "jobs.html";

        } catch (error) {
            console.error("Login error:", error);
            showMessage("login-message", "Unable to connect to the server.");
        } finally {
            button.disabled = false;
        }
    });
}

// ==================== PUBLIC JOB LIST ====================

async function loadJobs() {
    const jobList = document.getElementById("job-list");
    if (!jobList) return;

    jobList.textContent = "Loading jobs...";

    try {
        const response = await fetch(`${API_BASE_URL}/jobs`);
        const jobs = await readResponse(response);

        if (!response.ok) {
            jobList.textContent = "Unable to load jobs.";
            return;
        }

        if (!Array.isArray(jobs) || jobs.length === 0) {
            jobList.textContent = "No jobs are available right now.";
            return;
        }

        jobList.replaceChildren();

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
                `Experience: ${job.experience ?? "Not specified"}`;

            const applyButton = document.createElement("button");
            applyButton.type = "button";
            applyButton.textContent = "Apply Now";
            applyButton.addEventListener("click", () => applyForJob(job.id));

            card.append(title, description, details, applyButton);
            jobList.appendChild(card);
        });
    } catch (error) {
        console.error("Job loading error:", error);
        jobList.textContent = "Unable to connect to the server.";
    }
}

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
    const loginLink = document.getElementById("login-link");
    const registerLink = document.getElementById("register-link");
    const existingLogout = document.getElementById("logout-button");

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

document.getElementById("loadApplicationsBtn")?.addEventListener("click", async () => {
    const token = sessionStorage.getItem("jwtToken");
    const applicationsList = document.getElementById("applicationsList");
    const message = document.getElementById("applicationsMessage");

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
            message.textContent = applications.message || "Unable to load your applications.";
            return;
        }

        if (!Array.isArray(applications) || applications.length === 0) {
            message.textContent = "You have not applied to any jobs yet.";
            return;
        }

        message.textContent = `${applications.length} application(s)`;
        applications.forEach((application) => {
            const card = document.createElement("article");
            card.className = "job-card";
            const heading = document.createElement("h3");
            heading.textContent = `Job #${application.jobId}`;
            const status = document.createElement("p");
            status.textContent = `Status: ${application.status}`;
            card.append(heading, status);
            list.appendChild(card);
        });
    } catch (error) {
        message.textContent = "Unable to connect to the server.";
    }
});

// ==================== INITIALIZE ====================

loadJobs();
updateNavigation();