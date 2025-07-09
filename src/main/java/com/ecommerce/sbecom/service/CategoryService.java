package com.ecommerce.sbecom.service;

// This is the interface for the Service layer

import com.ecommerce.sbecom.payload.CategoryDTO;
import com.ecommerce.sbecom.payload.CategoryResponse;

import java.util.List;

public interface CategoryService {

    public CategoryResponse getAllCategories(Integer pageNumber,Integer pageSize, String sortBy, String sortOrder);

    public CategoryDTO createCategory(CategoryDTO categoryDTO);

    CategoryDTO deleteCategory(int categoryid);

    CategoryDTO updateCategory(CategoryDTO categoryDTO, int categoryid);
}
