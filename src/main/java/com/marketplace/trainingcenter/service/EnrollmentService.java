package com.marketplace.trainingcenter.service;

import com.marketplace.trainingcenter.dto.enrollment.EnrollmentRequest;
import com.marketplace.trainingcenter.dto.enrollment.EnrollmentResponse;
import com.marketplace.trainingcenter.dto.enrollment.StudentProgressResponse;
import com.marketplace.trainingcenter.model.entity.Enrollment;
import com.marketplace.trainingcenter.model.enums.EnrollmentStatus;

import java.util.List;

public interface EnrollmentService {

    EnrollmentResponse enrollInCourse(EnrollmentRequest enrollmentRequest, Long studentId);
    
    EnrollmentResponse getEnrollmentById(Long id);
    
    EnrollmentResponse getEnrollmentByStudentAndCourse(Long studentId, Long courseId);
    
    List<EnrollmentResponse> getEnrollmentsByStudentId(Long studentId);
    
    List<EnrollmentResponse> getEnrollmentsByCourseId(Long courseId);
    
    EnrollmentResponse updateEnrollmentStatus(Long id, EnrollmentStatus status);
    
    void deleteEnrollment(Long id);
    
    boolean isStudentEnrolledInCourse(Long studentId, Long courseId);
    
    StudentProgressResponse getStudentProgress(Long studentId, Long courseId);
    
    List<StudentProgressResponse> getStudentProgressForAllCourses(Long studentId);
    
    Enrollment getEnrollmentEntityById(Long id);
    
    double calculateCourseCompletionPercentage(Long studentId, Long courseId);
    
    Double calculateProgressPercentage(Enrollment enrollment);
}
