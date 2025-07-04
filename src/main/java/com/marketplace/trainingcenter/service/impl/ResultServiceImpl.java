package com.marketplace.trainingcenter.service.impl;

import com.marketplace.trainingcenter.dto.course.CourseResponse;
import com.marketplace.trainingcenter.dto.result.QuizSubmissionRequest;
import com.marketplace.trainingcenter.dto.result.ResultResponse;
import com.marketplace.trainingcenter.dto.statistics.StudentStatisticsResponse;
import com.marketplace.trainingcenter.exception.AccessDeniedException;
import com.marketplace.trainingcenter.exception.ResourceNotFoundException;
import com.marketplace.trainingcenter.model.entity.*;
import com.marketplace.trainingcenter.repository.*;
import com.marketplace.trainingcenter.service.CourseService;
import com.marketplace.trainingcenter.service.EnrollmentService;
import com.marketplace.trainingcenter.service.ResultService;
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
public class ResultServiceImpl implements ResultService {

    private final ResultRepository resultRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    @Override
    @Transactional
    public ResultResponse submitQuiz(Long studentId, QuizSubmissionRequest quizSubmission) {
        // Check if the course exists
        Course course = courseRepository.findById(quizSubmission.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", quizSubmission.getCourseId()));

        // Check if the user exists
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));

        // Check if the student is enrolled in the course
        boolean isEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(studentId, quizSubmission.getCourseId());
        if (!isEnrolled) {
            throw new AccessDeniedException("You are not enrolled in this course");
        }

        // Process the quiz submission
        int totalQuestions = quizSubmission.getAnswers().size();
        int correctAnswers = 0;
        Map<Long, Answer> correctAnswersMap = new HashMap<>();
        List<ResultResponse.QuestionResult> questionResults = new ArrayList<>();
        
        // Get all question IDs from the submission
        List<Long> questionIds = quizSubmission.getAnswers().stream()
                .map(QuizSubmissionRequest.AnswerSubmission::getQuestionId)
                .collect(Collectors.toList());
        
        // Check if all questions belong to the course
        List<Question> questions = questionRepository.findAllById(questionIds);
        if (questions.size() != totalQuestions) {
            throw new ResourceNotFoundException("One or more questions not found");
        }
        
        for (Question question : questions) {
            if (!question.getCourse().getId().equals(course.getId())) {
                throw new AccessDeniedException("One or more questions do not belong to the course");
            }
            
            // Get the correct answer for each question
            List<Answer> answers = answerRepository.findByQuestionId(question.getId());
            for (Answer answer : answers) {
                if (answer.isCorrect()) {
                    correctAnswersMap.put(question.getId(), answer);
                    break;
                }
            }
        }
        
        // Process each answer submission
        for (QuizSubmissionRequest.AnswerSubmission submission : quizSubmission.getAnswers()) {
            Long questionId = submission.getQuestionId();
            Long answerId = submission.getAnswerId();
            
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));
            
            Answer selectedAnswer = answerRepository.findById(answerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Answer", "id", answerId));
            
            Answer correctAnswer = correctAnswersMap.get(questionId);
            boolean isCorrect = selectedAnswer.isCorrect();
            
            if (isCorrect) {
                correctAnswers++;
            }
            
            questionResults.add(ResultResponse.QuestionResult.builder()
                    .questionId(questionId)
                    .questionText(question.getQuestion())
                    .selectedAnswerId(selectedAnswer.getId())
                    .selectedAnswer(selectedAnswer.getAnswer())
                    .correct(isCorrect)
                    .correctAnswerId(correctAnswer.getId())
                    .correctAnswer(correctAnswer.getAnswer())
                    .build());
        }
        
        // Calculate the score (percentage)
        int score = (correctAnswers * 100) / totalQuestions;
        
        // Create or update the result
        Result result = resultRepository.findByStudentIdAndCourseId(studentId, course.getId())
                .orElse(Result.builder()
                        .student(student)
                        .course(course)
                        .build());
        
        result.setScore(score);
        result.setCompletedAt(LocalDateTime.now());
        
        Result savedResult = resultRepository.save(result);

        
        // Build and return the response
        return ResultResponse.builder()
                .id(savedResult.getId())
                .studentId(student.getId())
                .studentName(student.getFullName())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .score(score)
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .percentage((double) score)
                .completedAt(savedResult.getCompletedAt())
                .questionResults(questionResults)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResultResponse getResultById(Long resultId) {
        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result", "id", resultId));
        
        // We need to fetch all questions for this course and the student's selected answers
        List<Question> questions = questionRepository.findByCourseId(result.getCourse().getId());
        
        List<ResultResponse.QuestionResult> questionResults = new ArrayList<>();
        int totalQuestions = questions.size();
        int correctAnswers = 0;
        
        // For now, we don't store the actual answers selected by the student, so we'll return
        // only the correct answers. In a real application, you would store and retrieve 
        // the actual answers selected by the student.
        for (Question question : questions) {
            List<Answer> answers = answerRepository.findByQuestionId(question.getId());
            Answer correctAnswer = answers.stream()
                    .filter(Answer::isCorrect)
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Correct answer", "questionId", question.getId()));
            
            questionResults.add(ResultResponse.QuestionResult.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestion())
                    .correctAnswerId(correctAnswer.getId())
                    .correctAnswer(correctAnswer.getAnswer())
                    .build());
        }
        
        // Build and return the response
        return ResultResponse.builder()
                .id(result.getId())
                .studentId(result.getStudent().getId())
                .studentName(result.getStudent().getFullName())
                .courseId(result.getCourse().getId())
                .courseTitle(result.getCourse().getTitle())
                .score(result.getScore())
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .percentage((double) result.getScore())
                .completedAt(result.getCompletedAt())
                .questionResults(questionResults)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultResponse> getResultsByStudentId(Long studentId) {
        // Check if the user exists
        if (!userRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("User", "id", studentId);
        }
        
        List<Result> results = resultRepository.findByStudentId(studentId);
        return results.stream()
                .map(this::mapToResultResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultResponse> getResultsByCourseId(Long courseId) {
        // Check if the course exists
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        
        List<Result> results = resultRepository.findByCourseId(courseId);
        return results.stream()
                .map(this::mapToResultResponse)
                .collect(Collectors.toList());
    }

//    @Override
//    @Transactional(readOnly = true)
//    public StudentStatisticsResponse getStudentStatistics(Long studentId) {
//        // Check if the user exists
//        User student = userRepository.findById(studentId)
//                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
//
//        // Get the student's enrollments
//        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
//
//        // Count enrolled courses
//        long enrolledCourses = enrollments.size();
//
//        // Get average score
//        Double averageScore = resultRepository.getAverageScoreByStudentId(studentId);
//        if (averageScore == null) {
//            averageScore = 0.0;
//        }
//
//        // Count total quizzes taken
//        List<Result> results = resultRepository.findByStudentId(studentId);
//        int totalQuizzesTaken = results.size();
//
//        // Build course statistics
//        List<StudentStatisticsResponse.CourseStatistics> courseStatistics = new ArrayList<>();
//
//        for (Enrollment enrollment : enrollments) {
//            Course course = enrollment.getCourse();
//            CourseResponse courseResponse = courseService.mapToCourseResponse(course);
//
//            Double completionPercentage = enrollmentService.calculateProgressPercentage(enrollment);
//
//            Result result = resultRepository.findByStudentIdAndCourseId(studentId, course.getId())
//                    .orElse(null);
//
//            Integer score = (result != null) ? result.getScore() : null;
//            boolean completed = enrollment.getCompletedAt() != null;
//
//            StudentStatisticsResponse.CourseStatistics courseStats = StudentStatisticsResponse.CourseStatistics.builder()
//                    .course(courseResponse)
//                    .score(score)
//                    .completionPercentage(completionPercentage)
//                    .completed(completed)
//                    .build();
//
//            courseStatistics.add(courseStats);
//        }
//
//        // Build and return the response
//        return StudentStatisticsResponse.builder()
//                .studentId(student.getId())
//                .studentName(student.getFirstName() + " " + student.getLastName())
//                .completedCourses((int) completedCourses)
//                .enrolledCourses(enrollments.size())
//                .averageScore(averageScore)
//                .totalQuizzesTaken(totalQuizzesTaken)
//                .courseStatistics(courseStatistics)
//                .build();
//    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageScoreByCourseId(Long courseId) {
        // Check if the course exists
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        
        Double averageScore = resultRepository.getAverageScoreByCourseId(courseId);
        return (averageScore != null) ? averageScore : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageScoreByStudentId(Long studentId) {
        // Check if the user exists
        if (!userRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("User", "id", studentId);
        }
        
        Double averageScore = resultRepository.getAverageScoreByStudentId(studentId);
        return (averageScore != null) ? averageScore : 0.0;
    }
    
    private ResultResponse mapToResultResponse(Result result) {
        return ResultResponse.builder()
                .id(result.getId())
                .studentId(result.getStudent().getId())
                .studentName(result.getStudent().getFullName())
                .courseId(result.getCourse().getId())
                .courseTitle(result.getCourse().getTitle())
                .score(result.getScore())
                .percentage((double) result.getScore())
                .completedAt(result.getCompletedAt())
                .build();
    }
}
