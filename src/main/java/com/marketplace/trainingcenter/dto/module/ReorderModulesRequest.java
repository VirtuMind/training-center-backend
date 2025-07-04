package com.marketplace.trainingcenter.dto.module;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderModulesRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @NotNull(message = "Module IDs list cannot be null")
    @Size(min = 1, message = "At least one module ID is required")
    private List<Long> moduleIds;
}
