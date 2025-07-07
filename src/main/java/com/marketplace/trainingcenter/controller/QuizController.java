package com.marketplace.trainingcenter.controller;

import com.marketplace.trainingcenter.dto.ApiResponse;
import com.marketplace.trainingcenter.dto.quiz.QuizResponse;
import com.marketplace.trainingcenter.dto.result.QuizSubmissionRequest;
import com.marketplace.trainingcenter.dto.result.ResultResponse;
import com.marketplace.trainingcenter.model.entity.Result;
import com.marketplace.trainingcenter.security.CustomUserDetails;
import com.marketplace.trainingcenter.service.EnrollmentService;
import com.marketplace.trainingcenter.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final EnrollmentService enrollmentService;

    @GetMapping("{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuizByCourseId(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long courseId) {

        // ensure the student is enrolled in the course
        Long userId = currentUser.getId();
        if(!enrollmentService.isStudentEnrolledInCourse(userId, courseId))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<QuizResponse>builder()
                            .success(false)
                            .message("You must be enrolled in the course to access the quiz")
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build());

        QuizResponse quiz = quizService.getQuizByCourseId(courseId);

        if (quiz == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<QuizResponse>builder()
                            .success(false)
                            .message("Quiz not found for the specified course")
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build());

        return ResponseEntity.ok(ApiResponse.success("Quiz retrieved successfully", quiz));
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Boolean>> submitQuiz(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody QuizSubmissionRequest quizSubmission) {
        Boolean result = quizService.submitQuiz(currentUser.getId(), quizSubmission);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quiz submitted successfully", result));
    }

}
