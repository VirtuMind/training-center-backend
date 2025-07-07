package com.marketplace.trainingcenter.service.impl;

import com.marketplace.trainingcenter.dto.course.CourseResponse;
import com.marketplace.trainingcenter.dto.lesson.LessonRequest;
import com.marketplace.trainingcenter.exception.ResourceNotFoundException;
import com.marketplace.trainingcenter.exception.UnauthorizedException;
import com.marketplace.trainingcenter.model.entity.CompletedLesson;
import com.marketplace.trainingcenter.model.entity.Lesson;
import com.marketplace.trainingcenter.model.entity.Module;
import com.marketplace.trainingcenter.model.entity.User;
import com.marketplace.trainingcenter.repository.CompletedLessonRepository;
import com.marketplace.trainingcenter.repository.LessonRepository;
import com.marketplace.trainingcenter.repository.ModuleRepository;
import com.marketplace.trainingcenter.repository.UserRepository;
import com.marketplace.trainingcenter.service.LessonService;
import com.marketplace.trainingcenter.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final CompletedLessonRepository completedLessonRepository;
    private final FileUploadUtil fileUploadUtil;

    @Override
    @Transactional
    public CourseResponse.LessonResponse createLesson(LessonRequest lessonRequest, MultipartFile video) {
        Module module = moduleRepository.findById(lessonRequest.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", lessonRequest.getModuleId()));


        Lesson lesson = Lesson.builder()
                .title(lessonRequest.getTitle())
                .module(module)
                .duration(lessonRequest.getDuration())
                .build();

        // Handle video upload if provided
        if (video != null && !video.isEmpty()) {
            try {
                lesson.setVideoUrl(fileUploadUtil.saveFile(video));
            } catch (Exception e) {
                throw new com.marketplace.trainingcenter.exception.BadRequestException(
                        "Failed to upload video: " + e.getMessage());
            }
        }

        Lesson savedLesson = lessonRepository.save(lesson);
        return mapToLessonResponse(savedLesson, false);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse.LessonResponse getLessonById(Long id, Long studentId) {
        Lesson lesson = getLessonEntityById(id);
        
        boolean completed = false;
        if (studentId != null) {
            Optional<CompletedLesson> completedModule = completedLessonRepository
                    .findByStudentIdAndLessonId(studentId, id);
            completed = completedModule.isPresent();
        }
        
        return mapToLessonResponse(lesson, completed);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse.LessonResponse> getLessonsByModuleId(Long moduleId, Long studentId) {
        // Ensure the module exists
        if (!moduleRepository.existsById(moduleId)) {
            throw new ResourceNotFoundException("Module", "id", moduleId);
        }

        List<Lesson> lessons = lessonRepository.getLessonsByModuleId(moduleId);
        
        return lessons.stream()
                .map(lesson -> {
                    boolean completed = false;
                    if (studentId != null) {
                        Optional<CompletedLesson> completedModule = completedLessonRepository
                                .findByStudentIdAndLessonId(studentId, lesson.getId());
                        completed = completedModule.isPresent();
                    }
                    return mapToLessonResponse(lesson, completed);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CourseResponse.LessonResponse updateLesson(Long id, LessonRequest lessonRequest, MultipartFile video) {
        Lesson lesson = getLessonEntityById(id);
        
        // Validate that the lesson belongs to the specified module
        if (!lesson.getModule().getId().equals(lessonRequest.getModuleId())) {
            throw new UnauthorizedException("Lesson does not belong to the specified module");
        }
        
        lesson.setTitle(lessonRequest.getTitle());
        
        if (lessonRequest.getDuration() != null) {
            lesson.setDuration(lessonRequest.getDuration());
        }

        
        // Handle video update if provided
        if (video != null && !video.isEmpty()) {
            try {
                // Delete old video if exists
                if (lesson.getVideoUrl() != null) {
                    fileUploadUtil.deleteFile(lesson.getVideoUrl());
                }
                
                String videoPath = fileUploadUtil.saveFile(video);
                lesson.setVideoUrl(videoPath);
            } catch (Exception e) {
                throw new com.marketplace.trainingcenter.exception.BadRequestException(
                        "Failed to upload video: " + e.getMessage());
            }
        }
        
        Lesson updatedLesson = lessonRepository.save(lesson);
        
        boolean completed = false;
        if (updatedLesson.getCompletedLessons() != null && !updatedLesson.getCompletedLessons().isEmpty()) {
            completed = true;
        }
        
        return mapToLessonResponse(updatedLesson, completed);
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        Lesson lesson = getLessonEntityById(id);
        
        // Delete video file if exists
        if (lesson.getVideoUrl() != null) {
            try {
                fileUploadUtil.deleteFile(lesson.getVideoUrl());
            } catch (Exception e) {
                // Log the error but continue with deletion
                System.err.println("Failed to delete video file: " + e.getMessage());
            }
        }
        
        lessonRepository.delete(lesson);
    }

    @Override
    @Transactional
    public void markLessonAsCompleted(Long lessonId, Long studentId) {
        Lesson lesson = getLessonEntityById(lessonId);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        
        Optional<CompletedLesson> existingCompletion = completedLessonRepository
                .findByStudentIdAndLessonId(studentId, lessonId);
        
        if (existingCompletion.isEmpty()) {
            CompletedLesson completedModule = CompletedLesson.builder()
                    .student(student)
                    .lesson(lesson)
                    .build();
            
            completedLessonRepository.save(completedModule);
        }
    }

    @Override
    @Transactional
    public void markLessonAsIncomplete(Long lessonId, Long studentId) {
        // Ensure the lesson exists
        getLessonEntityById(lessonId);
        
        // Delete the completion record if exists
        completedLessonRepository.deleteByStudentIdAndLessonId(studentId, lessonId);
    }

    @Override
    @Transactional(readOnly = true)
    public Lesson getLessonEntityById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", id));
    }

    @Override
    @Transactional
    public void reorderLessons(Long moduleId, List<Long> lessonIds) {
        // Ensure the module exists
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));
                
        // Validate that all lessons belong to the module
        List<Lesson> lessons = lessonRepository.findAllById(lessonIds);
        
        if (lessons.size() != lessonIds.size()) {
            throw new ResourceNotFoundException("One or more lessons not found");
        }
        
        for (Lesson lesson : lessons) {
            if (!lesson.getModule().getId().equals(moduleId)) {
                throw new UnauthorizedException("Lesson does not belong to the specified module");
            }
        }
        
        // Update the order indexes
        for (int i = 0; i < lessonIds.size(); i++) {
            Long lessonId = lessonIds.get(i);
            Lesson lesson = lessons.stream()
                    .filter(l -> l.getId().equals(lessonId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

            lessonRepository.save(lesson);
        }
    }

    // Helper method to map Lesson entity to LessonResponse DTO
    private CourseResponse.LessonResponse mapToLessonResponse(Lesson lesson, boolean completed) {
        return CourseResponse.LessonResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .duration(lesson.getDuration())
                .videoUrl(lesson.getVideoUrl())
                .completed(completed)
                .build();
    }
}
