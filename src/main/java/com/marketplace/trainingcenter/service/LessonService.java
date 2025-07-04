package com.marketplace.trainingcenter.service;

import com.marketplace.trainingcenter.dto.course.CourseResponse;
import com.marketplace.trainingcenter.dto.lesson.LessonRequest;
import com.marketplace.trainingcenter.model.entity.Lesson;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LessonService {

    CourseResponse.LessonResponse createLesson(LessonRequest lessonRequest, MultipartFile video);
    
    CourseResponse.LessonResponse getLessonById(Long id, Long studentId);
    
    List<CourseResponse.LessonResponse> getLessonsByModuleId(Long moduleId, Long studentId);
    
    CourseResponse.LessonResponse updateLesson(Long id, LessonRequest lessonRequest, MultipartFile video);
    
    void deleteLesson(Long id);
    
    void markLessonAsCompleted(Long lessonId, Long studentId);
    
    void markLessonAsIncomplete(Long lessonId, Long studentId);
    
    Lesson getLessonEntityById(Long id);
    
    void reorderLessons(Long moduleId, List<Long> lessonIds);
}
