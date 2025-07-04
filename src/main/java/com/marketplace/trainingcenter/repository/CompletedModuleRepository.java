package com.marketplace.trainingcenter.repository;

import com.marketplace.trainingcenter.model.entity.CompletedModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompletedModuleRepository extends JpaRepository<CompletedModule, Long> {

    @Query("SELECT cm FROM CompletedModule cm WHERE cm.student.id = :studentId AND cm.lesson.module.course.id = :courseId")
    List<CompletedModule> findByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    @Query("SELECT cm FROM CompletedModule cm WHERE cm.lesson.module.course.id = :courseId")
    List<CompletedModule> findByCourseId(@Param("courseId") Long courseId);
    
    Optional<CompletedModule> findByStudentIdAndLessonId(Long studentId, Long lessonId);
    
    @Modifying
    @Transactional
    void deleteByStudentIdAndLessonId(Long studentId, Long lessonId);
    
    @Query("SELECT COUNT(cm) FROM CompletedModule cm WHERE cm.student.id = :studentId AND cm.lesson.module.course.id = :courseId")
    Integer countCompletedLessonsByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(cm) FROM CompletedModule cm WHERE cm.student.id = :studentId")
    Integer countCompletedLessonsByStudentId(@Param("studentId") Long studentId);
}
