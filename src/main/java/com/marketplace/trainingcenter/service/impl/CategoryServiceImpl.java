package com.marketplace.trainingcenter.service.impl;

import com.marketplace.trainingcenter.dto.category.CategoryRequest;
import com.marketplace.trainingcenter.dto.category.CategoryResponse;
import com.marketplace.trainingcenter.exception.BadRequestException;
import com.marketplace.trainingcenter.exception.ResourceAlreadyExistsException;
import com.marketplace.trainingcenter.exception.ResourceNotFoundException;
import com.marketplace.trainingcenter.model.entity.Category;
import com.marketplace.trainingcenter.repository.CategoryRepository;
import com.marketplace.trainingcenter.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        if (categoryRepository.existsByName(categoryRequest.getName())) {
            throw new ResourceAlreadyExistsException("Category", "name", categoryRequest.getName());
        }

        Category category = Category.builder()
                .name(categoryRequest.getName())
                .description(categoryRequest.getDescription())
                .build();

        Category savedCategory = categoryRepository.save(category);
        return mapToCategoryResponse(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = getCategoryEntityById(id);
        return mapToCategoryResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        Category category = getCategoryEntityById(id);

        // Check if name is already taken by another category
        if (!category.getName().equals(categoryRequest.getName()) && 
                categoryRepository.existsByName(categoryRequest.getName())) {
            throw new ResourceAlreadyExistsException("Category", "name", categoryRequest.getName());
        }

        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        return mapToCategoryResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryEntityById(id);
        
        // Check if category has associated courses
        if (!category.getCourses().isEmpty()) {
            throw new BadRequestException("Cannot delete category with associated courses");
        }
        
        categoryRepository.delete(category);
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .courseCount(category.getCourses().size())
                .build();
    }
}
