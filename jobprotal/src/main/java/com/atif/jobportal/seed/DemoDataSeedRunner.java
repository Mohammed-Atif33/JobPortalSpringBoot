package com.atif.jobportal.seed;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Profile("dev & demo-seed")
@ConditionalOnProperty(
        prefix = "app.demo-seed",
        name = {"enabled", "confirm-reset"},
        havingValue = "true"
)
public class DemoDataSeedRunner implements ApplicationRunner {

    private static final String TARGET_DATABASE = "springjobproject";
    private static final Pattern LOCAL_MYSQL_URL = Pattern.compile(
            "^jdbc:mysql://(?:localhost|127\\.0\\.0\\.1|\\[::1\\])(?::\\d+)?/([^/?;]+)(?:[?;].*)?$",
            Pattern.CASE_INSENSITIVE
    );

    private final DataSource dataSource;
    private final DemoDataSeedService seedService;
    private final DemoCredentialsFileWriter credentialsFileWriter;

    public DemoDataSeedRunner(
            DataSource dataSource,
            DemoDataSeedService seedService,
            DemoCredentialsFileWriter credentialsFileWriter) {
        this.dataSource = dataSource;
        this.seedService = seedService;
        this.credentialsFileWriter = credentialsFileWriter;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String actualDatabase = connection.getCatalog();
            String jdbcUrl = connection.getMetaData().getURL();
            Matcher matcher = LOCAL_MYSQL_URL.matcher(jdbcUrl);

            if (!TARGET_DATABASE.equals(actualDatabase)
                    || !matcher.matches()
                    || !TARGET_DATABASE.equals(matcher.group(1))) {
                throw new IllegalStateException(
                        "Demo seed refused: the connection must target the local "
                                + TARGET_DATABASE + " database."
                );
            }
        }

        DemoDataSeedService.SeedResult result = seedService.resetAndSeed();
        Path credentialsFile = credentialsFileWriter.write(result.logins());
        System.out.println("Demo seed completed for database " + TARGET_DATABASE + ".");
        System.out.println("Applications created: " + result.applicationCount());
        System.out.println("Demo credentials written to " + credentialsFile + ".");
    }
}
