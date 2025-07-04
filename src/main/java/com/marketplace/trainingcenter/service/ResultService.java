package com.marketplace.trainingcenter.service;

import com.marketplace.trainingcenter.dto.result.QuizSubmissionRequest;
import com.marketplace.trainingcenter.dto.result.ResultResponse;
import com.marketplace.trainingcenter.dto.statistics.StudentStatisticsResponse;

import java.util.List;

public interface ResultService {

    /**
     * Submit a quiz and create a result.
     * 
     * @param studentId The ID of the student submitting the quiz
     * @param quizSubmission The quiz submission
     * @return The result of the quiz submission
     */
    ResultResponse submitQuiz(Long studentId, QuizSubmissionRequest quizSubmission);
    
    /**
     * Get a result by ID.
     * 
     * @param resultId The ID of the result to retrieve
     * @return The result
     */
    ResultResponse getResultById(Long resultId);
    
    /**
     * Get all results for a student.
     * 
     * @param studentId The ID of the student
     * @return A list of results for the student
     */
    List<ResultResponse> getResultsByStudentId(Long studentId);
    
    /**
     * Get all results for a course.
     * 
     * @param courseId The ID of the course
     * @return A list of results for the course
     */
    List<ResultResponse> getResultsByCourseId(Long courseId);
    
    /**
     * Get the statistics for a student.
     * 
     * @param studentId The ID of the student
     * @return The student statistics
     */
//    StudentStatisticsResponse getStudentStatistics(Long studentId);
    
    /**
     * Get the average score for a course.
     * 
     * @param courseId The ID of the course
     * @return The average score for the course
     */
    Double getAverageScoreByCourseId(Long courseId);
    
    /**
     * Get the average score for a student.
     * 
     * @param studentId The ID of the student
     * @return The average score for the student
     */
    Double getAverageScoreByStudentId(Long studentId);
}
