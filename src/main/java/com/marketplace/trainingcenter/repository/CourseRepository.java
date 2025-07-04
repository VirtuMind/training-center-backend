package com.marketplace.trainingcenter.repository;

import com.marketplace.trainingcenter.model.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByTrainerId(Long trainerId);
    
    List<Course> findByTrainerIdAndDeletedFalse(Long trainerId);
    
    List<Course> findByCategoryId(Long categoryId);
    
    List<Course> findByCategoryIdAndDeletedFalse(Long categoryId);
    
    List<Course> findByDeletedFalse();
    
    @Query("SELECT c FROM Course c WHERE c.title LIKE %:keyword% OR c.description LIKE %:keyword%")
    List<Course> searchCourses(@Param("keyword") String keyword);
    
    @Query("SELECT c FROM Course c WHERE c.deleted = false AND (c.title LIKE %:keyword% OR c.description LIKE %:keyword%)")
    List<Course> searchCoursesNotDeleted(@Param("keyword") String keyword);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId")
    Double getAverageRatingByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
    Integer getEnrollmentCountByCourseId(@Param("courseId") Long courseId);
}
