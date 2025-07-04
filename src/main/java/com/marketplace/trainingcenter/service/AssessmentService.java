package com.marketplace.trainingcenter.service;

import com.marketplace.trainingcenter.dto.assessment.AnswerRequest;
import com.marketplace.trainingcenter.dto.assessment.AnswerResponse;
import com.marketplace.trainingcenter.dto.assessment.QuestionRequest;
import com.marketplace.trainingcenter.dto.assessment.QuestionResponse;
import com.marketplace.trainingcenter.model.entity.Question;

import java.util.List;

public interface AssessmentService {

    QuestionResponse createQuestion(QuestionRequest questionRequest);
    
    QuestionResponse getQuestionById(Long id, Boolean includeCorrectAnswers);
    
    List<QuestionResponse> getQuestionsByCourseId(Long courseId, Boolean includeCorrectAnswers);
    
    QuestionResponse updateQuestion(Long id, QuestionRequest questionRequest);
    
    void deleteQuestion(Long id);
    
    AnswerResponse createAnswer(AnswerRequest answerRequest, Long questionId);
    
    AnswerResponse getAnswerById(Long id, Boolean includeCorrectFlag);
    
    List<AnswerResponse> getAnswersByQuestionId(Long questionId, Boolean includeCorrectFlag);
    
    AnswerResponse updateAnswer(Long id, AnswerRequest answerRequest);
    
    void deleteAnswer(Long id);
    
    Question getQuestionEntityById(Long id);
}
