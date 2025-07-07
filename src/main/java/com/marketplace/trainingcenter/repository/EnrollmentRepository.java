package com.marketplace.trainingcenter.repository;

import com.marketplace.trainingcenter.model.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    List<Enrollment> findByStudentId(Long studentId);
    
    List<Enrollment> findByCourseId(Long courseId);
    
    List<Enrollment> findByCourseIdIn(List<Long> courseIds);
    
    Integer countByStudentIdAndCourseId(Long studentId, Long courseId);
    
    Long countByCourseId(Long courseId);
    
    Long countByCourseIdIn(List<Long> courseIds);
    
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.trainer.id = :trainerId")
    Integer countByTrainerId(@Param("trainerId") Long trainerId);

    // get all enrollments for a courses of a trainer
    @Query("SELECT e FROM Enrollment e WHERE e.course.trainer.id = :trainerId")
    List<Enrollment> findByTrainerId(@Param("trainerId") Long trainerId);

}
