package com.marketplace.trainingcenter.service.impl;

import com.marketplace.trainingcenter.dto.course.CourseResponse;
import com.marketplace.trainingcenter.dto.module.ModuleRequest;
import com.marketplace.trainingcenter.exception.ResourceNotFoundException;
import com.marketplace.trainingcenter.exception.UnauthorizedException;
import com.marketplace.trainingcenter.model.entity.CompletedModule;
import com.marketplace.trainingcenter.model.entity.Course;
import com.marketplace.trainingcenter.model.entity.Lesson;
import com.marketplace.trainingcenter.model.entity.Module;
import com.marketplace.trainingcenter.repository.CompletedModuleRepository;
import com.marketplace.trainingcenter.repository.CourseRepository;
import com.marketplace.trainingcenter.repository.LessonRepository;
import com.marketplace.trainingcenter.repository.ModuleRepository;
import com.marketplace.trainingcenter.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final CompletedModuleRepository completedModuleRepository;

    @Override
    @Transactional
    public CourseResponse.ModuleResponse createModule(ModuleRequest moduleRequest) {
        Course course = courseRepository.findById(moduleRequest.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", moduleRequest.getCourseId()));

        Integer orderIndex = moduleRequest.getOrderIndex();
        if (orderIndex == null) {
            // If no order index is provided, place it at the end
            Integer maxOrderIndex = moduleRepository.getMaxOrderIndexByCourseId(course.getId());
            orderIndex = (maxOrderIndex != null) ? maxOrderIndex + 1 : 0;
        }

        Module module = Module.builder()
                .title(moduleRequest.getTitle())
                .course(course)
                .orderIndex(orderIndex)
                .build();

        Module savedModule = moduleRepository.save(module);
        return mapToModuleResponse(savedModule, null);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse.ModuleResponse getModuleById(Long id, Long studentId) {
        Module module = getModuleEntityById(id);
        return mapToModuleResponse(module, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse.ModuleResponse> getModulesByCourseId(Long courseId, Long studentId) {
        // Ensure the course exists
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }

        List<Module> modules = moduleRepository.findByCourseIdOrderByOrderIndex(courseId);
        
        // If no student ID is provided, just return the modules without completion info
        if (studentId == null) {
            return modules.stream()
                    .map(module -> mapToModuleResponse(module, null))
                    .collect(Collectors.toList());
        }
        
        // Get all completed lessons for this student and course
        List<CompletedModule> completedModules = completedModuleRepository
                .findByStudentIdAndCourseId(studentId, courseId);
        
        // Create a map of lessonId -> completed status for quick lookup
        Map<Long, Boolean> completionMap = new HashMap<>();
        for (CompletedModule completedModule : completedModules) {
            completionMap.put(completedModule.getLesson().getId(), true);
        }
        
        return modules.stream()
                .map(module -> mapToModuleResponseWithCompletionInfo(module, completionMap))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CourseResponse.ModuleResponse updateModule(Long id, ModuleRequest moduleRequest) {
        Module module = getModuleEntityById(id);
        
        // Validate that the module belongs to the specified course
        if (!module.getCourse().getId().equals(moduleRequest.getCourseId())) {
            throw new UnauthorizedException("Module does not belong to the specified course");
        }
        
        module.setTitle(moduleRequest.getTitle());
        
        // Update order index if provided
        if (moduleRequest.getOrderIndex() != null) {
            module.setOrderIndex(moduleRequest.getOrderIndex());
        }
        
        Module updatedModule = moduleRepository.save(module);
        return mapToModuleResponse(updatedModule, null);
    }

    @Override
    @Transactional
    public void deleteModule(Long id) {
        Module module = getModuleEntityById(id);
        moduleRepository.delete(module);
    }

    @Override
    @Transactional(readOnly = true)
    public Module getModuleEntityById(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", id));
    }

    @Override
    @Transactional
    public void reorderModules(Long courseId, List<Long> moduleIds) {
        // Ensure the course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
                
        // Validate that all modules belong to the course
        List<Module> modules = moduleRepository.findAllById(moduleIds);
        
        if (modules.size() != moduleIds.size()) {
            throw new ResourceNotFoundException("One or more modules not found");
        }
        
        for (Module module : modules) {
            if (!module.getCourse().getId().equals(courseId)) {
                throw new UnauthorizedException("Module does not belong to the specified course");
            }
        }
        
        // Update the order indexes
        for (int i = 0; i < moduleIds.size(); i++) {
            Long moduleId = moduleIds.get(i);
            Module module = modules.stream()
                    .filter(m -> m.getId().equals(moduleId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));
            
            module.setOrderIndex(i);
            moduleRepository.save(module);
        }
    }

    // Helper methods for mapping entities to DTOs
    private CourseResponse.ModuleResponse mapToModuleResponse(Module module, Long studentId) {
        List<Lesson> lessons = lessonRepository.findByModuleIdOrderByOrderIndex(module.getId());
        
        List<CourseResponse.LessonResponse> lessonResponses = new ArrayList<>();
        
        if (studentId != null) {
            // Get completed lessons for this student and module
            for (Lesson lesson : lessons) {
                boolean completed = completedModuleRepository
                        .findByStudentIdAndLessonId(studentId, lesson.getId())
                        .isPresent();
                
                lessonResponses.add(mapToLessonResponse(lesson, completed));
            }
        } else {
            // No student info, just map the lessons
            lessonResponses = lessons.stream()
                    .map(lesson -> mapToLessonResponse(lesson, false))
                    .collect(Collectors.toList());
        }
        
        return CourseResponse.ModuleResponse.builder()
                .id(module.getId())
                .title(module.getTitle())
                .orderIndex(module.getOrderIndex())
                .lessons(lessonResponses)
                .build();
    }
    
    private CourseResponse.ModuleResponse mapToModuleResponseWithCompletionInfo(
            Module module, Map<Long, Boolean> completionMap) {
        List<Lesson> lessons = lessonRepository.findByModuleIdOrderByOrderIndex(module.getId());
        
        List<CourseResponse.LessonResponse> lessonResponses = lessons.stream()
                .map(lesson -> {
                    boolean completed = completionMap.getOrDefault(lesson.getId(), false);
                    return mapToLessonResponse(lesson, completed);
                })
                .collect(Collectors.toList());
        
        return CourseResponse.ModuleResponse.builder()
                .id(module.getId())
                .title(module.getTitle())
                .orderIndex(module.getOrderIndex())
                .lessons(lessonResponses)
                .build();
    }
    
    private CourseResponse.LessonResponse mapToLessonResponse(Lesson lesson, boolean completed) {
        return CourseResponse.LessonResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .duration(lesson.getDuration())
                .video(lesson.getVideo())
                .orderIndex(lesson.getOrderIndex())
                .completed(completed)
                .build();
    }
}
