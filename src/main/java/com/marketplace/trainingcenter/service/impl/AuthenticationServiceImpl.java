package com.marketplace.trainingcenter.service.impl;

import com.marketplace.trainingcenter.dto.auth.JwtAuthResponse;
import com.marketplace.trainingcenter.dto.auth.LoginRequest;
import com.marketplace.trainingcenter.exception.ResourceNotFoundException;
import com.marketplace.trainingcenter.model.entity.User;
import com.marketplace.trainingcenter.repository.UserRepository;
import com.marketplace.trainingcenter.security.CustomUserDetails;
import com.marketplace.trainingcenter.security.JwtTokenProvider;
import com.marketplace.trainingcenter.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Override
    @Transactional
    public JwtAuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "username or email", username));

        return generateTokens(user);
    }


    @Override
    @Transactional
    public JwtAuthResponse generateTokens(User user) {
        // Create CustomUserDetails for JWT token generation
        UserDetails userDetails = new CustomUserDetails(user);

        // Generate access token
        String accessToken = tokenProvider.generateAccessToken(userDetails);


        // Create response
        return JwtAuthResponse.builder()
                .accessToken(accessToken)
                .expiresIn(accessTokenExpiration)
                .tokenType("Bearer")
                .user(JwtAuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .role(user.getRole().name())
                        .build())
                .build();
    }
}
