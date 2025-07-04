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
public class AdminDashboardResponse {

    private Integer totalUsers;
    private Integer totalStudents;
    private Integer totalTrainers;
    private Integer totalAdmins;
    private Integer totalCourses;
    private Integer totalEnrollments;
    private Integer totalCompletions;
    private Double overallCompletionRate;
    private Map<String, Long> userRegistrationsByMonth;
    private Map<String, Long> courseCreationsByMonth;
    private Map<String, Long> enrollmentsByMonth;
    private Map<String, Long> completionsByMonth;
    private List<CategoryStatistics> categoryStatistics;
    private List<TopCourse> topCoursesByEnrollment;
    private List<TopCourse> topCoursesByRating;
    private List<TopTrainer> topTrainersByEnrollment;
    private List<TopTrainer> topTrainersByRating;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStatistics {
        private Long categoryId;
        private String categoryName;
        private Integer courseCount;
        private Integer enrollmentCount;
        private Double averageRating;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCourse {
        private Long courseId;
        private String courseTitle;
        private Long trainerId;
        private String trainerName;
        private Integer enrollmentCount;
        private Double averageRating;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopTrainer {
        private Long trainerId;
        private String trainerName;
        private Integer courseCount;
        private Integer enrollmentCount;
        private Double averageRating;
    }
}
