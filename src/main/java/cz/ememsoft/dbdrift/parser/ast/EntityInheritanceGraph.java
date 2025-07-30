package cz.ememsoft.dbdrift.parser.ast;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Buduje a uchováva mapu všetkých tried a ich dedičských vzťahov.
 */
@Slf4j
@Getter
public class EntityInheritanceGraph {
    private final Map<String, ClassOrInterfaceDeclaration> typeMap;
    private final List<ClassOrInterfaceDeclaration> entities;

    public EntityInheritanceGraph(@NonNull Collection<CompilationUnit> compilationUnits) {
        log.debug("Vytváram graf dedičnosti a typov...");
        this.typeMap = compilationUnits.stream()
                .flatMap(cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream())
                .filter(c -> c.getFullyQualifiedName().isPresent())
                .collect(Collectors.toMap(c -> c.getFullyQualifiedName().get(), c -> c, (c1, c2) -> c1));

        this.entities = typeMap.values().stream()
                .filter(c -> c.isAnnotationPresent("Entity"))
                .toList();
        log.info("Graf dedičnosti vytvorený. Nájdených {} typov a {} @Entity tried.", typeMap.size(), entities.size());
    }

    public Optional<ClassOrInterfaceDeclaration> findClassByFqn(@NonNull String fqn) {
        return Optional.ofNullable(typeMap.get(fqn));
    }

    /**
     * Nájde deklaráciu nadtriedy pre danú triedu s robustným ošetrením typov.
     * @param declaration Trieda, ktorej nadtriedu hľadáme.
     * @return Optional obsahujúci deklaráciu nadtriedy.
     */
    public Optional<ClassOrInterfaceDeclaration> getSuperclassOf(@NonNull ClassOrInterfaceDeclaration declaration) {
        return declaration.getExtendedTypes().stream().findFirst().flatMap(superClassType -> {
            try {
                ResolvedType resolvedSuperClassType = superClassType.resolve();

                // OPRAVA č. 1 & 2: Bezpečná kontrola typu a správne použitie návratovej hodnoty.
                if (resolvedSuperClassType instanceof ResolvedReferenceType resolvedReferenceType) {
                    // metóda .getQualifiedName() vracia priamo String.
                    String fqn = resolvedReferenceType.getQualifiedName();
                    // Naša metóda .findClassByFqn() už správne vracia Optional.
                    return this.findClassByFqn(fqn);
                }

                log.trace("Nadtrieda pre '{}' nie je referenčný typ: {}", declaration.getNameAsString(), resolvedSuperClassType.describe());
                return Optional.empty();

            } catch (UnsolvedSymbolException e) {
                log.warn("Nepodarilo sa plne vyriešiť symbol pre nadtriedu '{}' v kontexte triedy '{}'. Dedičnosť pre túto vetvu nemusí byť kompletná.",
                        superClassType.getNameAsString(), declaration.getNameAsString());
                return Optional.empty();
            }
        });
    }
}