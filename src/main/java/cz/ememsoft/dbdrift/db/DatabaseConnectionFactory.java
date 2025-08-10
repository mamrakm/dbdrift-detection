package cz.ememsoft.dbdrift.db;

import cz.ememsoft.dbdrift.config.DatabaseConfig;
import cz.ememsoft.dbdrift.exception.ApplicationExceptions;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Továreň (factory) na vytváranie databázových pripojení.
 */
@Slf4j
public class DatabaseConnectionFactory {
    public static Connection createConnection(@NonNull DatabaseConfig config) {
        String url = config.getJdbcUrl();
        log.info("Pokúšam sa pripojiť k databáze na adrese: {}", url);
        try {
            return DriverManager.getConnection(url, config.user(), config.password());
        } catch (SQLException e) {
            log.error("Nepodarilo sa pripojiť k databáze. Skontrolujte prihlasovacie údaje, dostupnosť siete a databázy.", e);
            throw new ApplicationExceptions.DatabaseConnectionException("Nebolo možné nadviazať spojenie s databázou.", e);
        }
    }
}