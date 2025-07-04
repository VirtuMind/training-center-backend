package com.marketplace.trainingcenter.controller;

import com.marketplace.trainingcenter.dto.ApiResponse;
import com.marketplace.trainingcenter.dto.enrollment.EnrollmentRequest;
import com.marketplace.trainingcenter.dto.enrollment.EnrollmentResponse;
import com.marketplace.trainingcenter.dto.enrollment.StudentProgressResponse;
import com.marketplace.trainingcenter.model.enums.EnrollmentStatus;
import com.marketplace.trainingcenter.model.enums.UserRole;
import com.marketplace.trainingcenter.security.CustomUserDetails;
import com.marketplace.trainingcenter.service.CourseService;
import com.marketplace.trainingcenter.service.EnrollmentService;
import java.lang.Void;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollInCourse(
            @Valid @RequestBody EnrollmentRequest enrollmentRequest,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        EnrollmentResponse enrollmentResponse = enrollmentService.enrollInCourse(
                enrollmentRequest, currentUser.getId());
                
        return new ResponseEntity<>(
                ApiResponse.success("Enrolled in course successfully", enrollmentResponse),
                HttpStatus.CREATED
        );
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TRAINER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollmentById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        EnrollmentResponse enrollment = enrollmentService.getEnrollmentById(id);
        
        // Students can only view their own enrollments
        if (currentUser.getRole() == UserRole.STUDENT && 
                !enrollment.getStudentId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<EnrollmentResponse>builder()
                            .success(false)
                            .message("You are not authorized to view this enrollment")
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build());
        }
        
        // Trainers can only view enrollments for their courses
        if (currentUser.getRole() == UserRole.TRAINER) {
            try {
                courseService.validateCourseOwnership(enrollment.getCourseId(), currentUser.getId());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<EnrollmentResponse>builder()
                                .success(false)
                                .message("You are not authorized to view this enrollment")
                                .timestamp(java.time.LocalDateTime.now().toString())
                                .build());
            }
        }
        
        return new ResponseEntity<>(
                ApiResponse.success("Enrollment retrieved successfully", enrollment),
                HttpStatus.OK
        );
    }
    
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getEnrollmentsByStudentId(
            @PathVariable Long studentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        // Students can only view their own enrollments
        if (currentUser.getRole() == UserRole.STUDENT && 
                !studentId.equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<List<EnrollmentResponse>>builder()
                            .success(false)
                            .message("You are not authorized to view these enrollments")
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build());
        }
        
        List<EnrollmentResponse> enrollments = enrollmentService.getEnrollmentsByStudentId(studentId);
        return new ResponseEntity<>(
                ApiResponse.success("Enrollments retrieved successfully", enrollments),
                HttpStatus.OK
        );
    }
    
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getEnrollmentsByCourseId(
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        // Trainers can only view enrollments for their courses
        if (currentUser.getRole() == UserRole.TRAINER) {
            try {
                courseService.validateCourseOwnership(courseId, currentUser.getId());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<List<EnrollmentResponse>>builder()
                                .success(false)
                                .message("You are not authorized to view these enrollments")
                                .timestamp(java.time.LocalDateTime.now().toString())
                                .build());
            }
        }
        
        List<EnrollmentResponse> enrollments = enrollmentService.getEnrollmentsByCourseId(courseId);
        return new ResponseEntity<>(
                ApiResponse.success("Enrollments retrieved successfully", enrollments),
                HttpStatus.OK
        );
    }
    
    @GetMapping("/my-enrollments")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getMyEnrollments(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        List<EnrollmentResponse> enrollments = enrollmentService.getEnrollmentsByStudentId(currentUser.getId());
        return new ResponseEntity<>(
                ApiResponse.success("Your enrollments retrieved successfully", enrollments),
                HttpStatus.OK
        );
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> updateEnrollmentStatus(
            @PathVariable Long id,
            @RequestParam EnrollmentStatus status,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        EnrollmentResponse enrollment = enrollmentService.getEnrollmentById(id);
        
        // Students can only cancel their own enrollments
        if (currentUser.getRole() == UserRole.STUDENT) {
            if (!enrollment.getStudentId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<EnrollmentResponse>builder()
                                .success(false)
                                .message("You are not authorized to modify this enrollment")
                                .timestamp(java.time.LocalDateTime.now().toString())
                                .build());
            }
            
            // Students can only cancel enrollments
            if (status != EnrollmentStatus.CANCELLED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<EnrollmentResponse>builder()
                                .success(false)
                                .message("Students can only cancel their enrollments")
                                .timestamp(java.time.LocalDateTime.now().toString())
                                .build());
            }
        }
        
        EnrollmentResponse updatedEnrollment = enrollmentService.updateEnrollmentStatus(id, status);
        return new ResponseEntity<>(
                ApiResponse.success("Enrollment status updated successfully", updatedEnrollment),
                HttpStatus.OK
        );
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteEnrollment(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        EnrollmentResponse enrollment = enrollmentService.getEnrollmentById(id);
        
        // Students can only delete their own enrollments
        if (currentUser.getRole() == UserRole.STUDENT && 
                !enrollment.getStudentId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<Void>error("FORBIDDEN", "You are not authorized to delete this enrollment"));
        }
        
        enrollmentService.deleteEnrollment(id);
        return new ResponseEntity<>(
                ApiResponse.<Void>success("Enrollment deleted successfully", null),
                HttpStatus.OK
        );
    }
    
    @GetMapping("/progress/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentProgressResponse>> getMyProgress(
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        StudentProgressResponse progress = enrollmentService.getStudentProgress(
                currentUser.getId(), courseId);
                
        return new ResponseEntity<>(
                ApiResponse.success("Progress retrieved successfully", progress),
                HttpStatus.OK
        );
    }
    
    @GetMapping("/progress")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<StudentProgressResponse>>> getAllMyProgress(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        List<StudentProgressResponse> allProgress = enrollmentService.getStudentProgressForAllCourses(
                currentUser.getId());
                
        return new ResponseEntity<>(
                ApiResponse.success("All progress retrieved successfully", allProgress),
                HttpStatus.OK
        );
    }
    
    @GetMapping("/student/{studentId}/course/{courseId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TRAINER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollmentByStudentAndCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        // Students can only view their own enrollments
        if (currentUser.getRole() == UserRole.STUDENT && 
                !studentId.equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<EnrollmentResponse>builder()
                            .success(false)
                            .message("You are not authorized to view this enrollment")
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build());
        }
        
        // Trainers can only view enrollments for their courses
        if (currentUser.getRole() == UserRole.TRAINER) {
            try {
                courseService.validateCourseOwnership(courseId, currentUser.getId());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<EnrollmentResponse>builder()
                                .success(false)
                                .message("You are not authorized to view this enrollment")
                                .timestamp(java.time.LocalDateTime.now().toString())
                                .build());
            }
        }
        
        EnrollmentResponse enrollment = enrollmentService.getEnrollmentByStudentAndCourse(studentId, courseId);
        return new ResponseEntity<>(
                ApiResponse.success("Enrollment retrieved successfully", enrollment),
                HttpStatus.OK
        );
    }
}
