package com.ecommerce.sbecom.service;

import com.ecommerce.sbecom.exceptions.APIExceptionHandler;
import com.ecommerce.sbecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sbecom.model.Category;
import com.ecommerce.sbecom.payload.CategoryDTO;
import com.ecommerce.sbecom.payload.CategoryResponse;
import com.ecommerce.sbecom.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;



// Service Layer is used to handle all the business logics
// All the mentioned things were in the Controller layer and it was transferred here

@Service
public class CategoryServiceImp implements CategoryService {

    // Here we need the list of categories instead of one category
//    private List<Category> categories = new ArrayList<>();

    // Using Autowire injecting the dependency
    @Autowired
    private CategoryRepository categoryRepository;

    // This is used to convert models
    @Autowired
    private ModelMapper modelMapper;


    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String order) {
        //return categories;
        Sort sortByAndOrder = order.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // Using Pageable to gather the details
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        // Using the above to fetch the pages from the database
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        // Storing it as a list of categories
        List<Category> allCategories =  categoryPage.getContent();

        // Here we are going to set all the meta datas


        // Checking if allCategories is empty
        if(allCategories.isEmpty()) {
            throw new APIExceptionHandler("There is no category available");
        }

        // Now here we are implementing DTOS
        // We are going to convert the list of Category into List of Category DTOS using model mapper
        List<CategoryDTO> categoriesDTO = allCategories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class)).toList();

        // Now we are putting this into the CategoryResponse class
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoriesDTO);

        // Here we are setting all the pageable meta datas
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setLastPage(categoryPage.isLast());

        // Changing the above to return categories from the repository
        return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {

        // To create a unique identifier, we can make use of the index of the array list
//        nextId = nextId + 1;
//        category.setCategoryId(nextId);
        //categories.add(category);

        // Should convert the category DTO to an entity
        // We can use model mapper for this
        Category categoryEntity = modelMapper.map( categoryDTO, Category.class);


        // Here we are going to throw an exception when there already exists a category name
        Category categoryFromDb = categoryRepository.findByCategoryName(categoryEntity.getCategoryName());

        // If the savedCategory is null then n towo category with same name exists
        if (categoryFromDb != null) {
            throw new APIExceptionHandler("Category " + categoryFromDb.getCategoryName() + " already exists");
        }

        // Creating using repository
        Category savedCategory = categoryRepository.save(categoryEntity);

        // Converting the entity back to dto
        CategoryDTO savedCategoryDTO = modelMapper.map(savedCategory, CategoryDTO.class);

        return savedCategoryDTO;

    }

    @Override
    public CategoryDTO deleteCategory(int categoryid) {

        // We can also modify this by extracting only the needed category
        Optional<Category> category = categoryRepository.findById(categoryid);

        // Storing the category if it exists or throwing an exception if it does not
        Category categoryToDelete = category.orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryid));

        categoryRepository.delete(categoryToDelete);

        // Converting the entity to DTO and returning it
        CategoryDTO categoryDTO = modelMapper.map(categoryToDelete, CategoryDTO.class);

//        // Here we are going to get the particular category object.
//        // We are going to convert the list into streams and compare the category id with everything and getting the needed
//        // Here if the needed object is not present, we are assigning null value inorder to avoid any errors
//
//        // Storing all the categories from the repository in the list
//        List<Category> categories = categoryRepository.findAll();
//
//        // Here instead of storing NULL we are throwing an exception and we are catching it in the controller.
//        Category category = categories.stream()
//                .filter(c -> c.getCategoryId() == categoryid)
//                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "The ID not found!"));
//
//        // Removing the object
//        //categories.remove(category);
//
//        categoryRepository.delete(category);
//
//        // Throwing response exception we dont need this.
//        //if(category == null) {
//        //    return "Category with id " + categoryid + " not found";
//        //}

        return categoryDTO;
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, int categoryid) {

        // Before performing operations we need to convert it into an entity
        Category categoryEntity = modelMapper.map(categoryDTO, Category.class);

        // Before we need to fetch all cateogries
        // Convert it into stream, filter the needed category and do the process but it can also be done like below

        // Fetching the needed category
        Optional<Category> toUpdate = categoryRepository.findById(categoryid);

        // Store the category if exists or null if it doesnt and throw an error
        Category categoryToUpdate = toUpdate.orElseThrow(()-> new ResourceNotFoundException("Category", "categoryId", categoryid));

        // Set the categoryToUpdate name
        categoryToUpdate.setCategoryId(categoryid);
        categoryToUpdate.setCategoryName(categoryDTO.getCategoryName());

        // Saving the updated category
        categoryRepository.save(categoryToUpdate);

        // Convert the entity back to DTO and send it
        CategoryDTO updatedCategoryDTO = modelMapper.map(categoryToUpdate, CategoryDTO.class);

//        // Storing all the categories from the repository in the list
//        List<Category> categories = categoryRepository.findAll();
//
//        // Getting the appropriate object using stream
//        // The below will give us the object of present or give us null
//       Optional<Category> categoryUpdate = categories.stream()
//               .filter(c -> c.getCategoryId() == categoryid)
//               .findFirst();
//
//       // Check if the object is present and modifying
//        // So, isPresent can only be used if Optional has been used.
//        if (categoryUpdate.isPresent()) {
//            // Fetching the needed category
//            Category existingCategory = categoryUpdate.get();
//
//            // Updating it
//            existingCategory.setCategoryName(category.getCategoryName());
//
//            // Saving it using repository
//            Category categorySaved = categoryRepository.save(existingCategory);
//
//            return categorySaved;
//        }else{
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot update because the ID not found!");
//        }

        return updatedCategoryDTO;
    }
}
