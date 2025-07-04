package com.marketplace.trainingcenter.service.impl;

import com.marketplace.trainingcenter.dto.enrollment.EnrollmentRequest;
import com.marketplace.trainingcenter.dto.enrollment.EnrollmentResponse;
import com.marketplace.trainingcenter.dto.enrollment.StudentProgressResponse;
import com.marketplace.trainingcenter.exception.BadRequestException;
import com.marketplace.trainingcenter.exception.ResourceAlreadyExistsException;
import com.marketplace.trainingcenter.exception.ResourceNotFoundException;
import com.marketplace.trainingcenter.model.entity.CompletedModule;
import com.marketplace.trainingcenter.model.entity.Course;
import com.marketplace.trainingcenter.model.entity.Enrollment;
import com.marketplace.trainingcenter.model.entity.User;
import com.marketplace.trainingcenter.model.enums.EnrollmentStatus;
import com.marketplace.trainingcenter.repository.*;
import com.marketplace.trainingcenter.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CompletedModuleRepository completedModuleRepository;
    private final LessonRepository lessonRepository;
    private final ResultRepository resultRepository;

    @Override
    @Transactional
    public EnrollmentResponse enrollInCourse(EnrollmentRequest enrollmentRequest, Long studentId) {
        // Check if course exists
        Course course = courseRepository.findById(enrollmentRequest.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", enrollmentRequest.getCourseId()));
        
        // Check if student exists
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        
        // Check if student is already enrolled in this course
        Optional<Enrollment> existingEnrollment = enrollmentRepository
                .findByStudentIdAndCourseId(studentId, enrollmentRequest.getCourseId());
        
        if (existingEnrollment.isPresent()) {
            // If enrollment exists but is deleted, reactivate it
            if (existingEnrollment.get().getStatus() == EnrollmentStatus.CANCELLED) {
                Enrollment enrollment = existingEnrollment.get();
                enrollment.setStatus(EnrollmentStatus.ACTIVE);
                return mapToEnrollmentResponse(enrollmentRepository.save(enrollment));
            } else {
                throw new ResourceAlreadyExistsException("Enrollment", "courseId", enrollmentRequest.getCourseId().toString());
            }
        }
        
        // Create new enrollment
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return mapToEnrollmentResponse(savedEnrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollmentById(Long id) {
        Enrollment enrollment = getEnrollmentEntityById(id);
        return mapToEnrollmentResponse(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollmentByStudentAndCourse(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "studentId and courseId", studentId + ", " + courseId));
        
        return mapToEnrollmentResponse(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByStudentId(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        return enrollments.stream()
                .map(this::mapToEnrollmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByCourseId(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        return enrollments.stream()
                .map(this::mapToEnrollmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EnrollmentResponse updateEnrollmentStatus(Long id, EnrollmentStatus status) {
        Enrollment enrollment = getEnrollmentEntityById(id);
        enrollment.setStatus(status);
        return mapToEnrollmentResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public void deleteEnrollment(Long id) {
        Enrollment enrollment = getEnrollmentEntityById(id);
        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStudentEnrolledInCourse(Long studentId, Long courseId) {
        Optional<Enrollment> enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);
        return enrollment.isPresent() && enrollment.get().getStatus() == EnrollmentStatus.ACTIVE;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentProgressResponse getStudentProgress(Long studentId, Long courseId) {
        // Check if enrollment exists and is active
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "studentId and courseId", studentId + ", " + courseId));
        
        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new BadRequestException("Enrollment is not active");
        }
        
        // Calculate progress
        Integer totalLessons = lessonRepository.countLessonsByCourseId(courseId);
        Integer completedLessons = completedModuleRepository
                .countCompletedLessonsByStudentIdAndCourseId(studentId, courseId);
        
        double completionPercentage = totalLessons > 0 
                ? ((double) completedLessons / totalLessons) * 100 
                : 0.0;
        
        // Get average score from quiz results if available
        Double averageScore = resultRepository.findAverageScoreByStudentIdAndCourseId(studentId, courseId);
        
        return StudentProgressResponse.builder()
                .enrollmentId(enrollment.getId())
                .courseId(courseId)
                .courseTitle(enrollment.getCourse().getTitle())
                .courseCoverImage(enrollment.getCourse().getCoverImage())
                .completedLessons(completedLessons)
                .totalLessons(totalLessons)
                .completionPercentage(completionPercentage)
                .status(enrollment.getStatus().name())
                .averageScore(averageScore)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentProgressResponse> getStudentProgressForAllCourses(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        
        return enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .map(enrollment -> {
                    Long courseId = enrollment.getCourse().getId();
                    
                    Integer totalLessons = lessonRepository.countLessonsByCourseId(courseId);
                    Integer completedLessons = completedModuleRepository
                            .countCompletedLessonsByStudentIdAndCourseId(studentId, courseId);
                    
                    double completionPercentage = totalLessons > 0 
                            ? ((double) completedLessons / totalLessons) * 100 
                            : 0.0;
                    
                    Double averageScore = resultRepository.findAverageScoreByStudentIdAndCourseId(studentId, courseId);
                    
                    return StudentProgressResponse.builder()
                            .enrollmentId(enrollment.getId())
                            .courseId(courseId)
                            .courseTitle(enrollment.getCourse().getTitle())
                            .courseCoverImage(enrollment.getCourse().getCoverImage())
                            .completedLessons(completedLessons)
                            .totalLessons(totalLessons)
                            .completionPercentage(completionPercentage)
                            .status(enrollment.getStatus().name())
                            .averageScore(averageScore)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Enrollment getEnrollmentEntityById(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateCourseCompletionPercentage(Long studentId, Long courseId) {
        Integer totalLessons = lessonRepository.countLessonsByCourseId(courseId);
        if (totalLessons == 0) {
            return 0.0;
        }
        
        Integer completedLessons = completedModuleRepository
                .countCompletedLessonsByStudentIdAndCourseId(studentId, courseId);
        
        return ((double) completedLessons / totalLessons) * 100;
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateProgressPercentage(Enrollment enrollment) {
        Long studentId = enrollment.getStudent().getId();
        Long courseId = enrollment.getCourse().getId();
        
        Integer totalLessons = lessonRepository.countLessonsByCourseId(courseId);
        if (totalLessons == 0) {
            return 0.0;
        }
        
        Integer completedLessons = completedModuleRepository
                .countCompletedLessonsByStudentIdAndCourseId(studentId, courseId);
        
        return ((double) completedLessons / totalLessons) * 100;
    }

    // Helper method to map Entity to DTO
    private EnrollmentResponse mapToEnrollmentResponse(Enrollment enrollment) {
        Integer totalLessons = lessonRepository.countLessonsByCourseId(enrollment.getCourse().getId());
        Integer completedLessons = 0;
        
        // Only calculate completed lessons for active enrollments
        if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
            completedLessons = completedModuleRepository.countCompletedLessonsByStudentIdAndCourseId(
                    enrollment.getStudent().getId(), enrollment.getCourse().getId());
        }
        
        double progress = totalLessons > 0 ? ((double) completedLessons / totalLessons) * 100 : 0.0;
        
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .studentName(enrollment.getStudent().getFullName())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .courseCoverImage(enrollment.getCourse().getCoverImage())
                .enrollmentDate(enrollment.getCreatedAt())
                .status(enrollment.getStatus())
                .progress(progress)
                .completedLessons(completedLessons)
                .totalLessons(totalLessons)
                .build();
    }
}
