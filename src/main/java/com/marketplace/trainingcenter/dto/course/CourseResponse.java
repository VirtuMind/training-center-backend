package com.marketplace.trainingcenter.dto.course;

import com.marketplace.trainingcenter.dto.quiz.QuizRequest;
import com.marketplace.trainingcenter.dto.review.ReviewResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    private Long id;
    private String title;
    private String description;
    private String level;
    private String duration;
    private String coverImage;
    private Long categoryId;
    private String categoryName;
    private TrainerInfo trainer;
    private LocalDateTime createdAt;
    private Double averageRating;
    private Integer enrollmentsCount;
    private Integer moduleCount;
    private Integer lessonCount;
    private List<ModuleResponse> modules;
    private QuizRequest quiz;
    private List<ReviewResponse> reviews;
    private Boolean isEnrolled;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainerInfo {
        private Long id;
        private String username;
        private String fullName;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleResponse {
        private Long id;
        private String title;
        private List<LessonResponse> lessons;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonResponse {
        private Long id;
        private String title;
        private String duration;
        private String videoUrl;
        private boolean completed;
    }
}
