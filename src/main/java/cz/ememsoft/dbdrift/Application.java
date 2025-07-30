package cz.ememsoft.dbdrift;

import cz.ememsoft.dbdrift.cli.CliHandler;
import picocli.CommandLine;

/**
 * Hlavný vstupný bod aplikácie na detekciu databázového driftu.
 */
public class Application {
    /**
     * Hlavná metóda, ktorá spúšťa aplikáciu pomocou knižnice Picocli.
     * @param args Argumenty príkazového riadka.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new CliHandler()).execute(args);
        System.exit(exitCode);
    }
}