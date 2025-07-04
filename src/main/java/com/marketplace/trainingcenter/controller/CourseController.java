package com.marketplace.trainingcenter.controller;

import com.marketplace.trainingcenter.dto.ApiResponse;
import com.marketplace.trainingcenter.dto.course.CourseRequest;
import com.marketplace.trainingcenter.dto.course.CourseResponse;
import com.marketplace.trainingcenter.model.entity.User;
import com.marketplace.trainingcenter.service.CourseService;
import com.marketplace.trainingcenter.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses() {
        Long studentId = null;
        try {
            User currentUser = userService.getCurrentUserEntity();
            studentId = currentUser.getId();
        } catch (Exception ignored) {
            // User not authenticated, continue without student ID
        }
        
        List<CourseResponse> courses = courseService.getAllCourses(studentId);
        return new ResponseEntity<>(ApiResponse.success(courses), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable Long id) {
        Long studentId = null;
        try {
            User currentUser = userService.getCurrentUserEntity();
            studentId = currentUser.getId();
        } catch (Exception ignored) {
            // User not authenticated, continue without student ID
        }
        
        CourseResponse course = courseService.getCourseById(id, studentId);
        return new ResponseEntity<>(ApiResponse.success(course), HttpStatus.OK);
    }

    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCoursesByTrainer(@PathVariable Long trainerId) {
        Long studentId = null;
        try {
            User currentUser = userService.getCurrentUserEntity();
            studentId = currentUser.getId();
        } catch (Exception ignored) {
            // User not authenticated, continue without student ID
        }
        
        List<CourseResponse> courses = courseService.getCoursesByTrainerId(trainerId, studentId);
        return new ResponseEntity<>(ApiResponse.success(courses), HttpStatus.OK);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCoursesByCategory(@PathVariable Long categoryId) {
        Long studentId = null;
        try {
            User currentUser = userService.getCurrentUserEntity();
            studentId = currentUser.getId();
        } catch (Exception ignored) {
            // User not authenticated, continue without student ID
        }
        
        List<CourseResponse> courses = courseService.getCoursesByCategoryId(categoryId, studentId);
        return new ResponseEntity<>(ApiResponse.success(courses), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> searchCourses(@RequestParam String keyword) {
        Long studentId = null;
        try {
            User currentUser = userService.getCurrentUserEntity();
            studentId = currentUser.getId();
        } catch (Exception ignored) {
            // User not authenticated, continue without student ID
        }
        
        List<CourseResponse> courses = courseService.searchCourses(keyword, studentId);
        return new ResponseEntity<>(ApiResponse.success(courses), HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @RequestPart("course") @Valid CourseRequest courseRequest,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage
    ) {
        User currentUser = userService.getCurrentUserEntity();
        CourseResponse course = courseService.createCourse(courseRequest, coverImage, currentUser.getId());
        return new ResponseEntity<>(ApiResponse.success("Course created successfully", course), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @RequestPart("course") @Valid CourseRequest courseRequest,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage
    ) {
        CourseResponse course = courseService.updateCourse(id, courseRequest, coverImage);
        return new ResponseEntity<>(ApiResponse.success("Course updated successfully", course), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return new ResponseEntity<>(ApiResponse.success("Course deleted successfully", null), HttpStatus.OK);
    }
}
