package com.marketplace.trainingcenter.controller;

import com.marketplace.trainingcenter.dto.ApiResponse;
import com.marketplace.trainingcenter.dto.auth.JwtAuthResponse;
import com.marketplace.trainingcenter.dto.auth.LoginRequest;
import com.marketplace.trainingcenter.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtAuthResponse jwtAuthResponse = authenticationService.login(loginRequest);
        return new ResponseEntity<>(ApiResponse.success("Login successful", jwtAuthResponse), HttpStatus.OK);
    }
}
