package com.marketplace.trainingcenter.controller;

import com.marketplace.trainingcenter.dto.ApiResponse;
import com.marketplace.trainingcenter.dto.course.CourseResponse;
import com.marketplace.trainingcenter.dto.lesson.LessonRequest;
import com.marketplace.trainingcenter.dto.lesson.ReorderLessonsRequest;
import java.lang.Void;
import com.marketplace.trainingcenter.model.entity.Module;
import com.marketplace.trainingcenter.model.entity.User;
import com.marketplace.trainingcenter.model.enums.UserRole;
import com.marketplace.trainingcenter.security.CustomUserDetails;
import com.marketplace.trainingcenter.service.CourseService;
import com.marketplace.trainingcenter.service.LessonService;
import com.marketplace.trainingcenter.service.ModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    private final ModuleService moduleService;
    private final CourseService courseService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse.LessonResponse>> createLesson(
            @Valid @ModelAttribute LessonRequest lessonRequest,
            @RequestParam(required = false) MultipartFile video,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        // Get module to check course ownership
        Module module = moduleService.getModuleEntityById(lessonRequest.getModuleId());
        Long courseId = module.getCourse().getId();
        
        // Verify that the trainer is the owner of the course or is an admin
        if (currentUser.getRole() == UserRole.TRAINER) {
            courseService.validateCourseOwnership(courseId, currentUser.getId());
        }
        
        CourseResponse.LessonResponse createdLesson = lessonService.createLesson(lessonRequest, video);
        return new ResponseEntity<>(
                ApiResponse.success("Lesson created successfully", createdLesson),
                HttpStatus.CREATED
        );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse.LessonResponse>> getLessonById(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        
        // For students, track completion status
        Long studentId = currentUser != null && currentUser.getRole() == UserRole.STUDENT ? 
                currentUser.getId() : null;
        
        CourseResponse.LessonResponse lessonResponse = lessonService.getLessonById(id, studentId);
        return new ResponseEntity<>(
                ApiResponse.success("Lesson retrieved successfully", lessonResponse),
                HttpStatus.OK
        );
    }
    
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<ApiResponse<List<CourseResponse.LessonResponse>>> getLessonsByModuleId(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        
        // For students, track completion status
        Long studentId = currentUser != null && currentUser.getRole() == UserRole.STUDENT ? 
                currentUser.getId() : null;
        
        List<CourseResponse.LessonResponse> lessons = lessonService.getLessonsByModuleId(moduleId, studentId);
        return new ResponseEntity<>(
                ApiResponse.success("Lessons retrieved successfully", lessons),
                HttpStatus.OK
        );
    }
    
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse.LessonResponse>> updateLesson(
            @PathVariable Long id,
            @Valid @ModelAttribute LessonRequest lessonRequest,
            @RequestParam(required = false) MultipartFile video,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        // Get module to check course ownership
        Module module = moduleService.getModuleEntityById(lessonRequest.getModuleId());
        Long courseId = module.getCourse().getId();
        
        // Verify that the trainer is the owner of the course or is an admin
        if (currentUser.getRole() == UserRole.TRAINER) {
            courseService.validateCourseOwnership(courseId, currentUser.getId());
        }
        
        CourseResponse.LessonResponse updatedLesson = lessonService.updateLesson(id, lessonRequest, video);
        return new ResponseEntity<>(
                ApiResponse.success("Lesson updated successfully", updatedLesson),
                HttpStatus.OK
        );
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteLesson(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        // Get the lesson's module to check course ownership
        Long moduleId = lessonService.getLessonEntityById(id).getModule().getId();
        Module module = moduleService.getModuleEntityById(moduleId);
        Long courseId = module.getCourse().getId();
        
        // Verify that the trainer is the owner of the course or is an admin
        if (currentUser.getRole() == UserRole.TRAINER) {
            courseService.validateCourseOwnership(courseId, currentUser.getId());
        }
        
        lessonService.deleteLesson(id);
        return new ResponseEntity<>(
                ApiResponse.<Void>success("Lesson deleted successfully", null),
                HttpStatus.OK
        );
    }
    
    @PostMapping("/reorder")
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> reorderLessons(
            @Valid @RequestBody ReorderLessonsRequest reorderRequest,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        // Get module to check course ownership
        Module module = moduleService.getModuleEntityById(reorderRequest.getModuleId());
        Long courseId = module.getCourse().getId();
        
        // Verify that the trainer is the owner of the course or is an admin
        if (currentUser.getRole() == UserRole.TRAINER) {
            courseService.validateCourseOwnership(courseId, currentUser.getId());
        }
        
        lessonService.reorderLessons(reorderRequest.getModuleId(), reorderRequest.getLessonIds());
        return new ResponseEntity<>(
                ApiResponse.<Void>success("Lessons reordered successfully", null),
                HttpStatus.OK
        );
    }
    
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<?>> markLessonAsCompleted(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        lessonService.markLessonAsCompleted(id, currentUser.getId());
        return new ResponseEntity<>(
                ApiResponse.<Void>success("Lesson marked as completed", null),
                HttpStatus.OK
        );
    }
    
    @PostMapping("/{id}/incomplete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<?>> markLessonAsIncomplete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        lessonService.markLessonAsIncomplete(id, currentUser.getId());
        return new ResponseEntity<>(
                ApiResponse.<Void>success("Lesson marked as incomplete", null),
                HttpStatus.OK
        );
    }
}
