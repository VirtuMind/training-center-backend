package com.marketplace.trainingcenter.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerStatistics {

    private Integer coursesCreated;
    private Integer totalStudents;
    private Double averageRating;
    private Double averageCompletionRate;
    private Integer totalModules;
    private Integer totalLessons;
    private Integer totalQuestions;
}
