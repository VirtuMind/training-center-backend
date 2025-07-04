package com.marketplace.trainingcenter.service;

import com.marketplace.trainingcenter.dto.module.ModuleRequest;
import com.marketplace.trainingcenter.dto.course.CourseResponse;
import com.marketplace.trainingcenter.model.entity.Module;

import java.util.List;

public interface ModuleService {

    CourseResponse.ModuleResponse createModule(ModuleRequest moduleRequest);
    
    CourseResponse.ModuleResponse getModuleById(Long id, Long studentId);
    
    List<CourseResponse.ModuleResponse> getModulesByCourseId(Long courseId, Long studentId);
    
    CourseResponse.ModuleResponse updateModule(Long id, ModuleRequest moduleRequest);
    
    void deleteModule(Long id);
    
    Module getModuleEntityById(Long id);
    
    void reorderModules(Long courseId, List<Long> moduleIds);
}
