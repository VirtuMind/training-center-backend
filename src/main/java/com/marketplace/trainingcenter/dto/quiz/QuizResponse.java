package com.marketplace.trainingcenter.dto.quiz;

import com.marketplace.trainingcenter.dto.question.QuestionResponse;
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
public class QuizResponse {
    private Long courseId;
    private String courseTitle;
    private List<QuestionResponse> questions;
}
