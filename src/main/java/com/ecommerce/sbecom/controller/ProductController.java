package com.ecommerce.sbecom.controller;

import com.ecommerce.sbecom.config.AppConstants;
import com.ecommerce.sbecom.payload.ProductDTO;
import com.ecommerce.sbecom.payload.ProductResponse;
import com.ecommerce.sbecom.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    // Getting Product Endpoint
    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getProducts(@RequestParam(name = "pageNumber", required = false, defaultValue = AppConstants.PAGE_NUMBER)
                                                           Integer pageNumber,
                                                       @RequestParam(name = "pageSize", required = false, defaultValue = AppConstants.PAGE_SIZE)
                                                           Integer pageSize,
                                                       @RequestParam(name = "sortOrder", required = false, defaultValue = AppConstants.SORT_ORDER)
                                                           String sortOrder,
                                                       @RequestParam(name = "sortBy", required = false, defaultValue = AppConstants.SORT_BY_PRICE)
                                                           String sortBy ) {


        ProductResponse productResponse = productService.getAllProducts(pageNumber, pageSize, sortOrder, sortBy);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    // Getting Product by Category
    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductByCategoryId(@PathVariable Integer categoryId,
                                                                  @RequestParam(name = "pageNumber", required = false, defaultValue = AppConstants.PAGE_NUMBER)
                                                                  Integer pageNumber,
                                                                  @RequestParam(name = "pageSize", required = false, defaultValue = AppConstants.PAGE_SIZE)
                                                                      Integer pageSize,
                                                                  @RequestParam(name = "sortOrder", required = false, defaultValue = AppConstants.SORT_ORDER)
                                                                      String sortOrder,
                                                                  @RequestParam(name = "sortBy", required = false, defaultValue = AppConstants.SORT_BY)
                                                                      String sortBy){

        ProductResponse productResponse = productService.getAllProductsByCategory(categoryId, pageNumber, pageSize, sortOrder, sortBy);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    // Getting Product by a Keyword
    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductByKeyword(@PathVariable String keyword,
                                                               @RequestParam(name = "pageNumber", required = false, defaultValue = AppConstants.PAGE_NUMBER)
                                                               Integer pageNumber,
                                                               @RequestParam(name = "pageSize", required = false, defaultValue = AppConstants.PAGE_SIZE)
                                                                   Integer pageSize,
                                                               @RequestParam(name = "sortOrder", required = false, defaultValue = AppConstants.SORT_ORDER)
                                                                   String sortOrder,
                                                               @RequestParam(name = "sortBy", required = false, defaultValue = AppConstants.SORT_BY_PRODUCTNAME)
                                                                   String sortBy){

        ProductResponse productResponse = productService.getAllProductsByKeyword(keyword, pageNumber, pageSize, sortOrder, sortBy);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    // Add Product Endpoint
    @PostMapping("/admin/categories/{categoryId}/product")
    public ResponseEntity<ProductDTO> saveProduct(@RequestBody ProductDTO productDTO,
                                                 @PathVariable Integer categoryId) {
        // Passing it to the service
        ProductDTO savedProduct = productService.addProduct(categoryId, productDTO);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    // Update Product Endpoint
    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long productId,
                                                    @RequestBody ProductDTO productDTO){

        // Passing the productId and product to the service
        ProductDTO savedProductDTO = productService.updateProduct(productId, productDTO);
        return new ResponseEntity<>(savedProductDTO, HttpStatus.OK);
    }

    // Delete Product
    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId){

        // Using service layer to delete
        ProductDTO productDTO = productService.deleteProduct(productId);

        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }

    // Upload file image to the existing product
    @PutMapping("/admin/products/{productId}/image")
    public ResponseEntity<ProductDTO> uploadImage(@PathVariable Long productId,@RequestParam(name = "image") MultipartFile file) throws IOException {

        // Using service layer to upload
        ProductDTO productDTO = productService.uploadProductImage(productId, file);
        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }

}
