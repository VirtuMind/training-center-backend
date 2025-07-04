package com.marketplace.trainingcenter.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatisticsResponse {

    private Long courseId;
    private String courseTitle;
    private Integer enrollmentCount;
    private Integer completionCount;
    private Double completionRate;
    private Double averageScore;
    private Integer reviewCount;
    private Double averageRating;
    private LocalDateTime lastUpdated;
    private Map<String, Long> enrollmentsByMonth;
    private Map<String, Double> averageScoresByMonth;
    private List<StudentPerformance> topPerformers;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentPerformance {
        private Long studentId;
        private String studentName;
        private Integer score;
    }
}
