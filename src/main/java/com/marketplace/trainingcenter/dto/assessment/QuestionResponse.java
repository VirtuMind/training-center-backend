package com.marketplace.trainingcenter.dto.assessment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {

    private Long id;
    private String question;
    private Long courseId;
    private String courseTitle;
    private List<AnswerResponse> answers;
}
