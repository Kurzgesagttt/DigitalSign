package com.Kurzgesagtt.DigitalSignature.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseUrlConfiguration {

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "DATABASE_URL")
    public DataSource marketplaceDataSource(Environment environment) {
        URI databaseUri;
        try {
            databaseUri = URI.create(environment.getRequiredProperty("DATABASE_URL"));
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("DATABASE_URL must be a valid PostgreSQL connection URL.");
        }

        String scheme = databaseUri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("postgres") || scheme.equalsIgnoreCase("postgresql"))
                || databaseUri.getHost() == null || databaseUri.getRawPath() == null
                || databaseUri.getRawPath().length() < 2) {
            throw new IllegalStateException("DATABASE_URL must contain a PostgreSQL host and database name.");
        }

        String userInfo = databaseUri.getUserInfo();
        int credentialSeparator = userInfo == null ? -1 : userInfo.indexOf(':');
        if (credentialSeparator < 1 || credentialSeparator == userInfo.length() - 1) {
            throw new IllegalStateException("DATABASE_URL must include PostgreSQL username and password.");
        }

        String host = databaseUri.getHost();
        if (host.contains(":")) {
            host = "[" + host + "]";
        }
        int port = databaseUri.getPort() > 0 ? databaseUri.getPort() : 5432;
        String query = databaseUri.getRawQuery();
        if (query != null) {
            query = java.util.Arrays.stream(query.split("&"))
                    .filter(parameter -> !parameter.toLowerCase().startsWith("sslmode="))
                    .collect(java.util.stream.Collectors.joining("&"));
        }
        query = query == null || query.isBlank() ? "sslmode=require" : query + "&sslmode=require";
        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + databaseUri.getRawPath() + "?" + query;

        HikariConfig pool = new HikariConfig();
        pool.setJdbcUrl(jdbcUrl);
        pool.setUsername(userInfo.substring(0, credentialSeparator));
        pool.setPassword(userInfo.substring(credentialSeparator + 1));
        pool.setPoolName("digitalsign-postgres");
        pool.setMaximumPoolSize(Integer.parseInt(environment.getProperty("DB_POOL_MAX", "3")));
        pool.setMinimumIdle(0);
        return new HikariDataSource(pool);
    }
}
