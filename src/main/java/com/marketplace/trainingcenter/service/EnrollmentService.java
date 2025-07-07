package com.marketplace.trainingcenter.service;

import com.marketplace.trainingcenter.dto.enrollment.EnrollmentRequest;
import com.marketplace.trainingcenter.dto.enrollment.EnrollmentResponse;
import com.marketplace.trainingcenter.dto.enrollment.StudentProgressResponse;
import com.marketplace.trainingcenter.model.entity.Enrollment;

import java.util.List;

public interface EnrollmentService {

    EnrollmentResponse enrollInCourse(Long courseId, Long studentId);
    
    EnrollmentResponse getEnrollmentById(Long id);
    
    EnrollmentResponse getEnrollmentByStudentAndCourse(Long studentId, Long courseId);
    
    List<EnrollmentResponse> getEnrollmentsByStudentId(Long studentId);
    
    List<EnrollmentResponse> getEnrollmentsByCourseId(Long courseId);

    void deleteEnrollment(Long id);
    
    boolean isStudentEnrolledInCourse(Long studentId, Long courseId);

    Boolean toggleLessonCompletion(Long studentId, Long lessonId);
    
    StudentProgressResponse getStudentProgress(Long studentId, Long courseId);

    List<StudentProgressResponse> getStudentsProgressByTrainer(Long trainerId);
    
    List<StudentProgressResponse> getStudentProgressForAllCourses(Long studentId);
    
    Enrollment getEnrollmentEntityById(Long id);
    
    double calculateCourseCompletionPercentage(Long studentId, Long courseId);
    
    Double calculateProgressPercentage(Enrollment enrollment);
}
