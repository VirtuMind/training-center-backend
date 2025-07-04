package com.marketplace.trainingcenter.service;

import com.marketplace.trainingcenter.dto.category.CategoryRequest;
import com.marketplace.trainingcenter.dto.category.CategoryResponse;
import com.marketplace.trainingcenter.model.entity.Category;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest categoryRequest);
    
    CategoryResponse getCategoryById(Long id);
    
    List<CategoryResponse> getAllCategories();
    
    CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest);
    
    void deleteCategory(Long id);
    
    boolean existsByName(String name);
    
    Category getCategoryEntityById(Long id);
}
