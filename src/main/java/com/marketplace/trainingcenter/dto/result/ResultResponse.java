package com.marketplace.trainingcenter.dto.result;

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
public class ResultResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseTitle;
    private Integer score;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Double percentage;
    private LocalDateTime completedAt;
    private List<QuestionResult> questionResults;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResult {
        private Long questionId;
        private String questionText;
        private Long selectedAnswerId;
        private String selectedAnswer;
        private boolean correct;
        private Long correctAnswerId;
        private String correctAnswer;
    }
}
