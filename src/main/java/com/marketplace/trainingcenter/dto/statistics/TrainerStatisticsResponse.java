package com.marketplace.trainingcenter.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerStatisticsResponse {

    private Long trainerId;
    private String trainerName;
    private Integer publishedCourses;
    private Integer totalEnrollments;
    private Integer totalCompletions;
    private Double overallCompletionRate;
    private Double averageRating;
    private Integer totalReviews;
    private Map<String, Long> coursesByCategory;
    private List<CoursePerformance> coursePerformance;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoursePerformance {
        private Long courseId;
        private String courseTitle;
        private Integer enrollments;
        private Integer completions;
        private Double completionRate;
        private Double averageRating;
    }
}
