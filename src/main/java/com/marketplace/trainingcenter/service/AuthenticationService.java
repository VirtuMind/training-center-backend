package com.marketplace.trainingcenter.service;

import com.marketplace.trainingcenter.dto.auth.JwtAuthResponse;
import com.marketplace.trainingcenter.dto.auth.LoginRequest;
import com.marketplace.trainingcenter.model.entity.User;

public interface AuthenticationService {

    JwtAuthResponse login(LoginRequest loginRequest);

    
    JwtAuthResponse generateTokens(User user);
}
