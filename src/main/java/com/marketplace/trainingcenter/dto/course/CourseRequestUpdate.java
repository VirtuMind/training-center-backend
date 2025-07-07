package com.marketplace.trainingcenter.dto.course;

import com.marketplace.trainingcenter.model.enums.CourseLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequestUpdate {
    @NotBlank(message = "Course title is required")
    private String title;

    private String description;

    @NotNull(message = "Course level is required")
    private CourseLevel level;

    @NotBlank(message = "Course duration is required")
    private String duration;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String coverImageUrl;

    private MultipartFile coverImage; // Optional, can be null if not updating

    @NotNull(message = "Module list cannot be null")
    private String modules; // name="modules", holds JSON string

    @NotNull(message = "Quiz is required")
    private String quiz;  // name="quiz", holds JSON string
}
