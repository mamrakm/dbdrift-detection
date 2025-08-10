package cz.ememsoft.dbdrift.cli;

import cz.ememsoft.dbdrift.config.DatabaseConfig;
import cz.ememsoft.dbdrift.db.DatabaseConnectionFactory;
import cz.ememsoft.dbdrift.service.SchemaComparisonService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import java.util.concurrent.Callable;

/**
 * Hlavný handler pre príkazy využívajúci Picocli. Orchestruje celý tok aplikácie.
 */
@Slf4j
@Command(name = "db-drift-detector", mixinStandardHelpOptions = true, version = "1.0.0", description = "Generuje YAML súbory pre schému Oracle databázy a JPA entity na porovnanie pomocou diff nástrojov.")
public class CliHandler implements Callable<Integer> {
    @Mixin
    private CliArguments arguments;

    @Override
    public Integer call() {
        log.info("Spúšťam generovanie YAML súborov pre schému databázy a JPA entity...");
        log.debug("Prijaté argumenty (bez hesla): Host={}, Port={}, DBName={}, User={}, Schema={}, Classpath={}, Package={}",
                arguments.getHost(), arguments.getPort(), arguments.getDbName(), arguments.getUser(), 
                arguments.getSchema(), arguments.getClasspath(), arguments.getRootPackage());

        if (!"oracle".equalsIgnoreCase(arguments.getDbType())) {
            log.error("Nepodporovaný typ databázy: '{}'. Podporovaný je iba 'oracle'.", arguments.getDbType());
            return 1;
        }

        try {
            var dbConfig = new DatabaseConfig(
                    arguments.getHost(), arguments.getPort(), arguments.getDbName(),
                    arguments.getUser(), arguments.getPassword(), arguments.getSchema().toUpperCase()
            );
            
            var connectionFactory = new DatabaseConnectionFactory();
            var comparisonService = new SchemaComparisonService();
            
            try (var connection = connectionFactory.createConnection(dbConfig)) {
                comparisonService.generateSchemaYamls(
                    arguments.getClasspath(), 
                    arguments.getRootPackage(), 
                    connection, 
                    arguments.getSchema().toUpperCase()
                );
            }
            
            log.info("Generovanie YAML súborov bolo úspešne dokončené.");
            return 0;
        } catch (Exception e) {
            log.error("Počas generovania YAML súborov nastala kritická chyba.", e);
            return 1;
        }
    }
}