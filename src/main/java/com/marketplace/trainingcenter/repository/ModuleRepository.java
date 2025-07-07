package com.marketplace.trainingcenter.repository;

import com.marketplace.trainingcenter.model.entity.Course;
import com.marketplace.trainingcenter.model.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    List<Module> getModulesByCourseId(Long courseId);
}
