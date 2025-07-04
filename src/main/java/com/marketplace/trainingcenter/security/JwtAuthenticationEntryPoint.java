package com.marketplace.trainingcenter.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.trainingcenter.dto.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request, 
            HttpServletResponse response, 
            AuthenticationException authException
    ) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        ApiResponse<?> apiResponse = ApiResponse.error(
                "UNAUTHORIZED", 
                "You are not authorized to access this resource"
        );

        OutputStream outputStream = response.getOutputStream();
        new ObjectMapper().writeValue(outputStream, apiResponse);
        outputStream.flush();
    }
}
