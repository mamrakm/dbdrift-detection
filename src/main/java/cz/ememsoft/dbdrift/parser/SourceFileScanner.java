package cz.ememsoft.dbdrift.parser;


import cz.ememsoft.dbdrift.exception.ApplicationExceptions;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Skenuje adresár a hľadá zdrojové súbory.
 */
@Slf4j
public final class SourceFileScanner {
    private SourceFileScanner() {}
    public static List<Path> findJavaFiles(@NonNull Path sourceDir) {
        log.debug("Skenujem .java súbory v {}", sourceDir);
        if (!Files.isDirectory(sourceDir)) {
            throw new ApplicationExceptions.JpaParsingException("Zdrojový adresár neexistuje alebo nie je adresár: " + sourceDir);
        }
        try (Stream<Path> stream = Files.walk(sourceDir)) {
            return stream.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".java")).toList();
        } catch (IOException e) {
            throw new ApplicationExceptions.JpaParsingException("Nebolo možné čítať zdrojový adresár: " + sourceDir, e);
        }
    }
}