package cz.ememsoft.dbdrift.config;

import lombok.NonNull;

/**
 * Nemenný (immutable) nosič konfigurácie pre pripojenie k databáze.
 */
public record DatabaseConfig(
        @NonNull String host, int port, @NonNull String serviceName,
        @NonNull String user, @NonNull String password, @NonNull String schema
) {
    public String getJdbcUrl() {
        return String.format("jdbc:oracle:thin:@//%s:%d/%s", host, port, serviceName);
    }
}