package com.marketplace.trainingcenter.service.impl;

import com.marketplace.trainingcenter.dto.enrollment.EnrollmentRequest;
import com.marketplace.trainingcenter.dto.enrollment.EnrollmentResponse;
import com.marketplace.trainingcenter.dto.enrollment.LessonToggleResult;
import com.marketplace.trainingcenter.dto.enrollment.StudentProgressResponse;
import com.marketplace.trainingcenter.exception.BadRequestException;
import com.marketplace.trainingcenter.exception.ResourceAlreadyExistsException;
import com.marketplace.trainingcenter.exception.ResourceNotFoundException;
import com.marketplace.trainingcenter.model.entity.Course;
import com.marketplace.trainingcenter.model.entity.Enrollment;
import com.marketplace.trainingcenter.model.entity.User;
import com.marketplace.trainingcenter.model.entity.CompletedLesson;
import com.marketplace.trainingcenter.model.entity.Lesson;
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
    private final CompletedLessonRepository completedLessonRepository;
    private final LessonRepository lessonRepository;
    private final ResultRepository resultRepository;

    @Override
    @Transactional
    public EnrollmentResponse enrollInCourse(Long courseId, Long studentId) {
        // Check if course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        
        // Check if student exists
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        
        // Check if student is already enrolled in this course
        Optional<Enrollment> existingEnrollment = enrollmentRepository
                .findByStudentIdAndCourseId(studentId, courseId);

        if (existingEnrollment.isPresent()) {
            throw new ResourceAlreadyExistsException("Enrollment", "courseId", courseId.toString());
        }
        
        // Create new enrollment
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
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
    public void deleteEnrollment(Long id) {
        Enrollment enrollment = getEnrollmentEntityById(id);
        enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStudentEnrolledInCourse(Long studentId, Long courseId) {
        Optional<Enrollment> enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);
        return enrollment.isPresent();
    }

    @Override
    @Transactional
    public Boolean toggleLessonCompletion(Long studentId, Long lessonId) {

        // Check if lesson exists
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        
        // Check if student exists
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        
        // Get the course ID from the lesson's module
        Long courseId = lesson.getModule().getCourse().getId();
        
        // Check if the student is enrolled in the course containing this lesson
        if (!isStudentEnrolledInCourse(studentId, courseId)) {
            throw new BadRequestException("Student is not enrolled in the course containing this lesson");
        }
        
        // Check if the completed lesson record already exists
        Optional<CompletedLesson> existingCompletedLesson = completedLessonRepository
                .findByStudentIdAndLessonId(studentId, lessonId);
        
        if (existingCompletedLesson.isPresent()) {
            // If it exists, delete it (mark as not completed)
            completedLessonRepository.deleteByStudentIdAndLessonId(studentId, lessonId);
            return false; // Lesson is now marked as not completed
        } else {
            // If it doesn't exist, create it (mark as completed)
            CompletedLesson completedLesson = CompletedLesson.builder()
                    .student(student)
                    .lesson(lesson)
                    .build();
            
            completedLessonRepository.save(completedLesson);
            return true; // Lesson is now marked as completed
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StudentProgressResponse getStudentProgress(Long studentId, Long courseId) {
        // Check if enrollment exists and is active
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "studentId and courseId", studentId + ", " + courseId));

        
        // Calculate progress
        Integer totalLessons = lessonRepository.countLessonsByCourseId(courseId);
        Integer completedLessons = completedLessonRepository
                .countCompletedLessonsByStudentIdAndCourseId(studentId, courseId);
        
        double completionPercentage = totalLessons > 0 
                ? ((double) completedLessons / totalLessons) * 100 
                : 0.0;
        
        // Get average score from quiz results if available
        Double averageScore = resultRepository.findAverageScoreByStudentIdAndCourseId(studentId, courseId);
        
        return StudentProgressResponse.builder()
                .courseTitle(enrollment.getCourse().getTitle())
                .completedLessons(completedLessons)
                .totalLessons(totalLessons)
                .completionPercentage(completionPercentage)
                .averageScore(averageScore)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentProgressResponse> getStudentsProgressByTrainer(Long trainerId) {
        List<Enrollment> enrollments = enrollmentRepository.findByTrainerId(trainerId);

        return enrollments.stream()
                .map(enrollment -> {
                    Long studentId = enrollment.getStudent().getId();
                    Long courseId = enrollment.getCourse().getId();

                    Integer totalLessons = lessonRepository.countLessonsByCourseId(courseId);
                    Integer completedLessons = completedLessonRepository
                            .countCompletedLessonsByStudentIdAndCourseId(studentId, courseId);

                    double completionPercentage = totalLessons > 0
                            ? Math.round(((double) completedLessons / totalLessons) * 10000) / 100.0
                            : 0.0;

                    Double averageScore = resultRepository.findAverageScoreByStudentIdAndCourseId(studentId, courseId);

                    return StudentProgressResponse.builder()
                            .studentFullname(enrollment.getStudent().getFullName())
                            .studentUsername(enrollment.getStudent().getUsername())
                            .courseTitle(enrollment.getCourse().getTitle())
                            .completedLessons(completedLessons)
                            .totalLessons(totalLessons)
                            .completionPercentage(completionPercentage)
                            .averageScore(averageScore)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentProgressResponse> getStudentProgressForAllCourses(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        
        return enrollments.stream()
                .map(enrollment -> {
                    Long courseId = enrollment.getCourse().getId();
                    
                    Integer totalLessons = lessonRepository.countLessonsByCourseId(courseId);
                    Integer completedLessons = completedLessonRepository
                            .countCompletedLessonsByStudentIdAndCourseId(studentId, courseId);
                    
                    double completionPercentage = totalLessons > 0 
                            ? ((double) completedLessons / totalLessons) * 100 
                            : 0.0;
                    
                    Double averageScore = resultRepository.findAverageScoreByStudentIdAndCourseId(studentId, courseId);
                    
                    return StudentProgressResponse.builder()
                            .courseTitle(enrollment.getCourse().getTitle())
                            .completedLessons(completedLessons)
                            .totalLessons(totalLessons)
                            .completionPercentage(completionPercentage)
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
        
        Integer completedLessons = completedLessonRepository
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
        
        Integer completedLessons = completedLessonRepository
                .countCompletedLessonsByStudentIdAndCourseId(studentId, courseId);
        
        return ((double) completedLessons / totalLessons) * 100;
    }

    // Helper method to map Entity to DTO
    private EnrollmentResponse mapToEnrollmentResponse(Enrollment enrollment) {
        Integer totalLessons = lessonRepository.countLessonsByCourseId(enrollment.getCourse().getId());
        Integer completedLessons = completedLessonRepository.countCompletedLessonsByStudentIdAndCourseId(enrollment.getStudent().getId(),
                enrollment.getCourse().getId());
        String trainerFullname = enrollment.getCourse().getTrainer() != null
                ? enrollment.getCourse().getTrainer().getFullName()
                : "N/A";
      double progress = totalLessons > 0 ? Math.round(((double) completedLessons / totalLessons) * 10000) / 100.0 : 0.0;
        
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .studentFullname(enrollment.getStudent().getFullName())
                .courseId(enrollment.getCourse().getId())
                .courseCategory(enrollment.getCourse().getCategory().getName())
                .courseTitle(enrollment.getCourse().getTitle())
                .enrolledAt(enrollment.getCreatedAt())
                .trainerFullname(trainerFullname)
                .progressPercentage(progress)
                .completedLessons(completedLessons)
                .totalLessons(totalLessons)
                .build();
    }
}
