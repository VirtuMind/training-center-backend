package com.marketplace.trainingcenter.service;

import com.marketplace.trainingcenter.dto.course.CourseRequest;
import com.marketplace.trainingcenter.dto.course.CourseResponse;
import com.marketplace.trainingcenter.model.entity.Course;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CourseService {

    CourseResponse createCourse(CourseRequest courseRequest, MultipartFile coverImage, Long trainerId);
    
    CourseResponse getCourseById(Long id, Long studentId);
    
    List<CourseResponse> getAllCourses(Long studentId);
    
    List<CourseResponse> getCoursesByTrainerId(Long trainerId, Long studentId);
    
    List<CourseResponse> getCoursesByCategoryId(Long categoryId, Long studentId);
    
    CourseResponse updateCourse(Long id, CourseRequest courseRequest, MultipartFile coverImage);
    
    void deleteCourse(Long id);
    
    Course getCourseEntityById(Long id);
    
    Double getAverageRating(Long courseId);
    
    Integer getEnrollmentCount(Long courseId);
    
    List<CourseResponse> searchCourses(String keyword, Long studentId);
    
    void validateCourseOwnership(Long courseId, Long trainerId);
    
    CourseResponse mapToCourseResponse(Course course);
}
