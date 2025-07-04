package com.marketplace.trainingcenter.dto.lesson;

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
public class ReorderLessonsRequest {

    @NotNull(message = "Module ID is required")
    private Long moduleId;
    
    @NotNull(message = "Lesson IDs list cannot be null")
    @Size(min = 1, message = "At least one lesson ID is required")
    private List<Long> lessonIds;
}
