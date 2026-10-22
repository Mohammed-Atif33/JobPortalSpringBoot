# Job Portal Project Brain

## Repository map

- Git root: repository root containing `frontend/` and `jobprotal/`.
- Backend: `jobprotal/`, a Java 21 / Spring Boot 4 / Maven application.
- Frontend: static HTML, CSS, and vanilla JavaScript in `frontend/`.
- API base URL: `http://localhost:8080/api`; frontend local-server CORS ports are 5500 and 5501.
- Persistence: MySQL, with JPA `ddl-auto=update`; there are no checked-in migrations or seed scripts.

## Main API workflows

- `POST /api/users`: public registration; service assigns `JOB_SEEKER` and hashes passwords with BCrypt.
- `POST /api/auth/login`: public login; returns a signed JWT and user role.
- `GET /api/jobs` and `GET /api/jobs/{id}`: public job browsing.
- Recruiter job operations: `GET /api/jobs/my`, `POST /api/jobs`, `PUT /api/jobs/{id}`, and `DELETE /api/jobs/{id}`. Ownership is checked in `JobService`; deletion is refused while applications exist.
- `POST /api/applications`: job-seeker application submission; a repeat application is rejected with HTTP 409.
- `GET /api/applications/my`: returns the authenticated job seeker's own application list.
- Recruiter application operations: `GET /api/applications`, `GET /api/applications/{id}`, and `PUT /api/applications/{id}/status`; per-job ownership is enforced for individual reads and status changes.
- Other `/api/users` operations are denied. The former endpoints had no self-ownership boundary and accepted unsafe password/role updates.
- Invalid login credentials return a generic HTTP 401; invalid tokens, including tokens for deleted users, do not authenticate protected requests.
- `PasswordHashGenerator` now reads a password interactively and prints only the BCrypt hash; it no longer embeds or prints a sample password.

## Local setup

1. Install Java 21 and have a MySQL server/database ready. Choose database credentials locally; do not put them in tracked files.
2. Copy `jobprotal/src/main/resources/application-example.properties` to the ignored `jobprotal/src/main/resources/application.properties` and provide `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` in the local process environment. `JWT_SECRET` must be Base64 encoding of at least 32 random bytes.
3. PowerShell example for generating a new secret in the current shell (keep it private):

   ```powershell
   $env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
   ```

   Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in the same shell from your own local configuration.
4. Start the backend from the repository root:

   ```powershell
   Push-Location jobprotal
   .\mvnw.cmd spring-boot:run
   Pop-Location
   ```

5. In another terminal, serve the frontend from the repository root:

   ```powershell
   python -m http.server 5500 --directory frontend
   ```

   Open `http://localhost:5500`.

## Verification

- Focused application tests: `Push-Location jobprotal; .\mvnw.cmd -Dtest=ApplicationServiceTest test; Pop-Location`
- Full tests: `Push-Location jobprotal; .\mvnw.cmd test; Pop-Location`. The existing Spring context test requires `JWT_SECRET` and reachable MySQL.
- The project has one context-load test and focused Mockito service tests; endpoint-level security integration coverage remains a useful follow-up.
- The focused application tests pass after a clean rebuild. The context-load test was attempted but could not initialize because `JWT_SECRET` was unavailable; a local MySQL-backed full test run remains unverified.

## Current audit notes

- Existing frontend uncommitted edits displayed applicant name/email in recruiter application cards; retain these edits.
- Local `jobprotal/src/main/resources/application.properties` is ignored and contains machine-specific values. Never stage it or copy its values into documentation.
- `.vscode/` was untracked when audited. Do not remove local editor files.
- A change to `JobprotalApplication.java` appeared after the initial Git status; it only comments out imports and was not modified during this work.
- No database schema migration was added. Duplicate checking is application-level; a concurrent duplicate-submit race would require a uniqueness constraint and careful handling of any existing duplicate rows before adding one.
- No files have been committed or pushed as part of this work.
