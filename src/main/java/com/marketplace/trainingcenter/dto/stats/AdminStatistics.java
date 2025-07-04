package com.marketplace.trainingcenter.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatistics {

    private Integer totalUsers;
    private Integer totalStudents;
    private Integer totalTrainers;
    private Integer totalCourses;
    private Integer totalEnrollments;
    private Integer totalCategories;
    private Double averageCourseRating;
    private Integer totalCompletedCourses;
}
