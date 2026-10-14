package com.eventmanagement.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseUrlConfig {

    @Bean
    DataSource dataSource(DataSourceProperties properties) {
        String url = toJdbcUrl(properties.getUrl());
        DataSourceBuilder<?> builder = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(url)
                .username(properties.getUsername())
                .password(properties.getPassword());
        if (properties.getDriverClassName() != null) {
            builder.driverClassName(properties.getDriverClassName());
        }
        return builder.build();
    }

    static String toJdbcUrl(String url) {
        if (url == null || url.startsWith("jdbc:")) {
            return url;
        }

        URI uri = URI.create(url);
        String scheme = uri.getScheme();
        if (!"postgres".equalsIgnoreCase(scheme) && !"postgresql".equalsIgnoreCase(scheme)) {
            return url;
        }

        String authority = uri.getRawAuthority();
        int userInfoEnd = authority == null ? -1 : authority.lastIndexOf('@');
        if (authority == null || userInfoEnd == authority.length() - 1) {
            throw new IllegalArgumentException("DATABASE_URL must include a PostgreSQL host");
        }
        if (userInfoEnd >= 0) {
            authority = authority.substring(userInfoEnd + 1);
        }

        return "jdbc:postgresql://" + authority
                + (uri.getRawPath() == null ? "" : uri.getRawPath())
                + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
    }
}
