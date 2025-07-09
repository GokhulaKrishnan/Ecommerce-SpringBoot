package com.ecommerce.sbecom.repositories;

import com.ecommerce.sbecom.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// The Category is the entity and int is the data type of the primary key
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // A custom written query to fetch category by namex
    Category findByCategoryName(@NotBlank @Size(min = 5, message = "There should be a minimum of 5 characters") String categoryName);
}
