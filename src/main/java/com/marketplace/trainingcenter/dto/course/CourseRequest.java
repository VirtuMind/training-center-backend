package com.marketplace.trainingcenter.dto.course;

import com.marketplace.trainingcenter.model.enums.CourseLevel;
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
public class CourseRequest {

    @NotBlank(message = "Course title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Course level is required")
    private CourseLevel level;
    
    private String duration;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    private MultipartFile coverImage;
}
