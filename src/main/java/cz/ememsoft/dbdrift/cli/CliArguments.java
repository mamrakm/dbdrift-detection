package cz.ememsoft.dbdrift.cli;

import lombok.Getter;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;

/**
 * Definuje a uchováva argumenty príkazového riadku.
 */
@Getter
public class CliArguments {

    @Option(names = "--host", required = true, description = "Hostname databázového servera.")
    private String host;

    @Option(names = "--port", required = true, description = "Port databázového servera.")
    private int port;

    @Option(names = "--dbname", required = true, description = "Názov databázy (SID alebo Service Name).")
    private String dbName;

    @Option(names = "--user", required = true, description = "Používateľské meno pre pripojenie k databáze.")
    private String user;

    @Option(names = "--password", required = true, description = "Heslo pre pripojenie k databáze.", interactive = true)
    private String password;

    @Option(names = "--schema", required = true, description = "Názov databázovej schémy na kontrolu.")
    private String schema;

    // === KĽÚČOVÁ ZMENA ===
    // Parameter bol premenovaný na --source-dirs pre lepšiu zrozumiteľnosť.
    @Option(names = "--source-dirs", required = true, arity = "1..*", description = "Cesta ku koreňovému adresáru (alebo viacerým adresárom) Java zdrojových súborov.")
    private List<Path> sourceDirs;

    @Option(names = "--db-type", required = true, description = "Typ databázy (podporovaný je iba 'oracle').")
    private String dbType;
}
