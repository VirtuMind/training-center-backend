package com.marketplace.trainingcenter.repository;

import com.marketplace.trainingcenter.model.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    Optional<Result> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    List<Result> findByStudentId(Long studentId);
    
    List<Result> findByCourseId(Long courseId);
    
    @Query("SELECT AVG(r.score) FROM Result r WHERE r.student.id = :studentId")
    Double getAverageScoreByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT AVG(r.score) FROM Result r WHERE r.course.id = :courseId")
    Double getAverageScoreByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT AVG(r.score) FROM Result r WHERE r.student.id = :studentId AND r.course.id = :courseId")
    Double findAverageScoreByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
