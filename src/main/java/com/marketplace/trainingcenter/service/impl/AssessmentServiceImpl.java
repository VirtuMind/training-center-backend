package com.marketplace.trainingcenter.service.impl;

import com.marketplace.trainingcenter.dto.assessment.AnswerRequest;
import com.marketplace.trainingcenter.dto.assessment.AnswerResponse;
import com.marketplace.trainingcenter.dto.assessment.QuestionRequest;
import com.marketplace.trainingcenter.dto.assessment.QuestionResponse;
import com.marketplace.trainingcenter.exception.ResourceNotFoundException;
import com.marketplace.trainingcenter.model.entity.Answer;
import com.marketplace.trainingcenter.model.entity.Course;
import com.marketplace.trainingcenter.model.entity.Question;
import com.marketplace.trainingcenter.repository.AnswerRepository;
import com.marketplace.trainingcenter.repository.CourseRepository;
import com.marketplace.trainingcenter.repository.QuestionRepository;
import com.marketplace.trainingcenter.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public QuestionResponse createQuestion(QuestionRequest questionRequest) {
        // Check if course exists
        Course course = courseRepository.findById(questionRequest.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", questionRequest.getCourseId()));
        
        // Create question
        Question question = Question.builder()
                .question(questionRequest.getQuestion())
                .course(course)
                .build();
        
        Question savedQuestion = questionRepository.save(question);
        return mapToQuestionResponse(savedQuestion, true);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(Long id, Boolean includeCorrectAnswers) {
        Question question = getQuestionEntityById(id);
        return mapToQuestionResponse(question, includeCorrectAnswers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByCourseId(Long courseId, Boolean includeCorrectAnswers) {
        // Check if course exists
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        
        List<Question> questions = questionRepository.findByCourseId(courseId);
        return questions.stream()
                .map(question -> mapToQuestionResponse(question, includeCorrectAnswers))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(Long id, QuestionRequest questionRequest) {
        Question question = getQuestionEntityById(id);
        
        // Update question
        question.setQuestion(questionRequest.getQuestion());
        
        // If course ID is different, update it
        if (!question.getCourse().getId().equals(questionRequest.getCourseId())) {
            Course course = courseRepository.findById(questionRequest.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course", "id", questionRequest.getCourseId()));
            question.setCourse(course);
        }
        
        Question updatedQuestion = questionRepository.save(question);
        return mapToQuestionResponse(updatedQuestion, true);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        Question question = getQuestionEntityById(id);
        questionRepository.delete(question);
    }

    @Override
    @Transactional
    public AnswerResponse createAnswer(AnswerRequest answerRequest, Long questionId) {
        // Check if question exists
        Question question = getQuestionEntityById(questionId);
        
        // Create answer
        Answer answer = Answer.builder()
                .answer(answerRequest.getAnswer())
                .correct(answerRequest.getCorrect())
                .question(question)
                .build();
        
        Answer savedAnswer = answerRepository.save(answer);
        return mapToAnswerResponse(savedAnswer, true);
    }

    @Override
    @Transactional(readOnly = true)
    public AnswerResponse getAnswerById(Long id, Boolean includeCorrectFlag) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Answer", "id", id));
        
        return mapToAnswerResponse(answer, includeCorrectFlag);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnswerResponse> getAnswersByQuestionId(Long questionId, Boolean includeCorrectFlag) {
        // Check if question exists
        if (!questionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("Question", "id", questionId);
        }
        
        List<Answer> answers = answerRepository.findByQuestionId(questionId);
        return answers.stream()
                .map(answer -> mapToAnswerResponse(answer, includeCorrectFlag))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AnswerResponse updateAnswer(Long id, AnswerRequest answerRequest) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Answer", "id", id));
        
        // Update answer
        answer.setAnswer(answerRequest.getAnswer());
        answer.setCorrect(answerRequest.getCorrect());
        
        Answer updatedAnswer = answerRepository.save(answer);
        return mapToAnswerResponse(updatedAnswer, true);
    }

    @Override
    @Transactional
    public void deleteAnswer(Long id) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Answer", "id", id));
        
        answerRepository.delete(answer);
    }

    @Override
    @Transactional(readOnly = true)
    public Question getQuestionEntityById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", id));
    }
    
    // Helper methods to map entities to DTOs
    private QuestionResponse mapToQuestionResponse(Question question, Boolean includeCorrectAnswers) {
        List<Answer> answers = answerRepository.findByQuestionId(question.getId());
        
        List<AnswerResponse> answerResponses = answers.stream()
                .map(answer -> mapToAnswerResponse(answer, includeCorrectAnswers))
                .collect(Collectors.toList());
        
        return QuestionResponse.builder()
                .id(question.getId())
                .question(question.getQuestion())
                .courseId(question.getCourse().getId())
                .courseTitle(question.getCourse().getTitle())
                .answers(answerResponses)
                .build();
    }
    
    private AnswerResponse mapToAnswerResponse(Answer answer, Boolean includeCorrectFlag) {
        return AnswerResponse.builder()
                .id(answer.getId())
                .answer(answer.getAnswer())
                .correct(includeCorrectFlag ? answer.isCorrect() : null)
                .build();
    }
}
