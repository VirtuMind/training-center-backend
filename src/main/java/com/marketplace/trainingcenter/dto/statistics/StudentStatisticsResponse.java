package com.marketplace.trainingcenter.dto.statistics;

import com.marketplace.trainingcenter.dto.course.CourseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentStatisticsResponse {

    private Long studentId;
    private String studentName;
    private Integer completedCourses;
    private Integer enrolledCourses;
    private Double averageScore;
    private Integer totalQuizzesTaken;
    private List<CourseStatistics> courseStatistics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseStatistics {
        private CourseResponse course;
        private Integer score;
        private Double completionPercentage;
        private boolean completed;
    }
}
