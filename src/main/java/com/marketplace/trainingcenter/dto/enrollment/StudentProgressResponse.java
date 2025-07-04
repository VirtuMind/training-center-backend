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
    private Long enrollmentId;
    private Long courseId;
    private String courseTitle;
    private String courseCoverImage;
    private Integer completedLessons;
    private Integer totalLessons;
    private Double completionPercentage;
    private String status;
    private Double averageScore; // From quiz/assessment results
}
