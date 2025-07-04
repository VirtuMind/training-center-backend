package com.marketplace.trainingcenter.model.entity;

import com.marketplace.trainingcenter.model.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @NotBlank(message = "Full name is required")
    @Column(nullable = false)
    private String fullName;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "deleted")
    private boolean deleted = false;

    // Relationships
    @OneToMany(mappedBy = "trainer")
    private Set<Course> createdCourses = new HashSet<>();

    @OneToMany(mappedBy = "student")
    private Set<Enrollment> enrollments = new HashSet<>();

    @OneToMany(mappedBy = "student")
    private Set<Review> reviews = new HashSet<>();

    @OneToMany(mappedBy = "student")
    private Set<CompletedModule> completedModules = new HashSet<>();

    @OneToMany(mappedBy = "student")
    private Set<Result> results = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
