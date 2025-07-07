package com.marketplace.trainingcenter.repository;

import com.marketplace.trainingcenter.model.entity.CompletedLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
    public interface CompletedLessonRepository extends JpaRepository<CompletedLesson, Long> {

    @Query("SELECT cm FROM CompletedLesson cm WHERE cm.student.id = :studentId AND cm.lesson.module.course.id = :courseId")
    List<CompletedLesson> findByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    @Query("SELECT cm FROM CompletedLesson cm WHERE cm.lesson.module.course.id = :courseId")
    List<CompletedLesson> findByCourseId(@Param("courseId") Long courseId);
    
    Optional<CompletedLesson> findByStudentIdAndLessonId(Long studentId, Long lessonId);
    
    @Modifying
    @Transactional
    void deleteByStudentIdAndLessonId(Long studentId, Long lessonId);
    
    @Query("SELECT COUNT(cm) FROM CompletedLesson cm WHERE cm.student.id = :studentId AND cm.lesson.module.course.id = :courseId")
    Integer countCompletedLessonsByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(cm) FROM CompletedLesson cm WHERE cm.student.id = :studentId")
    Integer countCompletedLessonsByStudentId(@Param("studentId") Long studentId);
}
