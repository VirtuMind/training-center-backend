package com.marketplace.trainingcenter.controller;

import com.marketplace.trainingcenter.dto.ApiResponse;
import com.marketplace.trainingcenter.dto.review.ReviewRequest;
import com.marketplace.trainingcenter.dto.review.ReviewResponse;
import com.marketplace.trainingcenter.model.enums.UserRole;
import com.marketplace.trainingcenter.security.CustomUserDetails;
import com.marketplace.trainingcenter.service.EnrollmentService;
import com.marketplace.trainingcenter.service.ReviewService;
import java.lang.Void;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewRequest reviewRequest,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        // Check if student is enrolled in the course
        if (!enrollmentService.isStudentEnrolledInCourse(currentUser.getId(), reviewRequest.getCourseId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<ReviewResponse>builder()
                            .success(false)
                            .message("You must be enrolled in the course to leave a review")
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build());
        }
        
        // Check if student has already reviewed this course
        if (reviewService.hasUserReviewedCourse(currentUser.getId(), reviewRequest.getCourseId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<ReviewResponse>builder()
                            .success(false)
                            .message("You have already reviewed this course")
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build());
        }
        
        ReviewResponse createdReview = reviewService.createReview(reviewRequest, currentUser.getId());
        return new ResponseEntity<>(
                ApiResponse.success("Review created successfully", createdReview),
                HttpStatus.CREATED
        );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(@PathVariable Long id) {
        ReviewResponse review = reviewService.getReviewById(id);
        return new ResponseEntity<>(
                ApiResponse.success("Review retrieved successfully", review),
                HttpStatus.OK
        );
    }
    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByCourseId(@PathVariable Long courseId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByCourseId(courseId);
        return new ResponseEntity<>(
                ApiResponse.success("Reviews retrieved successfully", reviews),
                HttpStatus.OK
        );
    }
    
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByStudentId(
            @PathVariable Long studentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        // Students can only view their own reviews
        if (currentUser.getRole() == UserRole.STUDENT && 
                !studentId.equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<List<ReviewResponse>>builder()
                            .success(false)
                            .message("You are not authorized to view these reviews")
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build());
        }
        
        List<ReviewResponse> reviews = reviewService.getReviewsByStudentId(studentId);
        return new ResponseEntity<>(
                ApiResponse.success("Reviews retrieved successfully", reviews),
                HttpStatus.OK
        );
    }
    
    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        List<ReviewResponse> reviews = reviewService.getReviewsByStudentId(currentUser.getId());
        return new ResponseEntity<>(
                ApiResponse.success("Your reviews retrieved successfully", reviews),
                HttpStatus.OK
        );
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest reviewRequest,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        ReviewResponse existingReview = reviewService.getReviewById(id);
        
        // Students can only update their own reviews
        if (currentUser.getRole() == UserRole.STUDENT && 
                !existingReview.getStudentId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<ReviewResponse>builder()
                            .success(false)
                            .message("You are not authorized to update this review")
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build());
        }
        
        ReviewResponse updatedReview = reviewService.updateReview(id, reviewRequest);
        return new ResponseEntity<>(
                ApiResponse.success("Review updated successfully", updatedReview),
                HttpStatus.OK
        );
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        ReviewResponse existingReview = reviewService.getReviewById(id);
        
        // Students can only delete their own reviews
        if (currentUser.getRole() == UserRole.STUDENT && 
                !existingReview.getStudentId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<Void>error("FORBIDDEN", "You are not authorized to delete this review"));
        }
        
        reviewService.deleteReview(id);
        return new ResponseEntity<>(
                ApiResponse.<Void>success("Review deleted successfully", null),
                HttpStatus.OK
        );
    }
    
    @GetMapping("/course/{courseId}/average-rating")
    public ResponseEntity<ApiResponse<Double>> getAverageRatingByCourseId(@PathVariable Long courseId) {
        Double averageRating = reviewService.getAverageRatingByCourseId(courseId);
        return new ResponseEntity<>(
                ApiResponse.success("Average rating retrieved successfully", averageRating),
                HttpStatus.OK
        );
    }
    
    @GetMapping("/course/{courseId}/has-reviewed")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Boolean>> hasUserReviewedCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        Boolean hasReviewed = reviewService.hasUserReviewedCourse(currentUser.getId(), courseId);
        return new ResponseEntity<>(
                ApiResponse.success("Review check completed", hasReviewed),
                HttpStatus.OK
        );
    }
}
