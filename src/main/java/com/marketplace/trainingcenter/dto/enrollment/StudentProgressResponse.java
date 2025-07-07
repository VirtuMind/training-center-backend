package com.marketplace.trainingcenter.dto.enrollment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgressResponse {
    private String studentFullname;
    private String studentUsername;
    private String courseTitle;
    private Integer completedLessons;
    private Integer totalLessons;
    private Double completionPercentage;
    private Double averageScore; // From quiz results
}
