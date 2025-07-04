package com.marketplace.trainingcenter.dto.question;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {

    @NotBlank(message = "Question text is required")
    private String question;
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @Valid
    @NotNull(message = "Answers are required")
    @Size(min = 2, message = "Question must have at least 2 answers")
    private List<AnswerRequest> answers;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerRequest {
        @NotBlank(message = "Answer text is required")
        private String answer;
        
        @NotNull(message = "Correct flag is required")
        private Boolean correct;
    }
}
