package com.marketplace.trainingcenter.service;

import com.marketplace.trainingcenter.dto.user.UpdateProfileRequest;
import com.marketplace.trainingcenter.dto.user.UserRequest;
import com.marketplace.trainingcenter.dto.user.UserResponse;
import com.marketplace.trainingcenter.model.entity.User;
import com.marketplace.trainingcenter.model.enums.UserRole;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest userRequest);
    
    UserResponse getCurrentUser();
    
    UserResponse getUserById(Long id);
    
    List<UserResponse> getAllUsers();
    
    List<UserResponse> getUsersByRole(UserRole role);
    
    UserResponse updateUser(Long id, UpdateProfileRequest userRequest);
    
    UserResponse updateProfile(UpdateProfileRequest updateRequest);
    
    void deleteUser(Long id);
    
    boolean existsByUsername(String username);

    
    User getUserEntityById(Long id);
    
    User getCurrentUserEntity();
}
