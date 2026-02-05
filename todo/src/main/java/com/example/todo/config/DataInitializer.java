package com.example.todo.config;

import com.example.todo.entity.User;
import com.example.todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initUser() {
        return args -> {
            if (userRepository.findByUsername("user").isEmpty()) {
                User user = new User();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("password"));
                user.setRole("USER");
                userRepository.save(user);
            }
            if (userRepository.findByUsername("user2").isEmpty()) {
                User user = new User();
                user.setUsername("user2");
                user.setPassword(passwordEncoder.encode("password"));
                user.setRole("USER");
                userRepository.save(user);
            }
            if (userRepository.findByUsername("admin").isEmpty()) {
                User user = new User();
                user.setUsername("admin");
                user.setPassword(passwordEncoder.encode("adminpass"));
                user.setRole("ADMIN");
                userRepository.save(user);
            }
        };
    }
}
