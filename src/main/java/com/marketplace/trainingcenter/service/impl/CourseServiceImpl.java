package com.marketplace.trainingcenter.service.impl;

import com.marketplace.trainingcenter.dto.course.CourseRequest;
import com.marketplace.trainingcenter.dto.course.CourseResponse;
import com.marketplace.trainingcenter.exception.BadRequestException;
import com.marketplace.trainingcenter.exception.ResourceNotFoundException;
import com.marketplace.trainingcenter.model.entity.*;
import com.marketplace.trainingcenter.model.entity.Module;
import com.marketplace.trainingcenter.repository.*;
import com.marketplace.trainingcenter.service.CourseService;
import com.marketplace.trainingcenter.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final CompletedModuleRepository completedModuleRepository;
    private final ReviewRepository reviewRepository;
    private final FileUploadUtil fileUploadUtil;

    @Override
    @Transactional
    public CourseResponse createCourse(CourseRequest courseRequest, MultipartFile coverImage, Long trainerId) {
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", trainerId));
        
        Category category = categoryRepository.findById(courseRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", courseRequest.getCategoryId()));

        Course course = Course.builder()
                .title(courseRequest.getTitle())
                .description(courseRequest.getDescription())
                .level(courseRequest.getLevel())
                .duration(courseRequest.getDuration())
                .trainer(trainer)
                .category(category)
                .build();

        // Upload cover image if provided
        if (coverImage != null && !coverImage.isEmpty()) {
            try {
                String filePath = fileUploadUtil.saveFile(coverImage);
                course.setCoverImage(filePath);
            } catch (IOException e) {
                throw new BadRequestException("Error uploading cover image: " + e.getMessage());
            }
        }

        Course savedCourse = courseRepository.save(course);
        return mapToCourseResponse(savedCourse, null);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id, Long studentId) {
        Course course = getCourseEntityById(id);
        return mapToCourseDetailResponse(course, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses(Long studentId) {
        List<Course> courses = courseRepository.findByDeletedFalse();
        return courses.stream()
                .map(course -> mapToCourseResponse(course, studentId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByTrainerId(Long trainerId, Long studentId) {
        List<Course> courses = courseRepository.findByTrainerIdAndDeletedFalse(trainerId);
        return courses.stream()
                .map(course -> mapToCourseResponse(course, studentId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByCategoryId(Long categoryId, Long studentId) {
        List<Course> courses = courseRepository.findByCategoryIdAndDeletedFalse(categoryId);
        return courses.stream()
                .map(course -> mapToCourseResponse(course, studentId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest courseRequest, MultipartFile coverImage) {
        Course course = getCourseEntityById(id);
        
        Category category = categoryRepository.findById(courseRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", courseRequest.getCategoryId()));

        course.setTitle(courseRequest.getTitle());
        course.setDescription(courseRequest.getDescription());
        course.setLevel(courseRequest.getLevel());
        course.setDuration(courseRequest.getDuration());
        course.setCategory(category);

        // Update cover image if provided
        if (coverImage != null && !coverImage.isEmpty()) {
            try {
                // Delete old cover image if exists
                if (course.getCoverImage() != null) {
                    fileUploadUtil.deleteFile(course.getCoverImage());
                }
                
                String filePath = fileUploadUtil.saveFile(coverImage);
                course.setCoverImage(filePath);
            } catch (IOException e) {
                throw new BadRequestException("Error uploading cover image: " + e.getMessage());
            }
        }

        Course updatedCourse = courseRepository.save(course);
        return mapToCourseResponse(updatedCourse, null);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourseEntityById(id);
        course.setDeleted(true);
        courseRepository.save(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Course getCourseEntityById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Long courseId) {
        return reviewRepository.getAverageRatingByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getEnrollmentCount(Long courseId) {
        return courseRepository.getEnrollmentCountByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> searchCourses(String keyword, Long studentId) {
        List<Course> courses = courseRepository.searchCoursesNotDeleted(keyword);
        return courses.stream()
                .map(course -> mapToCourseResponse(course, studentId))
                .collect(Collectors.toList());
    }

    private CourseResponse mapToCourseResponse(Course course, Long studentId) {
        Double averageRating = reviewRepository.getAverageRatingByCourseId(course.getId());
        Integer enrollmentsCount = courseRepository.getEnrollmentCountByCourseId(course.getId());
        Integer moduleCount = moduleRepository.countByCourseId(course.getId());
        Integer lessonCount = lessonRepository.countLessonsByCourseId(course.getId());

        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .level(course.getLevel().name())
                .duration(course.getDuration())
                .coverImage(course.getCoverImage())
                .categoryId(course.getCategory().getId())
                .categoryName(course.getCategory().getName())
                .trainer(CourseResponse.TrainerInfo.builder()
                        .id(course.getTrainer().getId())
                        .username(course.getTrainer().getUsername())
                        .fullName(course.getTrainer().getFullName())
                        .build())
                .createdAt(course.getCreatedAt())
                .averageRating(averageRating)
                .enrollmentsCount(enrollmentsCount)
                .moduleCount(moduleCount)
                .lessonCount(lessonCount)
                .build();
    }

    private CourseResponse mapToCourseDetailResponse(Course course, Long studentId) {
        CourseResponse courseResponse = mapToCourseResponse(course, studentId);
        
        List<CourseResponse.ModuleResponse> moduleResponses = new ArrayList<>();
        List<Module> modules = moduleRepository.findByCourseIdOrderByOrderIndex(course.getId());
        
        for (Module module : modules) {
            List<CourseResponse.LessonResponse> lessonResponses = new ArrayList<>();
            List<Lesson> lessons = lessonRepository.findByModuleIdOrderByOrderIndex(module.getId());
            
            for (Lesson lesson : lessons) {
                boolean completed = false;
                if (studentId != null) {
                    completed = completedModuleRepository.findByStudentIdAndLessonId(studentId, lesson.getId()).isPresent();
                }
                
                lessonResponses.add(CourseResponse.LessonResponse.builder()
                        .id(lesson.getId())
                        .title(lesson.getTitle())
                        .duration(lesson.getDuration())
                        .video(lesson.getVideo())
                        .orderIndex(lesson.getOrderIndex())
                        .completed(completed)
                        .build());
            }
            
            moduleResponses.add(CourseResponse.ModuleResponse.builder()
                    .id(module.getId())
                    .title(module.getTitle())
                    .orderIndex(module.getOrderIndex())
                    .lessons(lessonResponses)
                    .build());
        }
        
        courseResponse.setModules(moduleResponses);
        return courseResponse;
    }
    
    @Override
    @Transactional(readOnly = true)
    public void validateCourseOwnership(Long courseId, Long trainerId) {
        Course course = getCourseEntityById(courseId);
        
        if (!course.getTrainer().getId().equals(trainerId)) {
            throw new com.marketplace.trainingcenter.exception.UnauthorizedException(
                "You are not authorized to modify this course");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse mapToCourseResponse(Course course) {
        return mapToCourseResponse(course, null);
    }
}
