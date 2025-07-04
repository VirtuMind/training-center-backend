package com.marketplace.trainingcenter.dto.user;

import com.marketplace.trainingcenter.model.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = "Username is required")
    @Email(message = "Username should be a valid email")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
    
    @NotBlank(message = "Full name is required")
    private String fullname;
    
    @NotNull(message = "Role is required")
    private UserRole role;
}
