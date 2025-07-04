package com.marketplace.trainingcenter.dto.module;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleRequest {

    @NotBlank(message = "Module title is required")
    private String title;
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    private Integer orderIndex;
}
