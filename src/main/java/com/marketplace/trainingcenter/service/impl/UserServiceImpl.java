package com.marketplace.trainingcenter.service.impl;

import com.marketplace.trainingcenter.dto.user.UpdateProfileRequest;
import com.marketplace.trainingcenter.dto.user.UserRequest;
import com.marketplace.trainingcenter.dto.user.UserResponse;
import com.marketplace.trainingcenter.exception.BadRequestException;
import com.marketplace.trainingcenter.exception.ForbiddenException;
import com.marketplace.trainingcenter.exception.ResourceAlreadyExistsException;
import com.marketplace.trainingcenter.exception.ResourceNotFoundException;
import com.marketplace.trainingcenter.model.entity.User;
import com.marketplace.trainingcenter.model.enums.UserRole;
import com.marketplace.trainingcenter.repository.UserRepository;
import com.marketplace.trainingcenter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new ResourceAlreadyExistsException("User", "username", userRequest.getUsername());
        }


        User user = User.builder()
                .username(userRequest.getUsername())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .fullName(userRequest.getFullname())
                .role(userRequest.getRole())
                .build();

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return mapToUserResponse(getCurrentUserEntity());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = getUserEntityById(id);
        return mapToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findByDeletedFalse().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateProfileRequest userRequest) {
        User user = getUserEntityById(id);

        // Check if username is already taken by another user
        if (!user.getUsername().equals(userRequest.getUsername()) &&
                userRepository.existsByUsername(userRequest.getUsername())) {
            throw new ResourceAlreadyExistsException("User", "username", userRequest.getUsername());
        }


        user.setUsername(userRequest.getUsername());
        user.setFullName(userRequest.getFullname());
        user.setRole(userRequest.getRole());

        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest updateRequest) {
        User currentUser = getCurrentUserEntity();

        if (updateRequest.getUsername() != null && !updateRequest.getUsername().equals(currentUser.getUsername())) {
            if (userRepository.existsByUsername(updateRequest.getUsername())) {
                throw new ResourceAlreadyExistsException("User", "username", updateRequest.getUsername());
            }
            currentUser.setUsername(updateRequest.getUsername());
        }

        if (updateRequest.getFullname() != null) {
            currentUser.setFullName(updateRequest.getFullname());
        }

        // Update password if provided
        if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
            currentUser.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }

        User updatedUser = userRepository.save(currentUser);
        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserEntityById(id);
        
        // Prevent deleting the last admin
        if (user.getRole() == UserRole.ADMIN && 
                userRepository.findByRoleAndNotDeleted(UserRole.ADMIN).size() <= 1) {
            throw new BadRequestException("Cannot delete the last admin user");
        }
        
        user.setDeleted(true);
        userRepository.save(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }


    @Override
    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("Not authenticated");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullName())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
