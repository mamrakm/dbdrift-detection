package cz.ememsoft.dbdrift.cli;

import lombok.Getter;
import lombok.ToString;
import picocli.CommandLine.Option;
import java.nio.file.Path;

/**
 * Dátová trieda, ktorá uchováva spracované argumenty z príkazového riadka pomocou Picocli.
 */
@Getter
@ToString(exclude = "password")
public class CliArguments {
    @Option(names = "--source-dir", required = true, description = "Cesta k zdrojovému adresáru Java projektu (napr. src/main/java).")
    private Path sourceDir;
    @Option(names = "--db-type", required = true, description = "Typ databázy. Musí byť 'oracle'.")
    private String dbType;
    @Option(names = "--host", required = true, description = "Adresa databázového servera.")
    private String host;
    @Option(names = "--port", required = true, description = "Port databázového servera.")
    private int port;
    @Option(names = "--user", required = true, description = "Používateľské meno pre pripojenie k databáze.")
    private String user;
    @Option(names = "--password", required = true, description = "Heslo pre pripojenie k databáze.", interactive = true)
    private String password;
    @Option(names = "--dbname", required = true, description = "Názov služby (Service Name) alebo SID Oracle databázy.")
    private String dbName;
    @Option(names = "--schema", required = true, description = "Názov schémy (vlastníka objektov) v Oracle (napr. HR).")
    private String schema;
}