package com.marketplace.trainingcenter.controller;

import com.marketplace.trainingcenter.dto.ApiResponse;
import com.marketplace.trainingcenter.dto.result.QuizSubmissionRequest;
import com.marketplace.trainingcenter.dto.result.ResultResponse;
import com.marketplace.trainingcenter.dto.statistics.StudentStatisticsResponse;
import com.marketplace.trainingcenter.model.entity.User;
import com.marketplace.trainingcenter.security.CustomUserDetails;
import com.marketplace.trainingcenter.service.ResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ResultResponse>> submitQuiz(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody QuizSubmissionRequest quizSubmission) {
        ResultResponse result = resultService.submitQuiz(currentUser.getId(), quizSubmission);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quiz submitted successfully", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TRAINER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ResultResponse>> getResultById(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable("id") Long resultId) {
        ResultResponse result = resultService.getResultById(resultId);
        
        // If the user is a student, only allow access to their own results
        if (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")) &&
            !result.getStudentId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<ResultResponse>builder()
                            .success(false)
                            .message("You don't have permission to access this result")
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build());
        }
        
        return ResponseEntity.ok(ApiResponse.success("Result retrieved successfully", result));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TRAINER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ResultResponse>>> getResultsByStudentId(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable("studentId") Long studentId) {
        // If the user is a student, only allow access to their own results
        if (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")) &&
            !studentId.equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<List<ResultResponse>>builder()
                            .success(false)
                            .message("You don't have permission to access these results")
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build());
        }
        
        List<ResultResponse> results = resultService.getResultsByStudentId(studentId);
        return ResponseEntity.ok(ApiResponse.success("Results retrieved successfully", results));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ResultResponse>>> getResultsByCourseId(
            @PathVariable("courseId") Long courseId) {
        List<ResultResponse> results = resultService.getResultsByCourseId(courseId);
        return ResponseEntity.ok(ApiResponse.success("Results retrieved successfully", results));
    }

//    @GetMapping("/statistics/student/{studentId}")
//    @PreAuthorize("hasAnyRole('STUDENT', 'TRAINER', 'ADMIN')")
//    public ResponseEntity<ApiResponse<StudentStatisticsResponse>> getStudentStatistics(
//            @AuthenticationPrincipal CustomUserDetails currentUser,
//            @PathVariable("studentId") Long studentId) {
//        // If the user is a student, only allow access to their own statistics
//        if (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")) &&
//            !studentId.equals(currentUser.getId())) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(ApiResponse.<StudentStatisticsResponse>builder()
//                            .success(false)
//                            .message("You don't have permission to access these statistics")
//                            .timestamp(java.time.LocalDateTime.now())
//                            .build());
//        }
//
//        StudentStatisticsResponse statistics = resultService.getStudentStatistics(studentId);
//        return ResponseEntity.ok(ApiResponse.success("Student statistics retrieved successfully", statistics));
//    }

    @GetMapping("/average/course/{courseId}")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Double>> getAverageScoreByCourseId(
            @PathVariable("courseId") Long courseId) {
        Double averageScore = resultService.getAverageScoreByCourseId(courseId);
        return ResponseEntity.ok(ApiResponse.success("Average score retrieved successfully", averageScore));
    }

    @GetMapping("/average/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TRAINER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Double>> getAverageScoreByStudentId(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable("studentId") Long studentId) {
        // If the user is a student, only allow access to their own average score
        if (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")) &&
            !studentId.equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<Double>builder()
                            .success(false)
                            .message("You don't have permission to access this information")
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build());
        }
        
        Double averageScore = resultService.getAverageScoreByStudentId(studentId);
        return ResponseEntity.ok(ApiResponse.success("Average score retrieved successfully", averageScore));
    }
}
