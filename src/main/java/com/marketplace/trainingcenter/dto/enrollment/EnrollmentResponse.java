package com.marketplace.trainingcenter.dto.enrollment;

import com.marketplace.trainingcenter.model.enums.EnrollmentStatus;
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
    private String studentName;
    private Long courseId;
    private String courseTitle;
    private String courseCoverImage;
    private LocalDateTime enrollmentDate;
    private EnrollmentStatus status;
    private Double progress;
    private Integer completedLessons;
    private Integer totalLessons;
}
