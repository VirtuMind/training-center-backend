package com.marketplace.trainingcenter.dto.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseMeta {
    private Long id;
    private String title;
    private String description;
    private String level;
    private String duration;
    private String coverImage;
    private String categoryName;
    private LocalDateTime updatedAt;
    private Double averageRating;
    private Integer enrollmentsCount;
}
