package cz.ememsoft.dbdrift.parser.ast;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class EntityInheritanceGraph {

    private final Map<String, ClassOrInterfaceDeclaration> classMap;
    private final List<ClassOrInterfaceDeclaration> entities;

    public EntityInheritanceGraph(Collection<CompilationUnit> compilationUnits) {
        this.classMap = compilationUnits.stream()
                .flatMap(cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream())
                .map(this::resolveDeclaration)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing // V prípade duplicity si necháme prvú nájdenú
                ));

        this.entities = classMap.values().stream()
                .filter(c -> c.isAnnotationPresent("Entity") && !c.isAbstract())
                .collect(Collectors.toList());

        log.info("V grafe dedičnosti identifikovaných {} konkrétnych entít.", entities.size());
    }

    private Optional<Map.Entry<String, ClassOrInterfaceDeclaration>> resolveDeclaration(ClassOrInterfaceDeclaration declaration) {
        try {
            // === KĽÚČOVÁ OPRAVA ===
            // Metóda resolve() na ClassOrInterfaceDeclaration vracia ResolvedReferenceTypeDeclaration,
            // z ktorej môžeme priamo získať kvalifikovaný názov.
            String qualifiedName = declaration.resolve().getQualifiedName();
            return Optional.of(Map.entry(qualifiedName, declaration));
        } catch (UnsolvedSymbolException | UnsupportedOperationException e) {
            log.trace("Nepodarilo sa získať plne kvalifikovaný názov pre triedu '{}'. Bude ignorovaná v grafe dedičnosti.", declaration.getNameAsString());
            return Optional.empty();
        }
    }

    public List<ClassOrInterfaceDeclaration> getEntities() {
        return entities;
    }

    public Optional<ClassOrInterfaceDeclaration> findClassByQualifiedName(String qualifiedName) {
        return Optional.ofNullable(classMap.get(qualifiedName));
    }
}