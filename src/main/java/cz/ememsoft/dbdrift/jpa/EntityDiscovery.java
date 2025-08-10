package cz.ememsoft.dbdrift.jpa;

import jakarta.persistence.Entity;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class EntityDiscovery {
    
    public Set<Class<?>> findEntitiesInPackage(String classpath, String packageName) throws Exception {
        Set<Class<?>> entities = new LinkedHashSet<>();
        
        File classpathFile = new File(classpath);
        URLClassLoader classLoader = new URLClassLoader(new URL[]{classpathFile.toURI().toURL()});
        
        if (classpathFile.isDirectory()) {
            entities.addAll(findEntitiesInDirectory(classLoader, classpathFile, packageName));
        } else if (classpathFile.getName().endsWith(".jar")) {
            entities.addAll(findEntitiesInJar(classLoader, classpathFile, packageName));
        } else {
            throw new IllegalArgumentException("Classpath must be a directory or JAR file");
        }
        
        log.info("Found {} JPA entities in package '{}'", entities.size(), packageName);
        return entities;
    }
    
    private Set<Class<?>> findEntitiesInDirectory(URLClassLoader classLoader, File directory, String packageName) throws Exception {
        Set<Class<?>> entities = new LinkedHashSet<>();
        String packagePath = packageName.replace('.', '/');
        File packageDir = new File(directory, packagePath);
        
        if (!packageDir.exists()) {
            log.warn("Package directory not found: {}", packageDir.getAbsolutePath());
            return entities;
        }
        
        scanDirectoryForClasses(classLoader, packageDir, packageName, entities);
        return entities;
    }
    
    private void scanDirectoryForClasses(URLClassLoader classLoader, File directory, String packageName, Set<Class<?>> entities) throws Exception {
        File[] files = directory.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectoryForClasses(classLoader, file, packageName + "." + file.getName(), entities);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = classLoader.loadClass(className);
                    if (clazz.isAnnotationPresent(Entity.class)) {
                        entities.add(clazz);
                        log.debug("Found entity: {}", className);
                    }
                } catch (ClassNotFoundException e) {
                    log.warn("Could not load class: {}", className);
                }
            }
        }
    }
    
    private Set<Class<?>> findEntitiesInJar(URLClassLoader classLoader, File jarFile, String packageName) throws Exception {
        Set<Class<?>> entities = new LinkedHashSet<>();
        String packagePath = packageName.replace('.', '/');
        
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                
                if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                    String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                    
                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazz.isAnnotationPresent(Entity.class)) {
                            entities.add(clazz);
                            log.debug("Found entity in JAR: {}", className);
                        }
                    } catch (ClassNotFoundException e) {
                        log.warn("Could not load class from JAR: {}", className);
                    }
                }
            }
        }
        
        return entities;
    }
}