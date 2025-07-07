package com.marketplace.trainingcenter.util;

import com.marketplace.trainingcenter.model.entity.Category;
import com.marketplace.trainingcenter.model.entity.User;
import com.marketplace.trainingcenter.model.enums.UserRole;
import com.marketplace.trainingcenter.repository.CategoryRepository;
import com.marketplace.trainingcenter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Users already exist in the database, skipping seeding...");
        }
        else{
            log.info("Seeding users...");
            seedUsers();
            log.info("Users seeded successfully!");
        }

        if (categoryRepository.count() > 0) {
            log.info("Categories already exist in the database, skipping seeding...");
        }
        else{
            log.info("Seeding categories...");
            seedCategories();
            log.info("Categories seeded successfully!");
        }


    }

    private void seedUsers() {
        // Create 3 admin users
        List<User> admins = List.of(
            createUser( "admin@centreformation.com", "123456", UserRole.ADMIN, "Mohammed Admin")
        );

        // Create 3 trainer users
        List<User> trainers = List.of(
            createUser( "trainer@centreformation.com", "123456", UserRole.TRAINER, "Mounir Dridi")
        );

        // Create 3 student users
        List<User> students = List.of(
            createUser("student@centreformation.com", "123456", UserRole.STUDENT, "Younes Khoubaz")
        );

        // Save all users
        userRepository.saveAll(admins);
        userRepository.saveAll(trainers);
        userRepository.saveAll(students);
    }

    private void seedCategories() {
        // Implement category seeding logic here if needed
        List<Category> categories = List.of(
            createCategory("Web Development"),
            createCategory("Marketing"),
            createCategory("Data Science"),
            createCategory("Design"),
            createCategory("Cloud Computing")
        );

        categoryRepository.saveAll(categories);
    }

    private User createUser(String username, String password, UserRole role, String fullName) {
        return User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .fullName(fullName)
                .build();
    }

    private Category createCategory(String name) {
        return Category.builder()
                .name(name)
                .build();
    }


}
