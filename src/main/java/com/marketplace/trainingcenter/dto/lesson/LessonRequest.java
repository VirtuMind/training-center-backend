package com.marketplace.trainingcenter.dto.lesson;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonRequest {

    @NotBlank(message = "Lesson title is required")
    private String title;
    
    @NotNull(message = "Module ID is required")
    private Long moduleId;
    
    private String duration;
    
    private MultipartFile video;
    
    private Integer orderIndex;
}
