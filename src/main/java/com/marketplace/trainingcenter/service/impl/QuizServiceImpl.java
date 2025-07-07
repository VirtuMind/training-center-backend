package com.marketplace.trainingcenter.service.impl;

import com.marketplace.trainingcenter.dto.question.QuestionResponse;
import com.marketplace.trainingcenter.dto.quiz.QuizResponse;
import com.marketplace.trainingcenter.dto.result.QuizSubmissionRequest;
import com.marketplace.trainingcenter.dto.result.ResultResponse;
import com.marketplace.trainingcenter.exception.AccessDeniedException;
import com.marketplace.trainingcenter.exception.ResourceNotFoundException;
import com.marketplace.trainingcenter.model.entity.*;
import com.marketplace.trainingcenter.repository.*;
import com.marketplace.trainingcenter.service.CourseService;
import com.marketplace.trainingcenter.service.EnrollmentService;
import com.marketplace.trainingcenter.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final ResultRepository resultRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    @Override
    public QuizResponse getQuizByCourseId(Long courseId) {
        // Check if the course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Fetch all questions for the course
        List<Question> questions = questionRepository.findByCourseId(courseId);
        if (questions.isEmpty()) {
            throw new ResourceNotFoundException("No questions found for the course", "courseId", courseId);
        }

        // Map questions to QuestionResponse
        List<QuestionResponse> questionResponses = questions.stream()
                .map(question -> QuestionResponse.builder()
                        .id(question.getId())
                        .question(question.getQuestion())
                        .answers(
                                answerRepository.findByQuestionId(question.getId()).stream()
                                .map(answer -> QuestionResponse.AnswerResponse.builder()
                                        .id(answer.getId())
                                        .answer(answer.getAnswer())
                                        .correct(answer.isCorrect())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        // Build and return the QuizResponse
        return QuizResponse.builder()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .questions(questionResponses)
                .build();
    }

    @Override
    @Transactional
    public Boolean submitQuiz(Long studentId, QuizSubmissionRequest quizSubmission) {
        Course course = courseRepository.findById(quizSubmission.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", quizSubmission.getCourseId()));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        // Create or update the result
        Result result = Result.builder()
                        .student(student)
                        .course(course)
                        .score(quizSubmission.getScore())
                        .build();
        try {
            resultRepository.save(result);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
