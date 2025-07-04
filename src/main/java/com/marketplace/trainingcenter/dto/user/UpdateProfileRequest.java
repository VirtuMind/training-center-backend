package com.marketplace.trainingcenter.dto.user;

import com.marketplace.trainingcenter.model.enums.UserRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Email
    @NotNull(message = "Username is required")
    private String username;

    @NotNull(message = "Full name is required")
    private String fullname;

    @Pattern(regexp = "^$|.{8,}", message = "Password must be at least 8 characters long if provided")
    private String password;

    @NotNull(message = "Role is required")
    private UserRole role;
}
