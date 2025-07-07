package com.marketplace.trainingcenter.controller;

import com.marketplace.trainingcenter.dto.ApiResponse;
import com.marketplace.trainingcenter.dto.course.CourseMeta;
import com.marketplace.trainingcenter.dto.course.CourseRequestUpdate;
import com.marketplace.trainingcenter.dto.course.CourseResponse;
import com.marketplace.trainingcenter.dto.course.CourseRequest;
import com.marketplace.trainingcenter.model.entity.User;
import com.marketplace.trainingcenter.service.CourseService;
import com.marketplace.trainingcenter.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
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

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable Long courseId) {
        Long studentId = null;
        try {
            User currentUser = userService.getCurrentUserEntity();
            studentId = currentUser.getId();
        } catch (Exception ignored) {
            // User not authenticated, continue without student ID
        }
        
        CourseResponse course = courseService.getCourseById(courseId, studentId);
        return new ResponseEntity<>(ApiResponse.success(course), HttpStatus.OK);
    }

    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<ApiResponse<List<CourseMeta>>> getCoursesByTrainer(@PathVariable Long trainerId) {
        
        List<CourseMeta> courses = courseService.getCoursesByTrainerId(trainerId);
        return new ResponseEntity<>(ApiResponse.success(courses), HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@ModelAttribute CourseRequest courseRequest, HttpServletRequest request) throws IOException {
        // Extract video files from the request
        Map<String, MultipartFile> videoFiles = new HashMap<>();
        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            multipartRequest.getFileMap().forEach((key, file) -> {
                if (key.startsWith("video_")) {
                    videoFiles.put(key, file);
                }
            });
        }
        User currentUser = userService.getCurrentUserEntity();
        CourseResponse course = courseService.createCourseWithModulesAndQuiz(courseRequest, currentUser.getId(), videoFiles);
        return new ResponseEntity<>(ApiResponse.success("Course created successfully with modules and quiz", course), HttpStatus.CREATED);
    }

    @GetMapping("/trainer/details/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseDetailsForEdit(@PathVariable Long id) {
        User currentUser = userService.getCurrentUserEntity();
        courseService.validateCourseOwnership(id, currentUser.getId());
        CourseResponse course = courseService.getCourseDetailsForEdit(id);
        return new ResponseEntity<>(ApiResponse.success("Course details retrieved successfully", course), HttpStatus.OK);
    }

    @PutMapping(value ="/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @ModelAttribute @Valid CourseRequestUpdate courseRequest,
            HttpServletRequest request
    ) throws IOException {
        // Extract video files from the request
        Map<String, MultipartFile> videoFiles = new HashMap<>();
        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            multipartRequest.getFileMap().forEach((key, file) -> {
                if (key.startsWith("video_")) {
                    videoFiles.put(key, file);
                }
            });
        }
        User currentUser = userService.getCurrentUserEntity();
        courseService.validateCourseOwnership(id, currentUser.getId());
        CourseResponse course = courseService.updateCourseWithModulesAndQuiz(id, courseRequest, videoFiles);
        return new ResponseEntity<>(ApiResponse.success("Course updated successfully with modules and quiz", course), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<ApiResponse<?>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return new ResponseEntity<>(ApiResponse.success("Course deleted successfully", null), HttpStatus.OK);
    }


}
