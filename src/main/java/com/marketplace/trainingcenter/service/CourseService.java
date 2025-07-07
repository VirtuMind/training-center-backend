package com.marketplace.trainingcenter.service;

import com.marketplace.trainingcenter.dto.course.CourseMeta;
import com.marketplace.trainingcenter.dto.course.CourseRequestUpdate;
import com.marketplace.trainingcenter.dto.course.CourseResponse;
import com.marketplace.trainingcenter.dto.course.CourseRequest;
import com.marketplace.trainingcenter.model.entity.Course;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CourseService {

    CourseResponse createCourseWithModulesAndQuiz(CourseRequest courseRequest, Long trainerId, Map<String, MultipartFile> videoMap) throws IOException;
    
    CourseResponse getCourseById(Long id, Long studentId);
    
    CourseResponse getCourseDetailsForEdit(Long id);
    
    List<CourseResponse> getAllCourses(Long studentId);
    
    List<CourseMeta> getCoursesByTrainerId(Long trainerId);
    
    List<CourseResponse> getCoursesByCategoryId(Long categoryId, Long studentId);

    CourseResponse updateCourseWithModulesAndQuiz(Long id, CourseRequestUpdate courseRequest, Map<String, MultipartFile> videoMap) throws IOException;
    
    void deleteCourse(Long id);
    
    Course getCourseEntityById(Long id);
    
    Double getAverageRating(Long courseId);
    
    Integer getEnrollmentCount(Long courseId);
    
    void validateCourseOwnership(Long courseId, Long trainerId);
}
