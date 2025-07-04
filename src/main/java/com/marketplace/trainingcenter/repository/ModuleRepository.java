package com.marketplace.trainingcenter.repository;

import com.marketplace.trainingcenter.model.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    List<Module> findByCourseIdOrderByOrderIndex(Long courseId);
    
    Integer countByCourseId(Long courseId);
    
    @Query("SELECT MAX(m.orderIndex) FROM Module m WHERE m.course.id = :courseId")
    Integer getMaxOrderIndexByCourseId(@Param("courseId") Long courseId);
}
