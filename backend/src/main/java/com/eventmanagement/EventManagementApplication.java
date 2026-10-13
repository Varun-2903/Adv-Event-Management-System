package com.eventmanagement;

import com.eventmanagement.entity.Role;
import com.eventmanagement.entity.User;
import com.eventmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class EventManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventManagementApplication.class, args);
    }

    @Bean
    CommandLineRunner seedAdmin(UserRepository users, PasswordEncoder encoder,
                                @Value("${app.admin.email}") String adminEmail,
                                @Value("${app.admin.initial-password}") String adminPassword) {
        return args -> {
            var existingAdmin = users.findByEmail(adminEmail);
            if (existingAdmin.isEmpty()) {
                User admin = new User();
                admin.setName("System Admin");
                admin.setEmail(adminEmail);
                admin.setPassword(encoder.encode(adminPassword));
                admin.setRole(Role.ADMIN);
                users.save(admin);
            } else {
                User admin = existingAdmin.get();
                if (!encoder.matches(adminPassword, admin.getPassword())) {
                    admin.setPassword(encoder.encode(adminPassword));
                    users.save(admin);
                }
            }
        };
    }
}
