package com.marketplace.trainingcenter.dto.enrollment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {

    private Long id;
    private Long studentId;
    private String studentFullname;
    private Long courseId;
    private String courseCategory;
    private String courseTitle;
    private LocalDateTime enrolledAt;
    private String trainerFullname;
    private Integer totalLessons;
    private Integer completedLessons;
    private Double progressPercentage;

}
