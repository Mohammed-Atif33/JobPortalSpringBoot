# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.1.1/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.1.1/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.1.1/reference/web/servlet.html)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/4.1.1/reference/using/devtools.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.1.1/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Validation](https://docs.spring.io/spring-boot/4.1.1/reference/io/validation.html)
* [OAuth2 Client](https://docs.spring.io/spring-boot/4.1.1/reference/web/spring-security.html#web.security.oauth2.client)

## CareerNest GitHub sign-in

GitHub sign-in is optional. The application continues to start with email/password
authentication when GitHub credentials are not configured. A GitHub login creates
a local `JOB_SEEKER` account on first sign-in. GitHub identities are matched by
GitHub's stable user ID; an existing local account is not automatically linked
because its email matches. Users in that situation should use their existing
email/password login.

### Register a GitHub OAuth App

1. Sign in to GitHub and open **Settings → Developer settings → OAuth Apps**.
2. Select **New OAuth App**.
3. Set **Application name** to `CareerNest`.
4. Set **Homepage URL** to `http://localhost:5500`.
5. Set **Authorization callback URL** to
   `http://localhost:8080/login/oauth2/code/github`.
   The backend has no custom port or context path, so Spring Security's default
   callback path uses port 8080. If the backend host, port, or context path
   changes, update the callback URL in the GitHub app accordingly.
6. Save the app, copy its **Client ID**, and select **Generate a new client
   secret**. Do not commit or share the secret.

### Configure and run locally (Windows PowerShell)

Set credentials in the same PowerShell window used to launch the backend:

```powershell
$env:GITHUB_CLIENT_ID = "<your-github-oauth-client-id>"
$env:GITHUB_CLIENT_SECRET = "<your-github-oauth-client-secret>"
$env:FRONTEND_URL = "http://localhost:5500"
$env:JWT_SECRET = "<your-existing-base64-jwt-signing-key>"
```

`GITHUB_CLIENT_ID` and `GITHUB_CLIENT_SECRET` are optional until GitHub sign-in
is used. `FRONTEND_URL` is the frontend origin without a trailing slash; the
default is `http://localhost:5500`. `FRONTEND_LOGIN_PATH` is the path to the
login page; its default is `/frontend/login.html`, matching the repository-root
Live Server setup. If you serve the `frontend` directory itself as the server
root, set `FRONTEND_LOGIN_PATH=/login.html`. Use the same hostname (`localhost`)
for the frontend and backend so the short-lived OAuth browser session is sent
during the credentialed code exchange. The existing API CORS allowlist includes the local
frontend ports 5500 and 5501. Configure the database variables shown in
`application-example.properties` as required for your local MySQL database.

From the `jobprotal` directory, start the backend:

```powershell
.\mvnw.cmd spring-boot:run
```

The normal local callback is
`http://localhost:8080/login/oauth2/code/github`; GitHub redirects there after
authorization. The backend then returns to
`http://localhost:5500/frontend/login.html` with a short-lived, one-time code
in the URL fragment. The frontend immediately removes the fragment and exchanges
the code using the browser session cookie. The application JWT is returned in the
exchange response and continues to be stored in `sessionStorage` as before; the
JWT itself is never placed in a URL. `sessionStorage` is accessible to
JavaScript, so the existing bearer-token design remains exposed to successful
same-origin script injection; this implementation does not switch the app to
cookie-based API authentication. The temporary OAuth session cookie is used
only to bind and consume the one-time code, and is invalidated after exchange.

Serve the repository root on port 5500 (for example, with the VS Code Live
Server extension), then open `http://localhost:5500/frontend/login.html`. Choose
**Continue with GitHub** and complete the GitHub authorization. A first-time
account requires a verified GitHub email. If GitHub provides no verified email,
sign-in is rejected with a safe message and no local account is created.

Run the backend tests from `jobprotal` with:

```powershell
.\mvnw.cmd test
```

GitHub's real authorization flow requires a registered OAuth App and its
credentials; automated tests do not validate external GitHub credentials or the
live callback.

## Job seeker profiles

Authenticated job seekers can retrieve their profile with `GET
/api/job-seeker/profile`, create one with `POST` to the same path, and update it
with `PUT`. Send the existing JWT as a bearer token. The server identifies the
account from the authenticated principal; profile requests do not accept an
account ID, email, password, or role. The full name comes from the existing
user account and is not editable here. With
`spring.jpa.hibernate.ddl-auto=update`, JPA creates the `job_seeker_profiles`
table and separate collection tables for skills, education, experience, and
projects. Existing user accounts do not get a profile row until they create one.

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Accessing data with MySQL](https://spring.io/guides/gs/accessing-data-mysql/)
* [Validation](https://spring.io/guides/gs/validating-form-input/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.
