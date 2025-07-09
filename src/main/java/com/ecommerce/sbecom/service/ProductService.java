package com.ecommerce.sbecom.service;


import com.ecommerce.sbecom.payload.ProductDTO;
import com.ecommerce.sbecom.payload.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {
    ProductDTO addProduct(Integer categoryId, ProductDTO productDTO);

    ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String order, String sortBy);

    ProductResponse getAllProductsByCategory(Integer categoryId, Integer pageNumber, Integer pageSize,  String order, String sortBy);

    ProductResponse getAllProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize,  String order, String sortBy);

    ProductDTO updateProduct(Long productId, ProductDTO productDTO);

    ProductDTO deleteProduct(Long productId);

    ProductDTO uploadProductImage(Long productId, MultipartFile file) throws IOException;
}
