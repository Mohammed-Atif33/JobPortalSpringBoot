package com.atif.jobportal.seed;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
@Profile("dev & demo-seed")
public class DemoCredentialsFileWriter {

    private static final Path CREDENTIALS_FILE =
            Path.of("DEMO_CREDENTIALS.md").toAbsolutePath().normalize();

    public Path write(List<DemoDataSeedService.DemoLogin> logins) throws IOException {
        StringBuilder document = new StringBuilder("""
                # CareerNest Demo Credentials

                Development-only credentials. Do not commit, share, or use these accounts in production.

                | Name | Email | Role | Demo password |
                |---|---|---|---|
                """);
        for (DemoDataSeedService.DemoLogin login : logins) {
            document.append("| ")
                    .append(escape(login.name())).append(" | ")
                    .append(escape(login.email())).append(" | ")
                    .append(escape(login.role())).append(" | `")
                    .append(escape(login.password())).append("` |\n");
        }

        Files.writeString(CREDENTIALS_FILE, document, StandardCharsets.UTF_8);
        return CREDENTIALS_FILE;
    }

    private String escape(String value) {
        return value.replace("|", "\\|").replace("`", "\\`");
    }
}
