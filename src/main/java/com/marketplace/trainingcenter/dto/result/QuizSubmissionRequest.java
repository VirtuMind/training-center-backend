package com.marketplace.trainingcenter.dto.result;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmissionRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @NotNull(message = "Answers are required")
    private List<AnswerSubmission> answers;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerSubmission {
        @NotNull(message = "Question ID is required")
        private Long questionId;
        
        @NotNull(message = "Answer ID is required")
        private Long answerId;
    }
}
