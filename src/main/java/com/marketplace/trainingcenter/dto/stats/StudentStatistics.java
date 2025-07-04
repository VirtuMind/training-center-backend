package com.marketplace.trainingcenter.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentStatistics {

    private Integer coursesEnrolled;
    private Integer coursesCompleted;
    private Integer hoursCompleted;
    private Double averageScore;
    private Double averageProgress;
    private Integer completedLessons;
}
