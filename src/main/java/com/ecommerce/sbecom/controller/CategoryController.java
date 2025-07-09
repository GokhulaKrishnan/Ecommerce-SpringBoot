package com.ecommerce.sbecom.controller;

// here we are going to create the endpoints
// We have not created any categories, we can create it using post method


import com.ecommerce.sbecom.config.AppConstants;
import com.ecommerce.sbecom.payload.CategoryDTO;
import com.ecommerce.sbecom.payload.CategoryResponse;
import com.ecommerce.sbecom.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


// Marking the class as the rest controller
@RestController
//@RequestMapping("/api")
public class CategoryController {

    // Create an object for the service
    private CategoryService categoryService;


    // Dummy API to test @RequestParam
    @GetMapping("/echo")
    public ResponseEntity<String> echoMessage(@RequestParam(name = "message", defaultValue = "Hi") String message) {
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    // Constructor Injection / We can also do field injection
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Creating endpoint to return the available categories
    @GetMapping("/api/public/categories")
    //@RequestMapping(value = "/api/public/categories", method = RequestMethod.GET)
    public ResponseEntity<CategoryResponse> getAllCategories(@RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                             @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                             @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
                                                             @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_ORDER, required = false) String sortOrder) {
        CategoryResponse categoryResponse = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(categoryResponse, HttpStatus.OK);
    }


    // Post method to create new activities and passed the category using the api call

    // Now we are going to modify the endpoint to make use of the CategoryDTO
    @PostMapping("/api/admin/category")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO savedCategory = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    // Creating an endpoint to delete a category

    // Here we also need to catch the excpetion thrown by the service layer when id is not found.
    // We are also using response entity in order to send readable messages to the client rather than sending error code.\

    // We are using try and catch.
    // Response entity is a wrapper class used to send meaningful messages with status code.
    @DeleteMapping("/api/admin/category/{categoryid}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable int categoryid) {

        CategoryDTO categoryDTO = categoryService.deleteCategory(categoryid);
        return new ResponseEntity<>(categoryDTO, HttpStatus.OK);


    }


    // Creating an endpoint to update a category

    // Here we will get both the category id and category object to update

    @PutMapping("/api/admin/categories/{categoryid}")
    public ResponseEntity<CategoryDTO> updateCategory(@RequestBody CategoryDTO categoryDTO, @PathVariable int categoryid) {

        // Should pass the id and object to the service layer
        CategoryDTO updatedCategory = categoryService.updateCategory(categoryDTO, categoryid);
        return new ResponseEntity<>(updatedCategory, HttpStatus.OK);

    }



}
