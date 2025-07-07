package com.marketplace.trainingcenter.dto.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizRequest {

    
    @Valid
    @NotEmpty(message = "Quiz must have at least one question")
    private List<QuestionRequest> questions = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionRequest {
        private Long id; // Null for new questions
        
        @NotBlank(message = "Question text is required")
        private String question;
        
        @Valid
        @NotEmpty(message = "Question must have at least one answer")
        private List<AnswerRequest> answers = new ArrayList<>();
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerRequest {
        private Long id; // Null for new answers
        
        @NotBlank(message = "Answer text is required")
        private String answer;
        
        private boolean correct;
    }
}
