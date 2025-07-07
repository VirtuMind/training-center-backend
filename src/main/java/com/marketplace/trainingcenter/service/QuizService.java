package com.marketplace.trainingcenter.service;

import com.marketplace.trainingcenter.dto.quiz.QuizResponse;
import com.marketplace.trainingcenter.dto.result.QuizSubmissionRequest;
import com.marketplace.trainingcenter.dto.result.ResultResponse;
import com.marketplace.trainingcenter.model.entity.Result;

import java.util.List;

public interface QuizService {

    QuizResponse getQuizByCourseId(Long courseId);

    Boolean submitQuiz(Long studentId, QuizSubmissionRequest quizSubmission);

}
