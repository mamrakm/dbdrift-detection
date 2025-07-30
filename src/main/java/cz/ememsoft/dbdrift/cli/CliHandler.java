package cz.ememsoft.dbdrift.cli;


import cz.ememsoft.dbdrift.config.DatabaseConfig;
import cz.ememsoft.dbdrift.service.DriftDetectionService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import java.util.concurrent.Callable;

/**
 * Hlavný handler pre príkazy využívajúci Picocli. Orchestruje celý tok aplikácie.
 */
@Slf4j
@Command(name = "db-drift-detector", mixinStandardHelpOptions = true, version = "1.0.0", description = "Deteguje drift medzi schémou Oracle databázy a JPA entitami.")
public class CliHandler implements Callable<Integer> {
    @Mixin
    private CliArguments arguments;

    @Override
    public Integer call() {
        log.info("Spúšťam proces detekcie databázového driftu...");
        log.debug("Prijaté argumenty (bez hesla): Host={}, Port={}, DBName={}, User={}, Schema={}, SourceDir={}",
                arguments.getHost(), arguments.getPort(), arguments.getDbName(), arguments.getUser(), arguments.getSchema(), arguments.getSourceDir());

        if (!"oracle".equalsIgnoreCase(arguments.getDbType())) {
            log.error("Nepodporovaný typ databázy: '{}'. Podporovaný je iba 'oracle'.", arguments.getDbType());
            return 1;
        }

        try {
            var dbConfig = new DatabaseConfig(
                    arguments.getHost(), arguments.getPort(), arguments.getDbName(),
                    arguments.getUser(), arguments.getPassword(), arguments.getSchema().toUpperCase()
            );
            var driftDetector = new DriftDetectionService(dbConfig);
            driftDetector.detectAndReportDrift(arguments.getSourceDir());
            log.info("Proces detekcie driftu bol úspešne dokončený.");
            return 0;
        } catch (Exception e) {
            log.error("Počas vykonávania detekcie driftu nastala kritická chyba.", e);
            return 1;
        }
    }
}