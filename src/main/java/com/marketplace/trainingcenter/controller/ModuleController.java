package com.marketplace.trainingcenter.controller;

import com.marketplace.trainingcenter.dto.ApiResponse;
import com.marketplace.trainingcenter.dto.course.CourseResponse;
import com.marketplace.trainingcenter.dto.module.ModuleRequest;
import com.marketplace.trainingcenter.dto.module.ReorderModulesRequest;
import com.marketplace.trainingcenter.model.entity.User;
import com.marketplace.trainingcenter.model.enums.UserRole;
import com.marketplace.trainingcenter.security.CustomUserDetails;
import com.marketplace.trainingcenter.service.CourseService;
import com.marketplace.trainingcenter.service.ModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;
    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse.ModuleResponse>> createModule(
            @Valid @RequestBody ModuleRequest moduleRequest,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        // Verify that the trainer is the owner of the course or is an admin
        if (currentUser.getRole() == UserRole.TRAINER) {
            courseService.validateCourseOwnership(moduleRequest.getCourseId(), currentUser.getId());
        }
        
        CourseResponse.ModuleResponse createdModule = moduleService.createModule(moduleRequest);
        return new ResponseEntity<>(
                ApiResponse.success("Module created successfully", createdModule),
                HttpStatus.CREATED
        );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse.ModuleResponse>> getModuleById(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        
        // For students, track completion status
        Long studentId = currentUser != null && currentUser.getRole() == UserRole.STUDENT ? 
                currentUser.getId() : null;
        
        CourseResponse.ModuleResponse moduleResponse = moduleService.getModuleById(id, studentId);
        return new ResponseEntity<>(
                ApiResponse.success("Module retrieved successfully", moduleResponse),
                HttpStatus.OK
        );
    }
    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<CourseResponse.ModuleResponse>>> getModulesByCourseId(
            @PathVariable Long courseId,
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        
        // For students, track completion status
        Long studentId = currentUser != null && currentUser.getRole() == UserRole.STUDENT ? 
                currentUser.getId() : null;
        
        List<CourseResponse.ModuleResponse> modules = moduleService.getModulesByCourseId(courseId, studentId);
        return new ResponseEntity<>(
                ApiResponse.success("Modules retrieved successfully", modules),
                HttpStatus.OK
        );
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse.ModuleResponse>> updateModule(
            @PathVariable Long id,
            @Valid @RequestBody ModuleRequest moduleRequest,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        // Verify that the trainer is the owner of the course or is an admin
        if (currentUser.getRole() == UserRole.TRAINER) {
            courseService.validateCourseOwnership(moduleRequest.getCourseId(), currentUser.getId());
        }
        
        CourseResponse.ModuleResponse updatedModule = moduleService.updateModule(id, moduleRequest);
        return new ResponseEntity<>(
                ApiResponse.success("Module updated successfully", updatedModule),
                HttpStatus.OK
        );
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteModule(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        // Get the module first to check course ownership
        Long courseId = moduleService.getModuleEntityById(id).getCourse().getId();
        
        // Verify that the trainer is the owner of the course or is an admin
        if (currentUser.getRole() == UserRole.TRAINER) {
            courseService.validateCourseOwnership(courseId, currentUser.getId());
        }
        
        moduleService.deleteModule(id);
        return new ResponseEntity<>(
                ApiResponse.success("Module deleted successfully", null),
                HttpStatus.OK
        );
    }
    
    @PostMapping("/reorder")
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> reorderModules(
            @Valid @RequestBody ReorderModulesRequest reorderRequest,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        // Verify that the trainer is the owner of the course or is an admin
        if (currentUser.getRole() == UserRole.TRAINER) {
            courseService.validateCourseOwnership(reorderRequest.getCourseId(), currentUser.getId());
        }
        
        moduleService.reorderModules(reorderRequest.getCourseId(), reorderRequest.getModuleIds());
        return new ResponseEntity<>(
                ApiResponse.success("Modules reordered successfully", null),
                HttpStatus.OK
        );
    }
}
