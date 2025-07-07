package com.marketplace.trainingcenter.repository;

import com.marketplace.trainingcenter.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByCourseId(Long courseId);
    
    List<Review> findByStudentId(Long studentId);
    
    List<Review> findByCourseIdIn(List<Long> courseIds);
    
    Optional<Review> findByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.course.id = :courseId")
    Double getAverageRatingByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.trainer.id = :trainerId")
    Double getAverageRatingByTrainerId(@Param("trainerId") Long trainerId);
}
