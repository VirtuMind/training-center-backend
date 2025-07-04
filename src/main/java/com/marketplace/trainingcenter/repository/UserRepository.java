package com.marketplace.trainingcenter.repository;

import com.marketplace.trainingcenter.model.entity.User;
import com.marketplace.trainingcenter.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);


    boolean existsByUsername(String username);


    List<User> findByRole(UserRole role);

    List<User> findByDeletedFalse();

    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.role = :role")
    List<User> findByRoleAndNotDeleted(UserRole role);
}
