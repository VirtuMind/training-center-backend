package com.marketplace.trainingcenter.repository;

import com.marketplace.trainingcenter.model.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByModuleIdOrderByOrderIndex(Long moduleId);
    
    Integer countByModuleId(Long moduleId);
    
    @Query("SELECT MAX(l.orderIndex) FROM Lesson l WHERE l.module.id = :moduleId")
    Integer getMaxOrderIndexByModuleId(@Param("moduleId") Long moduleId);
    
    @Query("SELECT COUNT(l) FROM Lesson l JOIN l.module m WHERE m.course.id = :courseId")
    Integer countLessonsByCourseId(@Param("courseId") Long courseId);
}
