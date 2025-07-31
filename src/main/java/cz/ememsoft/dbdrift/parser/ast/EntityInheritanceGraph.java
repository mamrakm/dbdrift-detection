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
    private final List<ClassOrInterfaceDeclaration> concreteEntities;

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

        this.concreteEntities = classMap.values().stream()
                .filter(c -> c.isAnnotationPresent("Entity") && !c.isAbstract())
                .collect(Collectors.toList());

        log.info("V grafe dedičnosti identifikovaných {} tried a {} konkrétnych entít.", classMap.size(), concreteEntities.size());
    }

    private Optional<Map.Entry<String, ClassOrInterfaceDeclaration>> resolveDeclaration(ClassOrInterfaceDeclaration declaration) {
        try {
            String qualifiedName = declaration.resolve().getQualifiedName();
            return Optional.of(Map.entry(qualifiedName, declaration));
        } catch (UnsolvedSymbolException | UnsupportedOperationException e) {
            // Ak plné vyriešenie zlyhá (napr. chýba predok z JARu), skúsime odvodiť názov z package.
            // Toto je kľúčové, aby sme triedu nestratili z mapy.
            Optional<String> packageName = declaration.findCompilationUnit()
                    .flatMap(cu -> cu.getPackageDeclaration().map(pd -> pd.getNameAsString()));

            if (packageName.isPresent()) {
                String qualifiedName = packageName.get() + "." + declaration.getNameAsString();
                log.trace("Plné vyriešenie zlyhalo pre '{}', používam odvodený názov: {}", declaration.getNameAsString(), qualifiedName);
                return Optional.of(Map.entry(qualifiedName, declaration));
            }
            log.warn("Nepodarilo sa získať ani odvodiť kvalifikovaný názov pre triedu '{}'. Bude ignorovaná.", declaration.getNameAsString());
            return Optional.empty();
        }
    }

    public List<ClassOrInterfaceDeclaration> getConcreteEntities() {
        return concreteEntities;
    }

    public Optional<ClassOrInterfaceDeclaration> findClassByQualifiedName(String qualifiedName) {
        return Optional.ofNullable(classMap.get(qualifiedName));
    }
}